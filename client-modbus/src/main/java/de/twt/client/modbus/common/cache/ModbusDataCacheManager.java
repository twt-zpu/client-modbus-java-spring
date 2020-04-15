package de.twt.client.modbus.common.cache;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Timestamp;
import java.time.ZonedDateTime;
import java.util.Date;
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
	private static double t = 0;
	private static long timestamp = new Date().getTime();
	
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
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		if (!modbusDataCaches.containsKey(slaveAddress)) {
			modbusDataCaches.put(slaveAddress, new ModbusData());
		}
	}
	
	synchronized static public void createModbusData(String slaveAddress, ModbusData modbusData) {
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		if (!modbusDataCaches.containsKey(slaveAddress)) {
			modbusDataCaches.put(slaveAddress, modbusData);
		} else {
			ModbusData data = modbusDataCaches.get(slaveAddress);
			data.setCoils(modbusData.getCoils());
			data.setDiscreteInputs(modbusData.getDiscreteInputs());
			data.setHoldingRegisters(modbusData.getHoldingRegisters());
			data.setInputRegisters(modbusData.getInputRegisters());
		}
	}
	
	synchronized static public void deleteModbusData(String slaveAddress){
		modbusDataCaches.remove(slaveAddress);
	}
	
	synchronized static public void putModbusData(String slaveAddress, ModbusData modbusData){
		modbusDataCaches.put(slaveAddress, modbusData);
	}
	
	synchronized static public void setCoil(String slaveAddress, int address, boolean value){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoil(address, value);
	}
	
	synchronized static public void setCoils(String slaveAddress, int address, boolean[] values){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoils(address, values);
	}
	
	synchronized static public void setCoils(String slaveAddress, HashMap<Integer, Boolean> coils){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoils(coils);
	}
	
	
	@SuppressWarnings("unchecked")
	synchronized static public HashMap<Integer, Boolean> getCoils(String slaveAddress){
		createModbusData(slaveAddress);
		return (HashMap<Integer, Boolean>) modbusDataCaches.get(slaveAddress).getCoils().clone();
	}
	
	synchronized static public void setDiscreteInput(String slaveAddress, int address, boolean value){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInput(address, value);
	}
	
	synchronized static public void setDiscreteInputs(String slaveAddress, int address, boolean[] values){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInputs(address, values);
	}
	
	synchronized static public void setDiscreteInputs(String slaveAddress, HashMap<Integer, Boolean> discreteInputs){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInputs(discreteInputs);
	}
	
	@SuppressWarnings("unchecked")
	synchronized static public HashMap<Integer, Boolean> getDiscreteInputs(String slaveAddress){
		createModbusData(slaveAddress);
		return (HashMap<Integer, Boolean>) modbusDataCaches.get(slaveAddress).getDiscreteInputs().clone();
	}
	
	synchronized static public void setHoldingRegister(String slaveAddress, int address, int value){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegister(address, value);
	}
	
	synchronized static public void setHoldingRegisters(String slaveAddress, int address, int[] values){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegisters(address, values);
	}
	
	synchronized static public void setHoldingRegisters(String slaveAddress, HashMap<Integer, Integer> holdingRegisters){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegisters(holdingRegisters);
	}
	
	@SuppressWarnings("unchecked")
	synchronized static public HashMap<Integer, Integer> getHoldingRegisters(String slaveAddress){
		createModbusData(slaveAddress);
		return (HashMap<Integer, Integer>) modbusDataCaches.get(slaveAddress).getHoldingRegisters().clone();
	}
	
	synchronized static public void setInputRegister(String slaveAddress, int address, int value){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegister(address, value);
	}
	
	synchronized static public void setInputRegisters(String slaveAddress, int address, int[] values){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegisters(address, values);
	}
	
	synchronized static public void setInputRegisters(String slaveAddress, HashMap<Integer, Integer> inputRegisters){
		createModbusData(slaveAddress);
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegisters(inputRegisters);
	}
	
	@SuppressWarnings("unchecked")
	synchronized static public HashMap<Integer, Integer> getInputRegisters(String slaveAddress){
		createModbusData(slaveAddress);
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
	
	synchronized static public Vector<SenML> convertToSenMLListIIOT() {
		Vector<SenML> smls = new Vector<SenML>();
		for (Map.Entry<String, ModbusData> modbusDataCacheSet : modbusDataCaches.entrySet()) {
			ModbusData modbusDataCache = modbusDataCacheSet.getValue();
			if (!modbusDataCache.getHoldingRegisters().containsKey(231)) {
				return null;
			}
			SenML sml0 = new SenML();
			SenML sml2 = new SenML();
			SenML sml3 = new SenML();
			SenML sml4 = new SenML();
			SenML sml5 = new SenML();
			SenML sml6 = new SenML();
			SenML sml7 = new SenML();
			sml0.setBn("environment");
			sml0.setBt((double) timestamp);
			sml2.setN("temperatue_1");
			sml2.setV((double) modbusDataCache.getHoldingRegisters().get(231));
			sml2.setT(t);
			sml2.setU("degree");
			sml3.setN("temperatue_2");
			sml3.setV((double) modbusDataCache.getHoldingRegisters().get(233));
			sml3.setT(t);
			sml3.setU("degree");
			sml4.setN("ampere_1");
			sml4.setV((double) modbusDataCache.getHoldingRegisters().get(235));
			sml4.setT(t);
			sml4.setU("mA");
			sml5.setN("ampere_2");
			sml5.setV((double) modbusDataCache.getHoldingRegisters().get(237));
			sml5.setT(t);
			sml5.setU("mA");
			sml6.setN("pressure_1");
			sml6.setV((double) modbusDataCache.getHoldingRegisters().get(239));
			sml6.setT(t);
			sml6.setU("Bars");
			sml7.setN("pressure_2");
			sml7.setV((double) modbusDataCache.getHoldingRegisters().get(241));
			sml7.setT(t);
			sml7.setU("Bars");
			smls.add(sml0);
			smls.add(sml2);
			smls.add(sml3);
			smls.add(sml4);
			smls.add(sml5);
			smls.add(sml6);
			smls.add(sml7);
			t = t - 1;
			// tranfer two int to one float
//			int i1 = modbusDataCache.getHoldingRegisters().get(235);
//			int i2 = modbusDataCache.getHoldingRegisters().get(237);
//			byte[] ba = {0, 0, 0, 0};
//			byte[] b1 = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(i1).array();
//			byte[] b2 = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(i2).array();
//			ba[0] = b1[0];
//			ba[1] = b1[1];
//			ba[2] = b2[0];
//			ba[3] = b2[1];
//			float f = ByteBuffer.wrap(ba).order(ByteOrder.BIG_ENDIAN).getFloat();
//			System.out.println(i1 + " " + i2);
//			System.out.println(b1[0] + " " + b1[1] + " " + b2[0] + " " + b2[1]);
//			System.out.println(f);
		}
		
		return smls;
	}
	
	synchronized static public Vector<SenML> convertToSenMLListWagoPLC() {
		Vector<SenML> smls = new Vector<SenML>();
		for (Map.Entry<String, ModbusData> modbusDataCacheSet : modbusDataCaches.entrySet()) {
			ModbusData modbusDataCache = modbusDataCacheSet.getValue();
			if (!modbusDataCache.getHoldingRegisters().containsKey(0)) {
				return null;
			}
			SenML sml0 = new SenML();
			SenML sml1 = new SenML();
			SenML sml2 = new SenML();
			SenML sml3 = new SenML();
			sml0.setBn("producition");
			sml0.setBt((double) timestamp);
			sml1.setN("SerialNumber");
			sml1.setV(modbusDataCache.getHoldingRegisters().containsKey(10) ? (double) modbusDataCache.getHoldingRegisters().get(10) : (double) 0);
			sml1.setT(t);
			sml1.setU("-");
			sml2.setN("productId");
			sml2.setV(modbusDataCache.getHoldingRegisters().containsKey(11) ? (double) modbusDataCache.getHoldingRegisters().get(11) : (double) 0);
			sml2.setT(t);
			sml2.setU("-");
			sml3.setN("width");
			sml3.setV(modbusDataCache.getHoldingRegisters().containsKey(0) ? (double) modbusDataCache.getHoldingRegisters().get(0) : (double) 0);
			sml3.setT(t);
			sml3.setU("mm");
			smls.add(sml0);
			smls.add(sml1);
			smls.add(sml2);
			smls.add(sml3);
			t = t-1;
		}
		System.out.println(Utilities.toJson(smls));
		return smls;
	}
	
	synchronized static public HashMap<String, String> convertModbusDataToCSVRecord(String slaveAddress) {
		HashMap<String, String> record = new HashMap<String, String>();
		final String timeStamp = ZonedDateTime.now().toString();
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
