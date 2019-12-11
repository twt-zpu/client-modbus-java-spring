package de.twt.client.modbus.common.cache.request;

import de.twt.client.modbus.common.ModbusReadRequestDTO;

public interface IModbusReadRequestCacheManager {
	
	public abstract void putReadRequest(String slaveAddress,
			ModbusReadRequestDTO request);

	public abstract ModbusReadRequestDTO getFirstReadRequest(String slaveAddress);

	public abstract void deleteFirstReadRequest(String slaveAddress);
	
	public abstract void deleteReadRequest(String slaveAddress, String id); 

	public abstract boolean isEmpty(String slaveAddress);

	public abstract boolean isIDExist(String slaveAddress, String id);
}