package de.twt.client.modbus.common.cache.request;

import de.twt.client.modbus.common.ModbusWriteRequestDTO;

public interface IModbusWriteRequestCacheManager {

	public abstract void putReadRequest(String slaveAddress,
			ModbusWriteRequestDTO request);

	public abstract ModbusWriteRequestDTO getFirstReadRequest(
			String slaveAddress);

	public abstract void deleteFirstReadRequest(String slaveAddress);
	
	public abstract boolean isEmpty(String slaveAddress);

}