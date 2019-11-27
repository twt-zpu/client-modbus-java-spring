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

import eu.arrowhead.common.CommonConstants;

@Component
@PropertySource("classpath:application.properties")
public class SlaveMultiRemoteIOConfigProperties {
	@Value("${slaveMemoryRange}")
	private int slaveMemoryRange;
	
	public int getSlaveMemoryRange() {
		return slaveMemoryRange;
	}

	public void setSlaveMemoryRange(int slaveMemoryRange) {
		this.slaveMemoryRange = slaveMemoryRange;
	}
	
}
