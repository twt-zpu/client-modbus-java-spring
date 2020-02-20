package de.twt.client.modbus.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.twt.client.modbus.common.constants.ModbusConstants;


@ConfigurationProperties(prefix="event.system")
public class EventModule {
	private List<Component> Components = new ArrayList<>();;
	
	public static class Component {
		private String name;
		private String preComponentName;
		private String nextComponentName;
		private DataInterface input;
		private DataInterface output;
		private List<Service> service = new ArrayList<>();;
		
		public static class Service {
			private String name;
			private Map<String, String> properties;
			
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public Map<String, String> getProperties() {
				return properties;
			}
			public void setProperties(Map<String, String> properties) {
				this.properties = properties;
			}
		}
		
		public static class DataInterface {
			private String slaveAddress;
			private ModbusConstants.MODBUS_DATA_TYPE type;
			private int address;
			
			public String getSlaveAddress() {
				return slaveAddress;
			}
			public void setSlaveAddress(String slaveAddress) {
				this.slaveAddress = slaveAddress;
			}
			public ModbusConstants.MODBUS_DATA_TYPE getType() {
				return type;
			}
			public void setType(ModbusConstants.MODBUS_DATA_TYPE type) {
				this.type = type;
			}
			public int getAddress() {
				return address;
			}
			public void setAddress(int address) {
				this.address = address;
			}
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPreComponentName() {
			return preComponentName;
		}
		public void setPreComponentName(String preComponentName) {
			this.preComponentName = preComponentName;
		}
		public String getNextComponentName() {
			return nextComponentName;
		}
		public void setNextComponentName(String nextComponentName) {
			this.nextComponentName = nextComponentName;
		}
		public DataInterface getInput() {
			return input;
		}
		public void setInput(DataInterface input) {
			this.input = input;
		}
		public DataInterface getOutput() {
			return output;
		}
		public void setOutput(DataInterface output) {
			this.output = output;
		}
		public List<Service> getService() {
			return service;
		}
		public void setService(List<Service> service) {
			this.service = service;
		}
	}

	public List<Component> getComponents() {
		return Components;
	}
	public void setComponents(List<Component> components) {
		Components = components;
	}
}
