package de.twt.client.modbus.common.cache;

import java.util.ArrayList;
import java.util.List;

import de.twt.client.modbus.common.ModbusSystem;

public class ModbusSystemCacheManager {
	private static ModbusSystem modbusSystem;
	private enum HeadTail {head, tail};

	synchronized public static ModbusSystem getModbusSystem() {
		return modbusSystem;
	}
	
	synchronized public static void setModbusSystem(ModbusSystem modbusSystem) {
		ModbusSystemCacheManager.modbusSystem = modbusSystem;
	}
	
	synchronized public static List<ModbusSystem.Component> getTailComponents() {
		return getHeadTailComponents(HeadTail.tail);
	}
	
	
	synchronized public static List<ModbusSystem.Component> getHeadComponents() {
		return getHeadTailComponents(HeadTail.head);
	}
	
	
	private static List<ModbusSystem.Component> getHeadTailComponents(HeadTail type) {
		List<ModbusSystem.Component> headTails = new ArrayList<>();
		
		List<ModbusSystem.Component> components = modbusSystem.getComponents();
		
		ArrayList<String> headTailsName = new ArrayList<>();
		ArrayList<String> componentsName = new ArrayList<>();
		for (ModbusSystem.Component component : components) {
			switch (type) {
			case head: headTailsName.add(component.getPreComponentName()); break;
			case tail: headTailsName.add(component.getNextComponentName()); break;
			}
			componentsName.add(component.getName());
		}
		
		for (String componentName : componentsName) {
			headTailsName.removeIf(name -> (componentName.equalsIgnoreCase(name)));
		}
		
		for (ModbusSystem.Component component : components) {
			String name = "";
			switch (type) {
			case head: name = component.getPreComponentName(); break;
			case tail: name = component.getNextComponentName(); break;
			}
			if (headTailsName.contains(name)) {
				headTails.add(component);
			}
		}
		
		return headTails;
	}
}
