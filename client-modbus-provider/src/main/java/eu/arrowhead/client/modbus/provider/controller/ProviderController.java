package eu.arrowhead.client.modbus.provider.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.client.modbus.cache.data.IModbusDataCacheManager;
import eu.arrowhead.client.modbus.cache.data.ModbusDataCacheManagerImpl;
import eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager;
import eu.arrowhead.client.modbus.common.ModbusData;
import eu.arrowhead.client.modbus.common.ModbusReadRequestDTO;
import eu.arrowhead.client.modbus.common.ModbusResponseDTO;
import eu.arrowhead.client.modbus.common.ModbusWriteRequestDTO;
import eu.arrowhead.client.modbus.provider.ModbusProviderConstants;
import eu.arrowhead.common.CommonConstants;

@RestController
public class ProviderController {
	
	//=================================================================================================
	// members
	private IModbusDataCacheManager modbusDataCacheManager = new ModbusDataCacheManagerImpl();
	private IModbusWriteRequestCacheManager modbusWriteRequestCacheManager = (IModbusWriteRequestCacheManager) new ModbusDataCacheManagerImpl();
	private String slaveAddress;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String setSlaveAddress(@RequestParam(name = ModbusProviderConstants.REQUEST_PARAM_SLAVEADDRESS) final String slaveAddress) {
		this.slaveAddress = slaveAddress;
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = ModbusProviderConstants.READ_MODBUS_DATA_URI)
	public ModbusResponseDTO readModbusData(@RequestBody final ModbusReadRequestDTO request) {
		ModbusResponseDTO response = new ModbusResponseDTO();
		ModbusData modbusData = new ModbusData();
		if (!request.getCoilsAddressMap().isEmpty()){
			modbusData.setCoils(readCoils(request.getCoilsAddressMap()));
		}
		if (!request.getDiscreteInputsAddressMap().isEmpty()){
			
		}
		if (!request.getHoldingRegistersAddressMap().isEmpty()){
			
		}
		if (!request.getInputRegistersAddressMap().isEmpty()){
			
		}
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = ModbusProviderConstants.WRITE_MODBUS_DATA_URI)
	public void writeModbusData(@RequestParam(name = ModbusProviderConstants.REQUEST_PARAM_SLAVEADDRESS, required = false) final String slaveAddress,
			@RequestBody final ModbusWriteRequestDTO request) {
		if (slaveAddress != null){
			this.slaveAddress = slaveAddress;
		}
		modbusWriteRequestCacheManager.putReadRequest(this.slaveAddress, request);
	}
	
	//=================================================================================================
	// methods
	@SuppressWarnings("rawtypes")
	private HashMap<Integer, Boolean> readCoils(HashMap<Integer, Integer> coilsAddress){
		HashMap<Integer, Boolean> coils = new HashMap<Integer, Boolean>();
		for(Map.Entry entry: coilsAddress.entrySet()){
			int address = (int) entry.getKey();
			int quantity = (int) entry.getValue();
			for(int idx = 0; idx < quantity; idx++){
				int key = address + idx;
				coils.put(key, modbusDataCacheManager.getCoils("").get(key));
			}
		}
		return coils;
	}
}
