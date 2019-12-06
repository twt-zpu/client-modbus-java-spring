package de.twt.client.modbus.slave.app;

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
import de.twt.client.modbus.common.cache.data.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.data.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.cache.request.IModbusReadRequestCacheManager;
import de.twt.client.modbus.common.cache.request.IModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.cache.request.ModbusReadRequestCacheManagerImpl;
import de.twt.client.modbus.common.cache.request.ModbusWriteRequestCacheManagerImpl;
import de.twt.client.modbus.common.constants.ModbusCommenConstants;
import de.twt.client.modbus.consumer.Consumer;
import de.twt.client.modbus.slave.SlaveTCP;
import de.twt.client.modbus.slave.config.SlaveRemoteIOs;
import de.twt.client.modbus.subscriber.ConfigEventProperites;
import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties(SlaveRemoteIOs.class)
@ComponentScan(basePackages = {ModbusCommenConstants.BASE_PACKAGE_SLAVE, 
		CommonConstants.BASE_PACKAGE, 
		ModbusCommenConstants.BASE_PACKAGE_COMMON, 
		ModbusCommenConstants.BASE_PACKAGE_CONSUMER,
		ModbusCommenConstants.BASE_PACKAGE_SUBSCRIBER})
public class SlaveApp implements ApplicationRunner {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private SlaveTCP slave;
	
	@Autowired
	private MasterTest master;
	
	@Autowired
	private Consumer consumer;
	
	@Autowired
	@Qualifier("remotes")
    private SlaveRemoteIOs remotes;
	
	@ConfigurationProperties(prefix="slave")
	@Bean("remotes")
	// @Primary
	public SlaveRemoteIOs getSlaveRemoteIOs() {
        return new SlaveRemoteIOs();
    }
	
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
		logger.info(remotes.getRemoteIOs().get(0).getAddress());
		logger.info(slave.getRange());
		// logger.info(configEventProperites.getEventTypeURIMap().get("modbusData"));
		logger.info(env.getProperty("client_system_name"));
		slave.startSlave();
		master.setModbusMaster();
		ModbusReadRequestDTO request = new ModbusReadRequestDTO();
		HashMap<Integer, Integer> coilsAddressMap = new HashMap<Integer, Integer>();
		coilsAddressMap.put(0, 13);
		request.setCoilsAddressMap(coilsAddressMap);
		readingRequestsCache.putReadRequest("127.0.0.1", request);
		ModbusWriteRequestDTO wRequest = new ModbusWriteRequestDTO();
		wRequest.setCoil(512, true);
		writingRequestsCache.putReadRequest("127.0.0.1", wRequest);
		// boolean[] coils = {true};
		// master.writeCoils(512, 1, coils);
		consumer.readData();
		consumer.writeData();
		logger.info(dataCache.getCoils("127.0.0.1").get(2));
		// consumer.readOneDataFromSlaveAddressDirectly();
	}
}
