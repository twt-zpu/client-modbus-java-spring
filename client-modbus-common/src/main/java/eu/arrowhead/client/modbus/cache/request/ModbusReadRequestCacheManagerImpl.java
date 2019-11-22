package eu.arrowhead.client.modbus.cache.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.arrowhead.client.modbus.common.ModbusReadRequestDTO;

public class ModbusReadRequestCacheManagerImpl implements IModbusReadRequestCacheManager {
	private final static HashMap<String, List<ModbusReadRequestDTO>> modbusReadRequestCaches = new HashMap<String, List<ModbusReadRequestDTO>>();
	private final static HashMap<String, Integer> operatingPositions = new HashMap<String, Integer>();
	
	@Override
	synchronized public void putReadRequest(String slaveAddress, ModbusReadRequestDTO request){
		if (!modbusReadRequestCaches.containsKey(slaveAddress)){
			modbusReadRequestCaches.put(slaveAddress, new ArrayList<ModbusReadRequestDTO>());
		}
		modbusReadRequestCaches.get(slaveAddress).add(request);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusReadRequestCacheManager#putReadRequestToTop(java.lang.String, eu.arrowhead.client.modbus.common.ModbusReadRequestDTO)
	 */
	@Override
	synchronized public void putReadRequestToTop(String slaveAddress, ModbusReadRequestDTO request){
		if (!modbusReadRequestCaches.containsKey(slaveAddress)){
			modbusReadRequestCaches.put(slaveAddress, new ArrayList<ModbusReadRequestDTO>());
		}
		int pos = operatingPositions.get(slaveAddress) + 1;
		operatingPositions.put(slaveAddress, pos);
		modbusReadRequestCaches.get(slaveAddress).add(0, request);
	}
	
	/* (non-Javadoc)
	 * @see eu.arrowhead.client.modbus.cache.request.IModbusReadRequestCacheManager#getFirstReadRequest(java.lang.String)
	 */
	@Override
	synchronized public ModbusReadRequestDTO getFirstReadRequest(String slaveAddress){
		if (!checkFirstReadRequest(slaveAddress)){
			return null;
		}
		operatingPositions.put(slaveAddress, 0);
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
		modbusReadRequestCaches.get(slaveAddress).remove(operatingPositions.get(slaveAddress));
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
