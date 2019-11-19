package eu.arrowhead.client.modbus.slave.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import eu.arrowhead.client.modbus.slave.config.SlaveMultiRemoteIO.RemoteIOData;
import eu.arrowhead.common.CommonConstants;

@Configuration
@PropertySource("classpath:application.properties")
public class SlaveMultiRemoteIOConfigProperties {
	@Value("${slaveMemoryRange}")
	int slaveMemoryRange;
	@Value("${remoteIOs.addresses}")
	String[] addresses;
	@Value("${remoteIOs.ports}")
	int[] ports;
	@Value("${remoteIOs.offsets}")
	int[] offsets;
	@Value("${remoteIOs.ranges}")
	String[] ranges;
	
	@Bean
	public SlaveMultiRemoteIO remoteIOs(){
		return new SlaveMultiRemoteIO(addresses, ports, offsets, ranges);
	}
	
	public int slaveMemoryRange(){
		return slaveMemoryRange;
	}
	
}



/*
public class SlaveMultiRemoteIOConfigProperty {
	private Map<String, String> remoteIO;
	@Value("${slaveMemoryRange}")
	private String slaveMemoryRange;
	private ArrayList<RemoteIOData> remoteIODatas = new ArrayList<RemoteIOData>();
	private final Logger logger = LogManager.getLogger(SlaveMultiRemoteIOConfigProperties.class);

	public Map<String, String> getRemoteIO() {
		return remoteIO;
	}

	public void setRemoteIO(Map<String, String> remoteIO) {
		this.remoteIO = remoteIO;
		assert checkRemoteIOKeys(remoteIO): "SlaveMultiRemoteIOConfigProperties: the keys in application.properties are not setted correctly.";
		assert checkRemoteIOValues(remoteIO): "SlaveMultiRemoteIOConfigProperties: the values in application.properties are not setted correctly.";
		
	}
	
	public String getSlaveMemoryRange() {
		System.out.println(slaveMemoryRange);
		return slaveMemoryRange;
	}

	public ArrayList<RemoteIOData> getRemoteIODatas() {
		return remoteIODatas;
	}
	
	private boolean checkRemoteIOKeys(Map<String, String> remoteIO){
		if (!remoteIO.containsKey("addresses")){
			logger.error("SlaveMultiRemoteIOConfigProperties: There is no remoteIO.addresses key in application.properties file.");
			return false;
		}
		if (!remoteIO.containsKey("ports")){
			logger.error("SlaveMultiRemoteIOConfigProperties: There is no remoteIO.ports key in application.properties file.");
			return false;
		}
		if (!remoteIO.containsKey("offsets")){
			logger.warn("SlaveMultiRemoteIOConfigProperties: The offset of all remote I/O is setted default as 0.");
		}
		if (!remoteIO.containsKey("originalValueRanges")){
			logger.warn("SlaveMultiRemoteIOConfigProperties: There is no remoteIO.originalValueRanges key in application.properties file. "
					+ "The address will be used directly from the connected master.");
		}
		
		return true;
	}

	private boolean checkRemoteIOValues(Map<String, String> remoteIO){
		String[] addresses = remoteIO.get("addresses").split(";");
		String[] ports = remoteIO.get("ports").split(";");
		if (addresses.length != ports.length){
			logger.error("SlaveMultiRemoteIOConfigProperties: The number of ports in application.properties file is setted incorrectly.");
			return false;
		}
		
		String[] offsets;
		if (remoteIO.containsKey("offsets")){
			offsets = remoteIO.get("offsets").split(";");
		} else {
			offsets = new String[addresses.length];
			Arrays.fill(offsets, "0");
		}
		if (addresses.length != offsets.length){
			logger.error("SlaveMultiRemoteIOConfigProperties: The number of offsets in application.properties file is setted incorrectly.");
			return false;
		}
		
		String[] valueRanges;
		if (remoteIO.containsKey("originalValueRanges")){
			valueRanges = remoteIO.get("originalValueRanges").split(";");
		} else {
			valueRanges = new String[0];
		}
		if (addresses.length != valueRanges.length){
			logger.error("SlaveMultiRemoteIOConfigProperties: The number of originalValueRanges in application.properties file is setted incorrectly.");
			return false;
		}
		
		for (int idx = 0; idx < addresses.length; idx++){
			RemoteIOData remoteIOData = new RemoteIOData();
			remoteIOData.address = addresses[idx];
			remoteIOData.port = Integer.valueOf(ports[idx]);
			remoteIOData.offset = Integer.valueOf(offsets[idx]);
			remoteIODatas.add(remoteIOData);
		}
		
		return true;
	}
	
	
	public class RemoteIOData {
		public String address;
		public int port;
		public int offset;
		public ArrayList<SlaveMemoryRange> ranges = new ArrayList<>(); 
		
		public SlaveMemoryRange getRange(int address) {
			for (SlaveMemoryRange range: ranges){
				if (range.start <= address && address >= range.end){
					return range;
				}
			}
			return null;
		}
	}
	
	public class SlaveMemoryRange {
		public int start;
		public int end;
	}
}
*/
