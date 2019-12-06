package de.twt.client.modbus.common.cache.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.twt.client.modbus.common.ModbusReadRequestDTO;

public class ModbusReadRequestCacheManagerImpl implements IModbusReadRequestCacheManager {
	private final static HashMap<String, List<ModbusReadRequestDTO>> modbusReadRequestCaches = new HashMap<String, List<ModbusReadRequestDTO>>();
	
	@Override
	synchronized public void putReadRequest(String slaveAddress, ModbusReadRequestDTO request){
		if (!modbusReadRequestCaches.containsKey(slaveAddress)){
			modbusReadRequestCaches.put(slaveAddress, new ArrayList<ModbusReadRequestDTO>());
		}
		modbusReadRequestCaches.get(slaveAddress).add(request);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusReadRequestCacheManager#getFirstReadRequest(java.lang.String)
	 */
	@Override
	synchronized public ModbusReadRequestDTO getFirstReadRequest(String slaveAddress){
		if (!checkFirstReadRequest(slaveAddress)){
			return null;
		}
		return modbusReadRequestCaches.get(slaveAddress).get(0);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusReadRequestCacheManager#deleteFirstReadRequest(java.lang.String)
	 */
	@Override
	synchronized public void deleteFirstReadRequest(String slaveAddress){
		if (!checkFirstReadRequest(slaveAddress)){
			return;
		}
		modbusReadRequestCaches.get(slaveAddress).remove(0);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusReadRequestCacheManager#isEmpty(java.lang.String)
	 */
	@Override
	synchronized public boolean isEmpty(String slaveAddress){
		return !checkFirstReadRequest(slaveAddress);
	}
	
	private boolean checkFirstReadRequest(String slaveAddress){
		if (!modbusReadRequestCaches.containsKey(slaveAddress)){
			return false;
		}
		if (modbusReadRequestCaches.get(slaveAddress).isEmpty()){
			return false;
		}
		return true;
	}
}
