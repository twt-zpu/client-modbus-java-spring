package de.twt.client.modbus.subscriber.controller;


import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.twt.client.modbus.common.cache.data.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.data.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.constants.EventConstants;
import de.twt.client.modbus.subscriber.constants.SubscriberConstants;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.EventDTO;

@RestController
@RequestMapping(SubscriberConstants.DEFAULT_EVENT_NOTIFICATION_BASE_URI)
public class SubscriberController {
	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(SubscriberController.class);
	private final IModbusDataCacheManager dataCache = new ModbusDataCacheManagerImpl();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	//TODO: implement here additional subscriber related REST end points
	@PostMapping(path = SubscriberConstants.MODBUS_DATA_URI) 
	public void receivePublsisherEventModbusData(@RequestBody final EventDTO event) {
		logger.info("receivePublsisherStartedRunEvent started... ");
		if( event.getEventType() == null) {			
			logger.debug("EventType is null.");
			return;
		}
		Map<String, String> metadata = event.getMetaData();
		String[] values = event.getPayload().split(",");
		final String slaveAddress = metadata.get(EventConstants.MODBUS_DATA_METADATA_SLAVEADDRESS);
		final String type = metadata.get(EventConstants.MODBUS_DATA_METADATA_TYPE);
		final int startAddress = Integer.parseInt(metadata.get(EventConstants.MODBUS_DATA_METADATA_STARTADDRESS));
		switch(type) {
		case EventConstants.MODBUS_DATA_METADATA_TYPE_COIL: 
			dataCache.setCoils(slaveAddress, getBooleanMap(startAddress, values)); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_DISCRETE_INPUT:
			dataCache.setDiscreteInputs(slaveAddress, getBooleanMap(startAddress, values)); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_HOLDING_REGISTER:
			dataCache.setHoldingRegisters(slaveAddress, getIntegerMap(startAddress, values)); break;
		case EventConstants.MODBUS_DATA_METADATA_TYPE_INPUT_REGISTER: 
			dataCache.setInputRegisters(slaveAddress, getIntegerMap(startAddress, values)); break;
		default: logger.warn("receivePublsisherEventModbusData: there is no metadata with type {}.", type); break;
		}
	}
	
	private HashMap<Integer, Boolean> getBooleanMap(int startAddress, String[] values) {
		HashMap<Integer, Boolean> map = new HashMap<Integer, Boolean>();
		for(int idx = 0; idx < values.length; idx++){
			if (!(values[idx].equalsIgnoreCase("true") || 
					values[idx].equalsIgnoreCase("false"))) {
				logger.warn("The data from the provider is not set correctly!");
				continue;
			}
			int offset = idx + startAddress;
			Boolean value = Boolean.valueOf(values[idx]);
			map.put(offset, value);
		}
		return map;
	}
	
	private HashMap<Integer, Integer> getIntegerMap(int startAddress, String[] values) {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(int idx = 0; idx < values.length; idx++){
			if (values[idx] == null || values[idx].length() == 0) {
				logger.warn("The data from the provider is not set correctly!");
	            continue;
	        }
			if (!(values[idx].chars().allMatch(Character::isDigit))) {
				logger.warn("The data from the provider is not set correctly!");
				continue;
			}
			int offset = idx + startAddress;
			Integer value = Integer.valueOf(values[idx]);
			map.put(offset, value);
		}
		return map;
	}
}
