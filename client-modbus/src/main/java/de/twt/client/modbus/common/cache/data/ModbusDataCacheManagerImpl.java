package de.twt.client.modbus.common.cache.data;

import java.util.HashMap;

import de.twt.client.modbus.common.ModbusData;

public class ModbusDataCacheManagerImpl implements IModbusDataCacheManager {
	private final static HashMap<String, ModbusData> modbusDataCaches = new HashMap<String, ModbusData>();
	private final static HashMap<String, Boolean> updateStatus = new HashMap<String, Boolean>();
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#getUpdateStatus(java.lang.String)
	 */
	@Override
	public boolean getUpdateStatus(String slaveAddress) {
		return updateStatus.get(slaveAddress);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setUpdateStatus(java.lang.String)
	 */
	@Override
	public void setUpdateStatus(String slaveAddress, boolean updateStatus) {
		ModbusDataCacheManagerImpl.updateStatus.put(slaveAddress, updateStatus);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#createModbusData(java.lang.String)
	 */
	@Override
	synchronized public void createModbusData(String slaveAddress){
		modbusDataCaches.put(slaveAddress, new ModbusData());
	}
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#deleteModbusData(java.lang.String)
	 */
	@Override
	synchronized public void deleteModbusData(String slaveAddress){
		modbusDataCaches.remove(slaveAddress);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#putModbusData(java.lang.String, eu.arrowhead.client.modbus.common.ModbusData)
	 */
	@Override
	synchronized public void putModbusData(String slaveAddress, ModbusData modbusData){
		modbusDataCaches.put(slaveAddress, modbusData);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setCoil(java.lang.String, int, boolean)
	 */
	@Override
	synchronized public void setCoil(String slaveAddress, int address, boolean value){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoil(address, value);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setCoils(java.lang.String, int, boolean[])
	 */
	@Override
	synchronized public void setCoils(String slaveAddress, int address, boolean[] values){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoils(address, values);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setCoils(java.lang.String, java.util.HashMap)
	 */
	@Override
	synchronized public void setCoils(String slaveAddress, HashMap<Integer, Boolean> coils){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setCoils(coils);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#getCoils(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public HashMap<Integer, Boolean> getCoils(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Boolean>) modbusDataCaches.get(slaveAddress).getCoils().clone();
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setDiscreteInput(java.lang.String, int, boolean)
	 */
	@Override
	synchronized public void setDiscreteInput(String slaveAddress, int address, boolean value){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInput(address, value);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setDiscreteInputs(java.lang.String, int, boolean[])
	 */
	@Override
	synchronized public void setDiscreteInputs(String slaveAddress, int address, boolean[] values){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInputs(address, values);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setDiscreteInputs(java.lang.String, java.util.HashMap)
	 */
	@Override
	synchronized public void setDiscreteInputs(String slaveAddress, HashMap<Integer, Boolean> discreteInputs){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setDiscreteInputs(discreteInputs);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#getDiscreteInputs(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public HashMap<Integer, Boolean> getDiscreteInputs(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Boolean>) modbusDataCaches.get(slaveAddress).getDiscreteInputs().clone();
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setHoldingRegister(java.lang.String, int, int)
	 */
	@Override
	synchronized public void setHoldingRegister(String slaveAddress, int address, int value){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegister(address, value);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setHoldingRegisters(java.lang.String, int, int[])
	 */
	@Override
	synchronized public void setHoldingRegisters(String slaveAddress, int address, int[] values){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegisters(address, values);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setHoldingRegisters(java.lang.String, java.util.HashMap)
	 */
	@Override
	synchronized public void setHoldingRegisters(String slaveAddress, HashMap<Integer, Integer> holdingRegisters){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setHoldingRegisters(holdingRegisters);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#getHoldingRegisters(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public HashMap<Integer, Integer> getHoldingRegisters(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Integer>) modbusDataCaches.get(slaveAddress).getHoldingRegisters().clone();
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setInputRegister(java.lang.String, int, int)
	 */
	@Override
	synchronized public void setInputRegister(String slaveAddress, int address, int value){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegister(address, value);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setInputRegisters(java.lang.String, int, int[])
	 */
	@Override
	synchronized public void setInputRegisters(String slaveAddress, int address, int[] values){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegisters(address, values);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#setInputRegisters(java.lang.String, java.util.HashMap)
	 */
	@Override
	public void setInputRegisters(String slaveAddress, HashMap<Integer, Integer> inputRegisters){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		setUpdateStatus(slaveAddress, true);
		modbusDataCaches.get(slaveAddress).setInputRegisters(inputRegisters);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.data.IModbusDataCacheManager#getInputRegisters(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public HashMap<Integer, Integer> getInputRegisters(String slaveAddress){
		assert modbusDataCaches.containsKey(slaveAddress): "ModbusDataCacheManagerImpl: There is no cache with slave address (" + slaveAddress + ").";
		return (HashMap<Integer, Integer>) modbusDataCaches.get(slaveAddress).getInputRegisters().clone();
	}
}
