package eu.arrowhead.client.modbus.master.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class ModbusTCPConfigProperties {
	@ConfigurationProperties(prefix="data")
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
	
	@Value("${periodTime}")
	private int periodTime;
	
	@Value("${slave.address}")
	private String slaveAddress;
	
	@Value("${slave.port}")
	private int slavePort;
	
	@Autowired
	private Data data;
	
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
	}

}
