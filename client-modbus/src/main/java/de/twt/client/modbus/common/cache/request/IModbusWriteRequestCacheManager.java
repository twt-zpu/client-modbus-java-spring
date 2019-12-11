package de.twt.client.modbus.common.cache.request;

import de.twt.client.modbus.common.ModbusWriteRequestDTO;

public interface IModbusWriteRequestCacheManager {

	public abstract void putWriteRequest(String slaveAddress,
			ModbusWriteRequestDTO request);

	public abstract ModbusWriteRequestDTO getWriteRequest(
			String slaveAddress);
	
	public abstract ModbusWriteRequestDTO getWriteRequestToImplement(
			String slaveAddress);
	
	public abstract boolean isImplemented(String slaveAddress);

}