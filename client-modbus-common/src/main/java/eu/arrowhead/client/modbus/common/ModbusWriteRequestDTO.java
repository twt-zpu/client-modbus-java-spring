package eu.arrowhead.client.modbus.common;

import java.util.HashMap;

public class ModbusWriteRequestDTO {
	private final HashMap<Integer, Boolean> coils = new HashMap<Integer, Boolean>();
	private final HashMap<Integer, Integer> holdingRegisters = new HashMap<Integer, Integer>();
	
	public void setCoil(int address, boolean value){
		coils.put(address, value);
	}
	
	public void setCoils(int address, boolean[] values){
		int index = address;
		for(boolean value : values)
			coils.put(index++, value);
	}
	
	public void setCoils(HashMap<Integer, Boolean> coils){
		this.coils.putAll(coils);
	}
	
	public HashMap<Integer, Boolean> getCoils(){
		return coils;
	}
	
	public void setHoldingRegister(int address, int value){
		holdingRegisters.put(address, value);
	}
	
	public void setHoldingRegisters(int address, int[] values){
		int index = address;
		for(int value : values)
			holdingRegisters.put(index++, value);
	}
	
	public void setHoldingRegisters(HashMap<Integer, Integer> holdingRegisters){
		this.holdingRegisters.putAll(holdingRegisters);
	}
	
	public HashMap<Integer, Integer> getHoldingRegisters(){
		return holdingRegisters;
	}
	
}
