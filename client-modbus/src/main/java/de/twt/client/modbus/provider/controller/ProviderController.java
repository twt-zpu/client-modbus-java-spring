package de.twt.client.modbus.provider.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.twt.client.modbus.common.ModbusData;
import de.twt.client.modbus.common.ModbusReadRequestDTO;
import de.twt.client.modbus.common.ModbusResponseDTO;
import de.twt.client.modbus.common.ModbusWriteRequestDTO;
import de.twt.client.modbus.common.cache.data.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.data.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.cache.request.IModbusReadRequestCacheManager;
import de.twt.client.modbus.common.cache.request.IModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.cache.request.ModbusReadRequestCacheManagerImpl;
import de.twt.client.modbus.common.cache.request.ModbusWriteRequestCacheManagerImpl;
import de.twt.client.modbus.common.constants.ModbusConstants;
import de.twt.client.modbus.provider.ModbusProviderConstants;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;

@RestController
public class ProviderController {
	
	//=================================================================================================
	// members
	private IModbusDataCacheManager modbusDataCacheManager = new ModbusDataCacheManagerImpl();
	private IModbusReadRequestCacheManager modbusReadRequestCacheManager = new ModbusReadRequestCacheManagerImpl();
	private IModbusWriteRequestCacheManager modbusWriteRequestCacheManager = new ModbusWriteRequestCacheManagerImpl();
	
	private final Logger logger = LogManager.getLogger(ProviderController.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		logger.debug("echo start...");
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = ModbusProviderConstants.READ_MODBUS_DATA_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	public ModbusResponseDTO readModbusData(@RequestBody final ModbusReadRequestDTO request,
			@RequestParam(name = ModbusProviderConstants.REQUEST_PARAM_KEY_SLAVEADDRESS, required = false) final String slaveAddress) {
		logger.debug("readModbusData start...");
		modbusReadRequestCacheManager.putReadRequest(slaveAddress, request);
		waitForReadRequestFinished(slaveAddress, request.getID());
		ModbusResponseDTO response = new ModbusResponseDTO();
		ModbusData modbusData = new ModbusData();
		if (!request.getCoilsAddressMap().isEmpty()){
			modbusData.setCoils(readData(slaveAddress, 
					ModbusConstants.MODBUS_DATA_TYPE_COIL, request.getCoilsAddressMap()));
		}
		if (!request.getDiscreteInputsAddressMap().isEmpty()){
			modbusData.setDiscreteInputs(readData(slaveAddress, 
					ModbusConstants.MODBUS_DATA_TYPE_DISCRETE_INPUT, request.getDiscreteInputsAddressMap()));
		}
		if (!request.getHoldingRegistersAddressMap().isEmpty()){
			modbusData.setHoldingRegisters(readData(slaveAddress, 
					ModbusConstants.MODBUS_DATA_TYPE_HOLDING_REGISTER, request.getHoldingRegistersAddressMap()));
		}
		if (!request.getInputRegistersAddressMap().isEmpty()){
			modbusData.setInputRegisters(readData(slaveAddress, 
					ModbusConstants.MODBUS_DATA_TYPE_INPUT_REGISTER, request.getInputRegistersAddressMap()));
		}
		response.addE(modbusData);
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = ModbusProviderConstants.WRITE_MODBUS_DATA_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean writeModbusData(@RequestBody final ModbusWriteRequestDTO request,
			@RequestParam(name = ModbusProviderConstants.REQUEST_PARAM_KEY_SLAVEADDRESS, required = false) final String slaveAddress) {
		logger.debug("writeModbusData({}) start...", slaveAddress);
		modbusWriteRequestCacheManager.putWriteRequest(slaveAddress, request);
		logger.debug(Utilities.toJson(modbusWriteRequestCacheManager.getWriteRequest(slaveAddress)));
		return true;
	}
	
	//=================================================================================================
	// methods
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> HashMap<Integer, T> readData(String slaveAddress, String type, HashMap<Integer, Integer> addressMap){
		HashMap<Integer, T> inputs = new HashMap<Integer, T>();
		for(Map.Entry entry: addressMap.entrySet()){
			int address = (int) entry.getKey();
			int quantity = (int) entry.getValue();
			for(int idx = 0; idx < quantity; idx++){
				int key = address + idx;
				T value = null;
				switch (type) {
				case ModbusConstants.MODBUS_DATA_TYPE_COIL: 
					value = (T) modbusDataCacheManager.getCoils(slaveAddress).get(key); break;
				case ModbusConstants.MODBUS_DATA_TYPE_DISCRETE_INPUT: 
					value = (T) modbusDataCacheManager.getDiscreteInputs(slaveAddress).get(key); break;
				case ModbusConstants.MODBUS_DATA_TYPE_HOLDING_REGISTER: 
					value = (T) modbusDataCacheManager.getHoldingRegisters(slaveAddress).get(key); break;
				case ModbusConstants.MODBUS_DATA_TYPE_INPUT_REGISTER: 
					value = (T) modbusDataCacheManager.getInputRegisters(slaveAddress).get(key); break;
				}
				inputs.put(key, value);
			}
		}
		return inputs;
	}

	private void waitForReadRequestFinished(String slaveAddress, String id) {
		int period = 0;
		while(modbusReadRequestCacheManager.isIDExist(slaveAddress, id)) {
			if (period++ > 100) {
				logger.debug("waitForReadRequestFinished: the request is not finished. use the default values in modbus data cache.");
				break;
			}
			
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
