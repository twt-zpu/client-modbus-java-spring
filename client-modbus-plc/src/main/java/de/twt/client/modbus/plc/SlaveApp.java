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
import de.twt.client.modbus.common.cache.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.IModbusReadRequestCacheManager;
import de.twt.client.modbus.common.cache.IModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.cache.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.cache.ModbusReadRequestCacheManagerImpl;
import de.twt.client.modbus.common.cache.ModbusWriteRequestCacheManagerImpl;
import de.twt.client.modbus.common.constants.PackageConstants;
import de.twt.client.modbus.consumer.Consumer;
import de.twt.client.modbus.slave.SlaveTCP;
import de.twt.client.modbus.slave.SlaveTCPConfig;
import de.twt.client.modbus.subscriber.ConfigEventProperites;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;

@SpringBootApplication
@PropertySource("classpath:application.properties")
//@EnableConfigurationProperties(SlaveTCPConfig.class)
@ComponentScan(basePackages = {PackageConstants.BASE_PACKAGE_SLAVE, 
		CommonConstants.BASE_PACKAGE, 
		PackageConstants.BASE_PACKAGE_COMMON, 
		PackageConstants.BASE_PACKAGE_CONSUMER,
		PackageConstants.BASE_PACKAGE_SUBSCRIBER})
public class SlaveApp implements ApplicationRunner {
	
	@Autowired
	private Environment env;
	
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
	
	private final IModbusDataCacheManager dataCache = new ModbusDataCacheManagerImpl();
	private final IModbusReadRequestCacheManager readingRequestsCache = new ModbusReadRequestCacheManagerImpl();
	private final IModbusWriteRequestCacheManager writingRequestsCache = new ModbusWriteRequestCacheManagerImpl();
	
	public static void main(final String[] args) {
		ApplicationContext app = SpringApplication.run(SlaveApp.class, args);
	}

	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.info("run started...");
		// logger.info(configEventProperites.getEventTypeURIMap().get("modbusData"));
		//logger.info(env.getProperty("client_system_name"));
		//SlaveTCP slave = env.getProperty("slave", SlaveTCP.class);
		slave.startSlave();
		master.setModbusMaster();
		//ModbusReadRequestDTO request = new ModbusReadRequestDTO();
		//HashMap<Integer, Integer> coilsAddressMap = new HashMap<Integer, Integer>();
		//coilsAddressMap.put(0, 13);
		//request.setCoilsAddressMap(coilsAddressMap);
		//readingRequestsCache.putReadRequest("127.0.0.1", request);
		// ModbusWriteRequestDTO wRequest = new ModbusWriteRequestDTO();
		// wRequest.setCoil(512, true);
		// writingRequestsCache.putWriteRequest("127.0.0.1", wRequest);
		//boolean[] coils = {true, false};
		//master.writeCoils(512, 2, coils);
		consumer.readDataThread();
		consumer.writeDataThread();
		//master.readData("coils", 0, 13);
	}
}
