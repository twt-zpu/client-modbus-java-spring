package eu.arrowhead.client.modbus.cache.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.arrowhead.client.modbus.common.ModbusReadRequestDTO;

public class ModbusReadRequestCacheManagerImpl {
	private final static HashMap<String, List<ModbusReadRequestDTO>> modbusReadRequestCaches = new HashMap<String, List<ModbusReadRequestDTO>>();
	
	public void putReadRequest(String slaveAddress, ModbusReadRequestDTO request){
		if (!modbusReadRequestCaches.containsKey(slaveAddress)){
			modbusReadRequestCaches.put(slaveAddress, new ArrayList<ModbusReadRequestDTO>());
		}
		modbusReadRequestCaches.get(slaveAddress).add(request);
	}
	
	public ModbusReadRequestDTO getFirstReadRequest(String slaveAddress){
		if (!checkFirstReadRequest(slaveAddress)){
			return null;
		}
		return modbusReadRequestCaches.get(slaveAddress).get(0);
	}
	
	public void deleteFirstReadRequest(String slaveAddress){
		if (!checkFirstReadRequest(slaveAddress)){
			return;
		}
		modbusReadRequestCaches.get(slaveAddress).remove(0);
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
