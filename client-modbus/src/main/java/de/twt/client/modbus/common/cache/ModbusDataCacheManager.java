package de.twt.client.modbus.common.cache;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import de.twt.client.modbus.common.ModbusData;
import de.twt.client.modbus.common.SenML;
import de.twt.client.modbus.common.constants.ModbusConstants;
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
	
	synchronized static public void setInputRegisters(String slaveAddress, HashMap<Integer, Integer> inputRegisters){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegisters(inputRegisters);
	}
	
	@SuppressWarnings("unchecked")
	synchronized static public HashMap<Integer, Integer> getInputRegisters(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Integer>) modbusDataCaches.get(slaveAddress).getInputRegisters().clone();
	}
	
	synchronized static public void setModbusData(String slaveAddress, ModbusData modbusData){
		setUpdateStatus(slaveAddress, true);
		createModbusData(slaveAddress);
		ModbusData data = modbusDataCaches.get(slaveAddress);
		data.setCoils(modbusData.getCoils());
		data.setDiscreteInputs(modbusData.getDiscreteInputs());
		data.setHoldingRegisters(modbusData.getHoldingRegisters());
		data.setInputRegisters(modbusData.getInputRegisters());
	}
	
	synchronized static public void setModbusData(String slaveAddress, ModbusConstants.MODBUS_DATA_TYPE type, int address, String value) {
		createModbusData(slaveAddress);
		ModbusData data = modbusDataCaches.get(slaveAddress);
		switch(type) {
		case coil: data.setCoil(address, Boolean.valueOf(value)); break;
		case discreteInput: data.setDiscreteInput(address, Boolean.valueOf(value)); break;
		case holdingRegister: data.setHoldingRegister(address, Integer.valueOf(value)); break;
		case inputRegister: data.setInputRegister(address, Integer.valueOf(value)); break;
		}
	}
	
	synchronized static public Vector<SenML> convertToSenMLList() {
		Vector<SenML> smls = new Vector<SenML>();
		for (Map.Entry<String, ModbusData> modbusDataCacheSet : modbusDataCaches.entrySet()) {
			SenML sml = new SenML();
			String slaveAddress = modbusDataCacheSet.getKey();
			ModbusData modbusDataCache = modbusDataCacheSet.getValue();
			sml.setBn("urn:dev:ipaddr:" + slaveAddress);
			sml.setBt(1619949538d);
			sml.setN("eventType");
			sml.setVs("modbusData");
			smls.add(sml);
			
			smls.addAll(convertModbusDataMemoryTypeToSenMLList(ModbusConstants.MODBUS_DATA_TYPE.coil, modbusDataCache.getCoils()));
			smls.addAll(convertModbusDataMemoryTypeToSenMLList(ModbusConstants.MODBUS_DATA_TYPE.discreteInput, modbusDataCache.getDiscreteInputs()));
			smls.addAll(convertModbusDataMemoryTypeToSenMLList(ModbusConstants.MODBUS_DATA_TYPE.holdingRegister, modbusDataCache.getHoldingRegisters()));
			smls.addAll(convertModbusDataMemoryTypeToSenMLList(ModbusConstants.MODBUS_DATA_TYPE.inputRegister, modbusDataCache.getInputRegisters()));
		}
		
		return smls;
	}
	
	synchronized static public HashMap<String, String> convertModbusDataToCSVRecord(String slaveAddress) {
		HashMap<String, String> record = new HashMap<String, String>();
		final String timeStamp = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		record.put("timeStamp", timeStamp);
		record.put("slaveAddress", slaveAddress);
		ModbusData modbusDataCache = modbusDataCaches.get(slaveAddress);
		
		record.putAll(convertModbusDataMemoryTypeToRecord(ModbusConstants.MODBUS_DATA_TYPE.coil, modbusDataCache.getCoils()));
		record.putAll(convertModbusDataMemoryTypeToRecord(ModbusConstants.MODBUS_DATA_TYPE.discreteInput, modbusDataCache.getDiscreteInputs()));
		record.putAll(convertModbusDataMemoryTypeToRecord(ModbusConstants.MODBUS_DATA_TYPE.holdingRegister, modbusDataCache.getHoldingRegisters()));
		record.putAll(convertModbusDataMemoryTypeToRecord(ModbusConstants.MODBUS_DATA_TYPE.inputRegister, modbusDataCache.getInputRegisters()));
		
		return record;
		
	}
	
	static private <T> Vector<SenML> convertModbusDataMemoryTypeToSenMLList(ModbusConstants.MODBUS_DATA_TYPE type, HashMap<Integer, T> memoryData) {
		Vector<SenML> smls = new Vector<SenML>();
		for (Map.Entry<Integer, T> dataSet : memoryData.entrySet()) {
			SenML sml = new SenML();
			String n = type.toString() + "[" + dataSet.getKey().toString() + "]";
			sml.setN(n);
			switch(type) {
			case coil: sml.setVb((boolean) dataSet.getValue()); break;
			case discreteInput: sml.setVb((boolean) dataSet.getValue()); break;
			case holdingRegister: sml.setV((double) (int) dataSet.getValue()); break;
			case inputRegister: sml.setV((double) (int) dataSet.getValue()); break;
			}
			smls.add(sml);
		}
		
		return smls;
	}
	
	static private <T> HashMap<String, String> convertModbusDataMemoryTypeToRecord(ModbusConstants.MODBUS_DATA_TYPE type, HashMap<Integer, T> memoryData) {
		HashMap<String, String> record = new HashMap<String, String>();
		for (Map.Entry<Integer, T> dataSet : memoryData.entrySet()) {
			String key = type.toString() + "[" + dataSet.getKey().toString() + "]";
			record.put(key, dataSet.getValue().toString());
		}
		
		return record;
	}
}
