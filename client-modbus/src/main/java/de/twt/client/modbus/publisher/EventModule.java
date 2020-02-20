package de.twt.client.modbus.publisher;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix="event.system")
public class EventModule {
	private List<Component> Components;
	
	public static class Component {
		private String name;
		private String preComponentName;
		private String nextComponentName;
		private List<Service> service;
		
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
