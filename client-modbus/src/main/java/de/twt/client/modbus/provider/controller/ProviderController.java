package de.twt.client.modbus.provider.controller;

import java.util.HashMap;
import java.util.Map;

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
import de.twt.client.modbus.common.cache.request.IModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.cache.request.ModbusWriteRequestCacheManagerImpl;
import de.twt.client.modbus.provider.ModbusProviderConstants;

@RestController
public class ProviderController {
	
	//=================================================================================================
	// members
	private IModbusDataCacheManager modbusDataCacheManager = new ModbusDataCacheManagerImpl();
	private IModbusWriteRequestCacheManager modbusWriteRequestCacheManager = new ModbusWriteRequestCacheManagerImpl();
	private String slaveAddress;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/*@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}*/
	
	//-------------------------------------------------------------------------------------------------
	/*@GetMapping(path = CommonConstants.ECHO_URI)
	public String setSlaveAddress(@RequestParam(name = ModbusProviderConstants.REQUEST_PARAM_KEY_SLAVEADDRESS) final String slaveAddress) {
		if (slaveAddress != null){
			this.slaveAddress = slaveAddress;
		}
		this.slaveAddress = slaveAddress;
		modbusDataCacheManager.createModbusData(slaveAddress);
		return "Got it!";
	}*/
	
	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = ModbusProviderConstants.READ_MODBUS_DATA_URI)
	public ModbusResponseDTO readModbusData(@RequestBody final ModbusReadRequestDTO request) {
		ModbusResponseDTO response = new ModbusResponseDTO();
		ModbusData modbusData = new ModbusData();
		if (!request.getCoilsAddressMap().isEmpty()){
			modbusData.setCoils(readData("coil", request.getCoilsAddressMap()));
		}
		if (!request.getDiscreteInputsAddressMap().isEmpty()){
			modbusData.setDiscreteInputs(readData("discreteInput", request.getDiscreteInputsAddressMap()));
		}
		if (!request.getHoldingRegistersAddressMap().isEmpty()){
			modbusData.setHoldingRegisters(readData("holdingRegister", request.getHoldingRegistersAddressMap()));
		}
		if (!request.getInputRegistersAddressMap().isEmpty()){
			modbusData.setInputRegisters(readData("inputRegister", request.getInputRegistersAddressMap()));
		}
		
		response.addE(modbusData);
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = ModbusProviderConstants.WRITE_MODBUS_DATA_URI)
	public boolean writeModbusData(@RequestParam(name = ModbusProviderConstants.REQUEST_PARAM_KEY_SLAVEADDRESS, required = false) final String slaveAddress,
			@RequestBody final ModbusWriteRequestDTO request) {
		if (slaveAddress != null){
			this.slaveAddress = slaveAddress;
		}
		modbusWriteRequestCacheManager.putReadRequest(this.slaveAddress, request);
		return true;
	}
	
	//=================================================================================================
	// methods
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> HashMap<Integer, T> readData(String type, HashMap<Integer, Integer> addressMap){
		HashMap<Integer, T> inputs = new HashMap<Integer, T>();
		for(Map.Entry entry: addressMap.entrySet()){
			int address = (int) entry.getKey();
			int quantity = (int) entry.getValue();
			for(int idx = 0; idx < quantity; idx++){
				int key = address + idx;
				T value = null;
				switch (type) {
				case "coil": value = (T) modbusDataCacheManager.getCoils(slaveAddress).get(key); break;
				case "discreteInput": value = (T) modbusDataCacheManager.getDiscreteInputs(slaveAddress).get(key); break;
				case "holdingRegister": value = (T) modbusDataCacheManager.getHoldingRegisters(slaveAddress).get(key); break;
				case "inputRegister": value = (T) modbusDataCacheManager.getInputRegisters(slaveAddress).get(key); break;
				}
				inputs.put(key, value);
			}
		}
		return inputs;
	}
}
