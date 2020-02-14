package de.twt.client.modbus.publisher;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.twt.client.modbus.common.cache.ModbusDataCacheManager;
import de.twt.client.modbus.common.constants.EventConstants;
import de.twt.client.modbus.publisher.PublisherConfig.Slave;
import de.twt.client.modbus.publisher.PublisherConfig.Slave.SlaveData;
import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.library.util.ClientCommonConstants;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

@Component
public class Publisher {
	
	@Value(ClientCommonConstants.$CLIENT_SYSTEM_NAME)
	private String clientSystemName;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_ADDRESS_WD)
	private String clientSystemAddress;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_PORT_WD)
	private int clientSystemPort;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	@Autowired 
	private PublisherConfig config;
	
	private final Logger logger = LogManager.getLogger(Publisher.class);
	private boolean stopPublishing;
	private String eventType;
	private SystemRequestDTO source;
	
	// publish all events regularly which are described in PublisherConfig
	public void publish(){
		logger.debug("start publishing all events regularly...");
		eventType = config.getEventType();
		source = createSystemRequestDTO();
		stopPublishing = false;
		new Thread(){
			public void run(){
				while(!stopPublishing){
					publishOnce();
				}
			}
		}.start();
	}
	
	// publish all events only once which are described in PublisherConfig (different slaves)
	public void publishOnce() {	
		logger.debug("publish all events once...");
		List<Slave> slaves = config.getSlaves();
		for (int idx = 0; idx < slaves.size(); idx++) {
			publishOnceWithSlaveAddress(slaves.get(idx));
		}
	}
	
	// get related data (certain slave) from modbus data cache 
	private void publishOnceWithSlaveAddress(Slave slaveConfig){
		final String slaveAddress = slaveConfig.getSlaveAddress();
		if (!ModbusDataCacheManager.containsSlave(slaveAddress)) {
			logger.warn("The slave ({}) does not exist in the modbus data cache.", slaveAddress);
			return;
		}
		
		List<SlaveData> slaveData = slaveConfig.getData();
		for (int idx = 0; idx < slaveData.size(); idx++) {
			publishOnceWithSlaveAddressAndDataConfig(slaveAddress, slaveData.get(idx));
			try {
				TimeUnit.MILLISECONDS.sleep(config.getPublishingPeriodTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.warn("The sleeping time between different events does not work.");
			}
		}
	}
	
	// publish one data with the certain slave address 
	private void publishOnceWithSlaveAddressAndDataConfig(String slaveAddress, SlaveData slaveDataConfig){
		final String type = slaveDataConfig.getType();
		final int startAddress = slaveDataConfig.getStartAddress();
		final int length = slaveDataConfig.getLength();
		final Map<String,String> metadata = new HashMap<String,String>();
		metadata.put(EventConstants.MODBUS_DATA_METADATA_SLAVEADDRESS, slaveAddress);
		metadata.put(EventConstants.MODBUS_DATA_METADATA_TYPE, type);
		metadata.put(EventConstants.MODBUS_DATA_METADATA_STARTADDRESS, String.valueOf(startAddress));
		metadata.put(EventConstants.MODBUS_DATA_METADATA_MODULE, slaveDataConfig.getModule());
		final String payload = getEventPayload(slaveAddress, type, startAddress, length);
		final String timeStamp = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final EventPublishRequestDTO publishRequestDTO = new EventPublishRequestDTO(eventType, source, metadata, payload, timeStamp);
		arrowheadService.publishToEventHandler(publishRequestDTO);
	}
	
	// create the system request dto
	private SystemRequestDTO createSystemRequestDTO(){
		logger.debug("create system request dto...");
		final SystemRequestDTO source = new SystemRequestDTO();
		source.setSystemName(clientSystemName);
		source.setAddress(clientSystemAddress);
		source.setPort(clientSystemPort);
		if (sslEnabled) {
			source.setAuthenticationInfo(Base64.getEncoder().encodeToString( arrowheadService.getMyPublicKey().getEncoded()));
		}
		return source;
	}
	
	private String getEventPayload(String slaveAddress, String type, int startAddress, int length) {
		logger.debug("generate the event playload...");
		String payload = "";
		switch(type){
		case EventConstants.MODBUS_DATA_METADATA_TYPE_COIL: 
			payload = getEventPayload(ModbusDataCacheManager.getCoils(slaveAddress), startAddress, length); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_DISCRETE_INPUT: 
			payload = getEventPayload(ModbusDataCacheManager.getDiscreteInputs(slaveAddress), startAddress, length); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_HOLDING_REGISTER: 
			payload = getEventPayload(ModbusDataCacheManager.getHoldingRegisters(slaveAddress), startAddress, length); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_INPUT_REGISTER: 
			payload = getEventPayload(ModbusDataCacheManager.getInputRegisters(slaveAddress), startAddress, length); break;
		}
		return payload;
	}
	
	private <T> String getEventPayload(HashMap<Integer, T> data, int startAddress, int length){
		String payload = "";
		for(int idx = startAddress; idx < length; idx++) {
			payload += data.get(idx).toString() + ',';
		}
		return payload.substring(0, payload.length()-1);
	}
	
}
