package de.twt.client.modbus.plcDemoProductionWP1;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import de.twt.client.modbus.common.SenML;
import de.twt.client.modbus.common.cache.ModbusDataCacheManager;
import de.twt.client.modbus.common.constants.PackageConstants;
import de.twt.client.modbus.consumer.Consumer;
import de.twt.client.modbus.slave.SlaveTCP;
import de.twt.client.modbus.slave.SlaveTCPConfig;
import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.http.HttpService;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {
		CommonConstants.BASE_PACKAGE, 
		PackageConstants.BASE_PACKAGE_COMMON, 
		PackageConstants.BASE_PACKAGE_PROVIDER,
		PackageConstants.BASE_PACKAGE_CONSUMER,
		PackageConstants.BASE_PACKAGE_SLAVE
		})
public class AppPLCProductionDemo implements ApplicationRunner {
	
	// private MasterTest master;
	
	@Autowired
	@Qualifier("slavePLC")
	private SlaveTCP slavePLC;
	
	@Bean
	public SlaveTCP slavePLC(@Qualifier("slaveConfig") SlaveTCPConfig slaveTCPConfig) {
		return new SlaveTCP(slaveTCPConfig);
	}
	
	@Bean
	@ConfigurationProperties(prefix="slave")
	public SlaveTCPConfig slaveConfig() {
		return new SlaveTCPConfig();
	}

	@Autowired
	private Consumer consumer;
	
	private final Logger logger = LogManager.getLogger(AppPLCProductionDemo.class);
	
	public static void main(final String[] args) {
		SpringApplication.run(AppPLCProductionDemo.class, args);
	}

	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.info("App started...");
		// slavePLC.startSlave();
		
		ModbusDataCacheManager.setDiscreteInput("127.0.0.1", 0, true);
		int registers[] = {10, 11, 30};
		ModbusDataCacheManager.setHoldingRegisters("127.0.0.1", 10, registers);
    		
		
		while(true) {
			TimeUnit.SECONDS.sleep(1);
			
			// System.out.println(Utilities.toJson(ModbusDataCacheManager.getHoldingRegisters("127.0.0.1")));
//			if(ModbusDataCacheManager.getDiscreteInputs("127.0.0.1").containsKey(0)) {
//				if(ModbusDataCacheManager.getDiscreteInputs("127.0.0.1").get(0)){
//					if (ModbusDataCacheManager.getDiscreteInputs("127.0.0.1").containsKey(1)) {
//						ModbusDataCacheManager.setDiscreteInput("127.0.0.1", 0, false);
//						System.out.println("start");
//						/*
//						while(ModbusDataCacheManager.getCoils("127.0.0.1").get(1)) {
//							
//						}
//						*/
//						TimeUnit.SECONDS.sleep(10);
//						System.out.println("end");
//						ModbusDataCacheManager.setDiscreteInput("127.0.0.1", 1, true);
//						consumer.sendDataToOPCUA();
//					}
//				}
//			}
		}
		
	}
}
