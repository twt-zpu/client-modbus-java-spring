package de.twt.client.modbus.remoteIO;

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
import de.twt.client.modbus.master.MasterTCPConfig;
import de.twt.client.modbus.publisher.Publisher;
import de.twt.client.modbus.publisher.EventModbusData;
import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@EnableConfigurationProperties({MasterTCPConfig.class, EventModbusData.class})
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE, 
		PackageConstants.BASE_PACKAGE_COMMON,
		PackageConstants.BASE_PACKAGE_PUBLISHER
		})
@PropertySource("classpath:application.properties")
public class MasterApp implements ApplicationRunner {
	
	@Autowired 
	private Publisher publisher;
	
	@Autowired
	@Qualifier("configModbusData")
	private EventModbusData configModbusData;
	
	@Bean
	@ConfigurationProperties(prefix="event.modbusdata")
	public EventModbusData configModbusData() {
		return new EventModbusData();
	}
	
	private final Logger logger = LogManager.getLogger(MasterApp.class);
	
	public static void main( final String[] args ) {
		SpringApplication.run(MasterApp.class, args);
    }
	
	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.info("start running...");
		ModbusDataCacheManager.createModbusData("127.0.0.1");
		boolean[] diescretInputs = {true, true, true, false, false, false, true, true, true};
		ModbusDataCacheManager.setDiscreteInputs("127.0.0.1", 0, diescretInputs);
		boolean[] coils = {false, true, false, false, false, false, false, true};
		ModbusDataCacheManager.setCoils("127.0.0.1", 512, coils);
		
		TimeUnit.MILLISECONDS.sleep(2000);
		publisher.publishModbusData(configModbusData);
		
	}
}
