package eu.arrowhead.client.modbus.cache.request;

import eu.arrowhead.client.modbus.common.ModbusReadRequestDTO;

public interface IModbusReadRequestCacheManager {

	public abstract void putReadRequestToTop(String slaveAddress,
			ModbusReadRequestDTO request);
	
	public abstract void putReadRequest(String slaveAddress,
			ModbusReadRequestDTO request);

	public abstract ModbusReadRequestDTO getFirstReadRequest(String slaveAddress);

	public abstract void deleteFirstReadRequest(String slaveAddress);

	public abstract boolean isEmpty(String slaveAddress);

}