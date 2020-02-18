package de.twt.client.modbus.plc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import de.twt.client.modbus.common.constants.PackageConstants;
import de.twt.client.modbus.slave.SlaveTCPConfig;
import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties(SlaveTCPConfig.class)
@ComponentScan(basePackages = { CommonConstants.BASE_PACKAGE, 
		PackageConstants.BASE_PACKAGE_COMMON, 
		PackageConstants.BASE_PACKAGE_SUBSCRIBER})
public class SlaveApp implements ApplicationRunner {
	private final Logger logger = LogManager.getLogger(SlaveApp.class);
	
	public static void main(final String[] args) {
		SpringApplication.run(SlaveApp.class, args);
	}

	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.info("run started...");
	}
}
