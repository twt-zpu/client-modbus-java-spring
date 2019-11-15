package eu.arrowhead.client.modbus.cache.request;

import eu.arrowhead.client.modbus.common.ModbusWriteRequestDTO;

public interface IModbusWriteRequestCacheManager {

	public abstract void putReadRequest(String slaveAddress,
			ModbusWriteRequestDTO request);

	public abstract ModbusWriteRequestDTO getFirstReadRequest(
			String slaveAddress);

	public abstract void deleteFirstReadRequest(String slaveAddress);

}