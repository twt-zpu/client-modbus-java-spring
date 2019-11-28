package de.twt.client.modbus.master.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Primary
@Component
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix="master", ignoreUnknownFields=true, ignoreInvalidFields=true)
public class ModbusTCPConfigProperties {
	private int periodTime;
	private Slave slave;
	private Data data;
	
	public static class Slave {
		private String address;
		private int port;
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		
	}
	
	public static class Data {
		private Read read;
		private Write write;
		
		public static class Read {
			private List<Range> coils = new ArrayList<>();
			private List<Range> discreteInputs = new ArrayList<>();
			private List<Range> holdingRegisters = new ArrayList<>();
			private List<Range> inputRegisters = new ArrayList<>();
			public List<Range> getCoils() {
				return coils;
			}
			public void setCoils(List<Range> coils) {
				this.coils = coils;
			}
			public List<Range> getDiscreteInputs() {
				return discreteInputs;
			}
			public void setDiscreteInputs(List<Range> discreteInputs) {
				this.discreteInputs = discreteInputs;
			}
			public List<Range> getHoldingRegisters() {
				return holdingRegisters;
			}
			public void setHoldingRegisters(List<Range> holdingRegisters) {
				this.holdingRegisters = holdingRegisters;
			}
			public List<Range> getInputRegisters() {
				return inputRegisters;
			}
			public void setInputRegisters(List<Range> inputRegisters) {
				this.inputRegisters = inputRegisters;
			}
			
		}
		
		public static class Write {
			private List<Range> coils  = new ArrayList<>();
			private List<Range> holdingRegisters = new ArrayList<>();
			public List<Range> getCoils() {
				return coils;
			}
			public void setCoils(List<Range> coils) {
				this.coils = coils;
			}
			public List<Range> getHoldingRegisters() {
				return holdingRegisters;
			}
			public void setHoldingRegisters(List<Range> holdingRegisters) {
				this.holdingRegisters = holdingRegisters;
			}
			
		}
		
		public static class Range {
			private int start;
			private int end;
			public int getStart() {
				return start;
			}
			public void setStart(int start) {
				this.start = start;
			}
			public int getEnd() {
				return end;
			}
			public void setEnd(int end) {
				this.end = end;
			}
			
		}

		public Read getRead() {
			return read;
		}

		public void setRead(Read read) {
			this.read = read;
		}

		public Write getWrite() {
			return write;
		}

		public void setWrite(Write write) {
			this.write = write;
		}
		
	}

	
	public int getPeriodTime() {
		return periodTime;
	}

	public void setPeriodTime(int periodTime) {
		this.periodTime = periodTime;
	}

	public Slave getSlave() {
		return slave;
	}

	public void setSlave(Slave slave) {
		this.slave = slave;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	

	/*
	@Value("${periodTime}")
	private int periodTime;
	
	@Value("${slave.address}")
	private String slaveAddress;
	
	@Value("${slave.port}")
	private int slavePort;
	
	@Autowired
	private Data data;
	
	@ConfigurationProperties(prefix="master")
	public class Data {
		private Map<String, String> read;
		private Map<String, String> write;

		public Map<String, String> getWrite() {
			return write;
		}

		public void setWrite(Map<String, String> write) {
			this.write = write;
		}

		public Map<String, String> getRead() {
			return read;
		}

		public void setRead(Map<String, String> read) {
			this.read = read;
		}
	}
	
	public String getSlaveAddress() {
		return slaveAddress;
	}

	public void setSlaveAddress(String slaveAddress) {
		this.slaveAddress = slaveAddress;
	}

	public int getSlavePort() {
		return slavePort;
	}

	public void setSlavePort(int slavePort) {
		this.slavePort = slavePort;
	}
	
	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public int getPeriodTime() {
		return periodTime;
	}

	public void setPeriodTime(int periodTime) {
		this.periodTime = periodTime;
	}*/

}
