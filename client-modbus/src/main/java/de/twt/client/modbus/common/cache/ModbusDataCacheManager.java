package de.twt.client.modbus.common.cache;

import java.util.HashMap;

import de.twt.client.modbus.common.ModbusData;
import eu.arrowhead.common.Utilities;

public class ModbusDataCacheManager {
	private final static HashMap<String, ModbusData> modbusDataCaches = new HashMap<String, ModbusData>();
	private final static HashMap<String, Boolean> updateStatus = new HashMap<String, Boolean>();
	
	static public boolean containsSlave(String slaveAddress) {
		return modbusDataCaches.containsKey(slaveAddress);
	}
	
	static public boolean getUpdateStatus(String slaveAddress) {
		return updateStatus.get(slaveAddress);
	}
	
	static public void setUpdateStatus(String slaveAddress, boolean updateStatus) {
		ModbusDataCacheManager.updateStatus.put(slaveAddress, updateStatus);
	}
	
	synchronized static public void createModbusData(String slaveAddress){
		if (!modbusDataCaches.containsKey(slaveAddress)) {
			modbusDataCaches.put(slaveAddress, new ModbusData());
		}
	}
	
	synchronized static public void deleteModbusData(String slaveAddress){
		modbusDataCaches.remove(slaveAddress);
	}
	
	synchronized static public void putModbusData(String slaveAddress, ModbusData modbusData){
		modbusDataCaches.put(slaveAddress, modbusData);
	}
	
	synchronized static public void setCoil(String slaveAddress, int address, boolean value){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoil(address, value);
	}
	
	synchronized static public void setCoils(String slaveAddress, int address, boolean[] values){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoils(address, values);
	}
	
	synchronized static public void setCoils(String slaveAddress, HashMap<Integer, Boolean> coils){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoils(coils);
	}
	
	
	@SuppressWarnings("unchecked")
	synchronized static public HashMap<Integer, Boolean> getCoils(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Boolean>) modbusDataCaches.get(slaveAddress).getCoils().clone();
	}
	
	synchronized static public void setDiscreteInput(String slaveAddress, int address, boolean value){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInput(address, value);
	}
	
	synchronized static public void setDiscreteInputs(String slaveAddress, int address, boolean[] values){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInputs(address, values);
	}
	
	synchronized static public void setDiscreteInputs(String slaveAddress, HashMap<Integer, Boolean> discreteInputs){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInputs(discreteInputs);
	}
	
	@SuppressWarnings("unchecked")
	synchronized static public HashMap<Integer, Boolean> getDiscreteInputs(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Boolean>) modbusDataCaches.get(slaveAddress).getDiscreteInputs().clone();
	}
	
	synchronized static public void setHoldingRegister(String slaveAddress, int address, int value){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegister(address, value);
	}
	
	synchronized static public void setHoldingRegisters(String slaveAddress, int address, int[] values){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegisters(address, values);
	}
	
	synchronized static public void setHoldingRegisters(String slaveAddress, HashMap<Integer, Integer> holdingRegisters){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegisters(holdingRegisters);
	}
	
	@SuppressWarnings("unchecked")
	synchronized static public HashMap<Integer, Integer> getHoldingRegisters(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Integer>) modbusDataCaches.get(slaveAddress).getHoldingRegisters().clone();
	}
	
	synchronized static public void setInputRegister(String slaveAddress, int address, int value){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegister(address, value);
	}
	
	synchronized static public void setInputRegisters(String slaveAddress, int address, int[] values){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegisters(address, values);
	}
	
	static public void setInputRegisters(String slaveAddress, HashMap<Integer, Integer> inputRegisters){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegisters(inputRegisters);
	}
	
	@SuppressWarnings("unchecked")
	static public HashMap<Integer, Integer> getInputRegisters(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Integer>) modbusDataCaches.get(slaveAddress).getInputRegisters().clone();
	}
	
	static public void setModbusData(String slaveAddress, ModbusData modbusData){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		createModbusData(slaveAddress);
		ModbusData data = modbusDataCaches.get(slaveAddress);
		data.setCoils(modbusData.getCoils());
		data.setDiscreteInputs(modbusData.getDiscreteInputs());
		data.setHoldingRegisters(modbusData.getHoldingRegisters());
		data.setInputRegisters(modbusData.getInputRegisters());
	}
}
