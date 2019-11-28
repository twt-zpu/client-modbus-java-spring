package de.twt.client.modbus.common.cache.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.twt.client.modbus.common.ModbusWriteRequestDTO;

public class ModbusWriteRequestCacheManagerImpl implements IModbusWriteRequestCacheManager {
	private final static HashMap<String, List<ModbusWriteRequestDTO>> modbusWriteRequestCaches = new HashMap<String, List<ModbusWriteRequestDTO>>();
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager#putReadRequest(java.lang.String, eu.arrowhead.client.modbus.common.ModbusWriteRequestDTO)
	 */
	@Override
	public void putReadRequest(String slaveAddress, ModbusWriteRequestDTO request){
		if (!modbusWriteRequestCaches.containsKey(slaveAddress)){
			modbusWriteRequestCaches.put(slaveAddress, new ArrayList<ModbusWriteRequestDTO>());
		}
		modbusWriteRequestCaches.get(slaveAddress).add(request);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager#getFirstReadRequest(java.lang.String)
	 */
	@Override
	public ModbusWriteRequestDTO getFirstReadRequest(String slaveAddress){
		if (!checkFirstReadRequest(slaveAddress)){
			return null;
		}
		return modbusWriteRequestCaches.get(slaveAddress).get(0);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager#deleteFirstReadRequest(java.lang.String)
	 */
	@Override
	public void deleteFirstReadRequest(String slaveAddress){
		if (!checkFirstReadRequest(slaveAddress)){
			return;
		}
		modbusWriteRequestCaches.get(slaveAddress).remove(0);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager#isEmpty(java.lang.String)
	 */
	@Override
	public boolean isEmpty(String slaveAddress){
		return !checkFirstReadRequest(slaveAddress);
	}
	
	private boolean checkFirstReadRequest(String slaveAddress){
		if (!modbusWriteRequestCaches.containsKey(slaveAddress)){
			return false;
		}
		if (modbusWriteRequestCaches.get(slaveAddress).isEmpty()){
			return false;
		}
		return true;
	}
}
