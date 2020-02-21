package de.twt.client.modbus.plcProduction;

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

import de.twt.client.modbus.common.cache.ModbusDataCacheManager;
import de.twt.client.modbus.common.constants.PackageConstants;
import de.twt.client.modbus.publisher.EventModbusData;
import de.twt.client.modbus.publisher.EventModule;
import de.twt.client.modbus.publisher.Publisher;
import de.twt.client.modbus.slave.SlaveTCP;
import de.twt.client.modbus.slave.SlaveTCPConfig;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {PackageConstants.BASE_PACKAGE_SLAVE, 
		CommonConstants.BASE_PACKAGE, 
		PackageConstants.BASE_PACKAGE_COMMON, 
		PackageConstants.BASE_PACKAGE_PUBLISHER,
		PackageConstants.BASE_PACKAGE_SUBSCRIBER
		})
public class AppPLCProduction implements ApplicationRunner {
	
	private MasterTest master;
	
	@Autowired
	@Qualifier("slavePLCProductionLine")
	private SlaveTCP slavePLCProductionLine;
	
	@Bean
	public SlaveTCP slavePLCProductionLine(@Qualifier("slavePLCProductionLineConfig") SlaveTCPConfig slaveTCPConfig) {
		return new SlaveTCP(slaveTCPConfig);
	}
	
	@Bean
	@ConfigurationProperties(prefix="slave0")
	public SlaveTCPConfig slavePLCProductionLineConfig() {
		return new SlaveTCPConfig();
	}
	
	@Autowired 
	private Publisher publisher;
	
	@Autowired
	@Qualifier("configModule")
	private EventModule configModule;
	
	
	@Bean
	@ConfigurationProperties(prefix="event.system0")
	public EventModule configModule() {
		return new EventModule();
	}
	
	@Autowired
	@Qualifier("configModbusData")
	private EventModbusData configModbusData;
	
	@Bean
	@ConfigurationProperties(prefix="event.modbusdata")
	public EventModbusData configModbusData() {
		return new EventModbusData();
	}
	
	private final Logger logger = LogManager.getLogger(AppPLCProduction.class);
	
	public static void main(final String[] args) {
		SpringApplication.run(AppPLCProduction.class, args);
	}

	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.info("App started...");
		// slavePLCProductionLine.startSlave();
		// master.setupModbusMaster();
		// boolean[] coils = { true };
		// master.writeCoils(12, 1, coils);
		// logger.info(Utilities.toJson(configModule));
		ModbusDataCacheManager.createModbusData("127.0.0.1");
		ModbusDataCacheManager.setCoil("127.0.0.1", 12, true);
		
		while(true) {
			TimeUnit.MILLISECONDS.sleep(3000);
			publisher.publishOntology(configModule);
			publisher.publishModbusDataOnce(configModbusData);
		}
			
	}
}
