package de.twt.client.modbus.common.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.twt.client.modbus.common.ModbusWriteRequestDTO;

public class ModbusWriteRequestCacheManagerImpl implements IModbusWriteRequestCacheManager {
	private final static HashMap<String, ModbusWriteRequestDTO> modbusWriteRequestCaches = new HashMap<String, ModbusWriteRequestDTO>();
	private final static HashMap<String, Boolean> modbusWriteRequestImplementedCaches = new HashMap<String, Boolean>();
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager#putWriteRequest(java.lang.String, eu.arrowhead.client.modbus.common.ModbusWriteRequestDTO)
	 */
	@Override
	synchronized public void putWriteRequest(String slaveAddress, ModbusWriteRequestDTO request){
		modbusWriteRequestCaches.put(slaveAddress, request);
		modbusWriteRequestImplementedCaches.put(slaveAddress, Boolean.FALSE);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager#getWriteRequest(java.lang.String)
	 */
	@Override
	synchronized public ModbusWriteRequestDTO getWriteRequest(String slaveAddress){
		return modbusWriteRequestCaches.get(slaveAddress);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager#getWriteRequestToImplement(java.lang.String)
	 */
	@Override
	synchronized public ModbusWriteRequestDTO getWriteRequestToImplement(String slaveAddress){
		modbusWriteRequestImplementedCaches.put(slaveAddress, Boolean.TRUE);
		return getWriteRequest(slaveAddress);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager#isEmpty(java.lang.String)
	 */
	@Override
	synchronized public boolean isImplemented(String slaveAddress){
		if (!modbusWriteRequestImplementedCaches.containsKey(slaveAddress)){
			return true;
		}
		return modbusWriteRequestImplementedCaches.get(slaveAddress).booleanValue();
	}
}
