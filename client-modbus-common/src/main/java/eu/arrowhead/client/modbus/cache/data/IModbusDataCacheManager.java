package eu.arrowhead.client.modbus.cache.data;

import java.util.HashMap;

import eu.arrowhead.client.modbus.common.ModbusData;

public interface IModbusDataCacheManager {

	public abstract boolean getUpdateStatus(String slaveAddress);
	
	public abstract void setUpdateStatus(String slaveAddress, boolean updateStatus);
	
	public abstract void createModbusData(String slaveAddress);

	public abstract void deleteModbusData(String slaveAddress);

	public abstract void putModbusData(String slaveAddress,
			ModbusData modbusData);

	public abstract void setCoil(String slaveAddress, int address, boolean value);

	public abstract void setCoils(String slaveAddress, int address,
			boolean[] values);

	public abstract void setCoils(String slaveAddress,
			HashMap<Integer, Boolean> coils);

	public abstract HashMap<Integer, Boolean> getCoils(String slaveAddress);

	public abstract void setDiscreteInput(String slaveAddress, int address,
			boolean value);

	public abstract void setDiscreteInputs(String slaveAddress, int address,
			boolean[] values);

	public abstract void setDiscreteInputs(String slaveAddress,
			HashMap<Integer, Boolean> discreteInputs);

	public abstract HashMap<Integer, Boolean> getDiscreteInputs(
			String slaveAddress);

	public abstract void setHoldingRegister(String slaveAddress, int address,
			int value);

	public abstract void setHoldingRegisters(String slaveAddress, int address,
			int[] values);

	public abstract void setHoldingRegisters(String slaveAddress,
			HashMap<Integer, Integer> holdingRegisters);

	public abstract HashMap<Integer, Integer> getHoldingRegisters(
			String slaveAddress);

	public abstract void setInputRegister(String slaveAddress, int address,
			int value);

	public abstract void setInputRegisters(String slaveAddress, int address,
			int[] values);

	public abstract void setInputRegisters(String slaveAddress,
			HashMap<Integer, Integer> inputRegisters);

	public abstract HashMap<Integer, Integer> getInputRegisters(
			String slaveAddress);

}