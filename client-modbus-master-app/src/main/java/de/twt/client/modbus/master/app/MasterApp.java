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

import de.twt.client.modbus.common.constants.ModbusCommenConstants;
import de.twt.client.modbus.master.config.ModbusTCPConfigProperties;
import de.twt.client.modbus.publisher.event.PublisherConfig;
import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@EnableConfigurationProperties(ModbusTCPConfigProperties.class)
@ComponentScan(basePackages = {ModbusCommenConstants.BASE_PACKAGE_MASTER, 
		CommonConstants.BASE_PACKAGE, 
		ModbusCommenConstants.BASE_PACKAGE_COMMON, 
		ModbusCommenConstants.BASE_PACKAGE_PUBLISHER})
@PropertySource("classpath:application.properties")
public class MasterApp implements ApplicationRunner {
	@Autowired
	private ModbusTCPConfigProperties conf;
	
	@Autowired 
	private PublisherConfig config;
	
	private final Logger logger = LogManager.getLogger(MasterApp.class);
	
	public static void main( final String[] args ) {
		ApplicationContext app = SpringApplication.run(MasterApp.class, args);
		/*for(String name: app.getBeanDefinitionNames())
			System.out.println(name + "\n");*/
    }
	
	@Override
	public void run(final ApplicationArguments args) throws Exception {
		logger.debug("start running...");
		logger.info(conf.getPeriodTime());
		logger.warn(config.toString());
		logger.warn(config.getSlaves().get(0).getData().get(0).getStartAddress());
	}
}
