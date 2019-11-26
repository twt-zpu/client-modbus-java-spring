package eu.arrowhead.client.modbus.publisher;

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

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.library.util.ClientCommonConstants;
import eu.arrowhead.client.modbus.cache.data.IModbusDataCacheManager;
import eu.arrowhead.client.modbus.cache.data.ModbusDataCacheManagerImpl;
import eu.arrowhead.client.modbus.publisher.event.PublisherConfig;
import eu.arrowhead.client.modbus.publisher.event.PublisherConfig.Slave;
import eu.arrowhead.client.modbus.publisher.event.PublisherConfig.Slave.SlaveData;
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
	private final IModbusDataCacheManager dataCache = new ModbusDataCacheManagerImpl();
	private boolean stopPublishing;
	private String eventType;
	private SystemRequestDTO source;
	
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
	
	public void publishOnce() {	
		logger.debug("publish all events once...");
		List<Slave> slaves = config.getSlaves();
		for (int idx = 0; idx < slaves.size(); idx++) {
			publishOnceWithSlaveAddress(slaves.get(idx));
		}
	}
	
	private void publishOnceWithSlaveAddress(Slave slaveConfig){
		final String slaveAddress = slaveConfig.getSlaveAddress();
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
	
	private void publishOnceWithSlaveAddressAndDataConfig(String slaveAddress, SlaveData slaveDataConfig){
		final String type = slaveDataConfig.getType();
		final int startAddress = slaveDataConfig.getStartAddress();
		final int length = slaveDataConfig.getLength();
		final Map<String,String> metadata = new HashMap<String,String>();
		metadata.put("slaveAddress", slaveAddress);
		metadata.put("type", type);
		metadata.put("startAddress", String.valueOf(startAddress));
		final String payload = getEventPlayload(slaveAddress, type, startAddress, length);
		final String timeStamp = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final EventPublishRequestDTO publishRequestDTO = new EventPublishRequestDTO(eventType, source, metadata, payload, timeStamp);
		arrowheadService.publishToEventHandler(publishRequestDTO);
	}
	
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
	
	private String getEventPlayload(String slaveAddress, String type, int startAddress, int length) {
		logger.debug("generate the event playload...");
		String playload = "";
		switch(type){
		case "coil": playload = getEventPlayload(dataCache.getCoils(slaveAddress), 
				startAddress, length); break;
		case "discreteInput": playload = getEventPlayload(dataCache.getDiscreteInputs(slaveAddress), 
				startAddress, length); break;
		case "holdingRegister": playload = getEventPlayload(dataCache.getHoldingRegisters(slaveAddress), 
				startAddress, length); break;
		case "InputRegister": playload = getEventPlayload(dataCache.getInputRegisters(slaveAddress), 
				startAddress, length); break;
		}
		return playload;
	}
	
	private <T> String getEventPlayload(HashMap<Integer, T> data, int startAddress, int length){
		String playload = "";
		for(int idx = startAddress; idx < length; idx++) {
			playload += data.get(idx).toString() + ',';
		}
		return playload.substring(0, playload.length()-1);
	}
	
}
