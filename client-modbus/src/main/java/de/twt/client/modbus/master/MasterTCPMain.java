package de.twt.client.modbus.master;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.twt.client.modbus.master.config.ModbusTCPConfigProperties;
import de.twt.client.modbus.master.config.ModbusTCPConfigProperties.Data;

@Configuration
@ComponentScan
@EnableConfigurationProperties(ModbusTCPConfigProperties.class)
public class MasterTCPMain {
	public static void main( final String[] args ) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MasterTCPMain.class);
		ModbusTCPConfigProperties app = ctx.getBean(ModbusTCPConfigProperties.class);
		MasterTCP master = ctx.getBean(MasterTCP.class);
		//master.setModbusMaster();
		//master.readDataThread();
		System.out.println(app.getData().getRead().getCoils());
		ctx.close();
	}
}
