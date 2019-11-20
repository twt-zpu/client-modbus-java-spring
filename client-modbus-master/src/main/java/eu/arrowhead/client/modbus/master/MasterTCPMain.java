package eu.arrowhead.client.modbus.master;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.client.modbus.master.config.ModbusTCPConfigProperties;
import eu.arrowhead.client.modbus.master.config.ModbusTCPConfigProperties.Data;

@Configuration
@ComponentScan
@EnableConfigurationProperties(Data.class)
public class MasterTCPMain {
	public static void main( final String[] args ) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MasterTCPMain.class);
		ModbusTCPConfigProperties app = ctx.getBean(ModbusTCPConfigProperties.class);
		MasterTCP master = ctx.getBean(MasterTCP.class);
		master.setModbusMaster();
		master.readDataThread();
		System.out.println(app.getData().getRead().keySet());
		ctx.close();
	}
}
