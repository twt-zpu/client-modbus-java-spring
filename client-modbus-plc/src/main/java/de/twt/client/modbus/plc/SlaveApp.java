package de.twt.client.modbus.plc;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import de.twt.client.modbus.common.ModbusReadRequestDTO;
import de.twt.client.modbus.common.ModbusWriteRequestDTO;
import de.twt.client.modbus.common.cache.ModbusReadRequestCacheManager;
import de.twt.client.modbus.common.cache.ModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.constants.PackageConstants;
import de.twt.client.modbus.consumer.Consumer;
import de.twt.client.modbus.slave.SlaveTCP;
import de.twt.client.modbus.slave.SlaveTCPConfig;
import de.twt.client.modbus.subscriber.SubscriberEventTypeURI;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {PackageConstants.BASE_PACKAGE_SLAVE, 
		CommonConstants.BASE_PACKAGE, 
		PackageConstants.BASE_PACKAGE_COMMON, 
		PackageConstants.BASE_PACKAGE_CONSUMER,
		PackageConstants.BASE_PACKAGE_SUBSCRIBER})
public class SlaveApp implements ApplicationRunner {
	
	@Autowired
	@Qualifier("slave")
	private SlaveTCP slave;
	
	@Bean
	public SlaveTCP slave(@Qualifier("slaveTCPConfig") SlaveTCPConfig slaveTCPConfig) {
		return new SlaveTCP(slaveTCPConfig);
	}
	
	@Bean
	@ConfigurationProperties(prefix="slave0")
	public SlaveTCPConfig slaveTCPConfig() {
		return new SlaveTCPConfig();
	}
	
	@Autowired
	private MasterTest master;
	
	@Autowired
	private Consumer consumer;
	
	
	private final Logger logger = LogManager.getLogger(SlaveApp.class);
	
	public static void main(final String[] args) {
		ApplicationContext app = SpringApplication.run(SlaveApp.class, args);
	}

	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.info("run started...");
		//slave.startSlave();
		//master.setModbusMaster();
		//consumer.readDataThread();
		//consumer.writeDataThread();
	}
}
