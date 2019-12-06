package de.twt.client.modbus.master.app;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import de.twt.client.modbus.common.cache.data.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.data.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.constants.ModbusCommenConstants;
import de.twt.client.modbus.master.MasterTCP;
import de.twt.client.modbus.master.config.ModbusTCPConfigProperties;
import de.twt.client.modbus.publisher.Publisher;
import de.twt.client.modbus.publisher.event.PublisherConfig;
import de.twt.client.modbus.slave.SlaveTCP;
import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@EnableConfigurationProperties(ModbusTCPConfigProperties.class)
@ComponentScan(basePackages = {ModbusCommenConstants.BASE_PACKAGE_MASTER, 
		CommonConstants.BASE_PACKAGE, 
		ModbusCommenConstants.BASE_PACKAGE_COMMON, 
		ModbusCommenConstants.BASE_PACKAGE_PROVIDER,
		//ModbusCommenConstants.BASE_PACKAGE_PUBLISHER
		})
@PropertySource("classpath:application.properties")
public class MasterApp implements ApplicationRunner {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private SlaveTest slave;
	
	@Autowired
	private MasterTCP master;
	
	@Autowired
	private ModbusTCPConfigProperties conf;
	
	//@Autowired 
	//private PublisherConfig config;
	
	//@Autowired 
	//private Publisher publisher;
	
	private final Logger logger = LogManager.getLogger(MasterApp.class);
	
	private final IModbusDataCacheManager dataCache = new ModbusDataCacheManagerImpl();
	
	public static void main( final String[] args ) {
		ApplicationContext app = SpringApplication.run(MasterApp.class, args);
    }
	
	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.debug("start running...");
		dataCache.createModbusData("127.0.0.1");
		slave.setData();
		slave.startSlave();
		master.setupModbusMaster();
		// master.readDataThreadForEvent();
		master.writeDataThread();
		//publisher.publish();
	}
}
