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

import de.twt.client.modbus.common.ModbusData;
import de.twt.client.modbus.common.cache.ModbusDataCacheManager;
import de.twt.client.modbus.common.constants.EventConstants;
import de.twt.client.modbus.publisher.EventModbusData.Slave;
import de.twt.client.modbus.publisher.EventModbusData.Slave.SlaveData;
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
	
	private final Logger logger = LogManager.getLogger(Publisher.class);
	private boolean stopPublishing;
	private EventModbusData configModbusData;
	private String eventType;
	private SystemRequestDTO source;
	
	//-------------------------------------------------------------------------------------------------
	// Equipment Ontology event
	
	public void publishOntology() {
		
	}
	
	//-------------------------------------------------------------------------------------------------
	// Modbus Data event
	
	// publish all events (modbus data) regularly which are described in PublisherConfig
	public void publishModbusData(EventModbusData configModbusData){
		logger.debug("start publishing all events regularly...");
		this.configModbusData = configModbusData;
		eventType = configModbusData.getEventType();
		source = createSystemRequestDTO();
		stopPublishing = false;
		new Thread(){
			public void run(){
				while(!stopPublishing){
					publishModbusDataOnce();
				}
			}
		}.start();
	}
	
	// publish all events (modbus data) only once which are described in PublisherConfig (different slaves)
	public void publishModbusDataOnce() { 
		logger.debug("publish all events once...");
		List<Slave> slaves = configModbusData.getSlaves();
		for (int idx = 0; idx < slaves.size(); idx++) {
			publishModbusDataOnceWithSlaveAddress(slaves.get(idx));
		}
	}
	
	// get related data (certain slave) from modbus data cache 
	private void publishModbusDataOnceWithSlaveAddress(Slave slaveConfig){
		final String slaveAddress = slaveConfig.getSlaveAddress();
		if (!ModbusDataCacheManager.containsSlave(slaveAddress)) {
			logger.warn("The slave ({}) does not exist in the modbus data cache.", slaveAddress);
			return;
		}
		
		ModbusData modbusData = new ModbusData();
		List<SlaveData> slaveData = slaveConfig.getData();
		for (int idx = 0; idx < slaveData.size(); idx++) {
			setModbusData(modbusData, slaveAddress, slaveData.get(idx));
		}
		final String payload = Utilities.toJson(modbusData);
		final Map<String,String> metadata = new HashMap<String,String>();
		metadata.put(EventConstants.MODBUS_DATA_METADATA_SLAVEADDRESS, slaveAddress);
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
	
	private void setModbusData(ModbusData modbusData, String slaveAddress, SlaveData slaveDataConfig) {
		final String type = slaveDataConfig.getType();
		final int startAddress = slaveDataConfig.getStartAddress();
		final int length = slaveDataConfig.getLength();
		
		switch(type){
		case EventConstants.MODBUS_DATA_METADATA_TYPE_COIL: 
			modbusData.setCoils(getFilteredData(ModbusDataCacheManager.getCoils(slaveAddress), startAddress, length)); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_DISCRETE_INPUT: 
			modbusData.setDiscreteInputs(getFilteredData(ModbusDataCacheManager.getDiscreteInputs(slaveAddress), startAddress, length)); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_HOLDING_REGISTER: 
			modbusData.setHoldingRegisters(getFilteredData(ModbusDataCacheManager.getHoldingRegisters(slaveAddress), startAddress, length)); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_INPUT_REGISTER: 
			modbusData.setInputRegisters(getFilteredData(ModbusDataCacheManager.getInputRegisters(slaveAddress), startAddress, length)); break;
		}
	}
	
	private <T> HashMap<Integer, T> getFilteredData(HashMap<Integer, T> data, int startAddress, int length){
		HashMap<Integer, T> filteredData = new HashMap<Integer, T>();
		for(int idx = 0; idx < length; idx++) {
			int id = idx + startAddress;
			filteredData.put(id, data.get(id));
		}
		return filteredData;
	}
	/*
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
	*/
}
