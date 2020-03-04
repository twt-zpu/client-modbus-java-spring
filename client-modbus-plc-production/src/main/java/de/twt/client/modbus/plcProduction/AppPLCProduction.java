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

import de.twt.client.modbus.common.ModbusSystem;
import de.twt.client.modbus.common.cache.ModbusDataCacheManager;
import de.twt.client.modbus.common.constants.PackageConstants;
import de.twt.client.modbus.dataWriter.ModbusDataRecordContent;
import de.twt.client.modbus.dataWriter.ModbusDataWriter;
import de.twt.client.modbus.publisher.EventModbusData;
import de.twt.client.modbus.publisher.Publisher;
import de.twt.client.modbus.slave.SlaveTCP;
import de.twt.client.modbus.slave.SlaveTCPConfig;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;

@SpringBootApplication
@PropertySource("classpath:application.properties")
// @EnableConfigurationProperties(ModbusDataRecordContent.class)
@ComponentScan(basePackages = {PackageConstants.BASE_PACKAGE_SLAVE, 
		CommonConstants.BASE_PACKAGE, 
		PackageConstants.BASE_PACKAGE_COMMON, 
		PackageConstants.BASE_PACKAGE_PUBLISHER,
		PackageConstants.BASE_PACKAGE_SUBSCRIBER,
		PackageConstants.BASE_PACKAGE_DATAWRITER
		})
public class AppPLCProduction implements ApplicationRunner {
	
	private MasterTest master;
	
	@Autowired
	private ModbusDataWriter modbusDataWriter;
	
	@Autowired
	private ModbusDataRecordContent modbusDataRecordContent;
	
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
	private ModbusSystem modbusSystem;
	
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
		logger.info(modbusSystem.getName());
		// slavePLCProductionLine.startSlave();
		// master.setupModbusMaster();
		// boolean[] coils = { true };
		// master.writeCoils(12, 1, coils);
		// logger.info(Utilities.toJson(configModule));
		ModbusDataCacheManager.createModbusData("127.0.0.1");
		ModbusDataCacheManager.setCoil("127.0.0.1", 12, true);
		ModbusDataCacheManager.setCoil("127.0.0.1", 11, true);
		ModbusDataCacheManager.setDiscreteInput("127.0.0.1", 11, true);
		ModbusDataCacheManager.setInputRegister("127.0.0.1", 11, 111);
		
		logger.info(Utilities.toJson(modbusDataRecordContent.getFileName()));
		modbusDataWriter.startRecord();
		
		// logger.info(Utilities.toJson(ModbusDataCacheManager.convertToSenMLList()));
		//ModbusSystemCacheManager.setModbusSystem(modbusSystem);
		
			TimeUnit.MILLISECONDS.sleep(3000);
			// publisher.publishOntology();
			// publisher.publishModbusDataOnce(configModbusData);
		
	}
}
