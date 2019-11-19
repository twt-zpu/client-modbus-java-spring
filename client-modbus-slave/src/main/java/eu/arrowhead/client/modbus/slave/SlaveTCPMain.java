package eu.arrowhead.client.modbus.slave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import eu.arrowhead.client.modbus.slave.config.SlaveMultiRemoteIOConfigProperties;

// @SpringBootApplication
@Configuration
@ComponentScan
public class SlaveTCPMain {

	@Autowired
    private Environment env;
	
	public static void main( final String[] args ) {
		// SpringApplication context = new SpringApplication(SlaveMultiRemoteIOConfigProperties.class);
		// context.getAllSources();
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SlaveTCPMain.class);
		// SlaveMultiRemoteIO app = ctx.getBean(SlaveMultiRemoteIO.class);
		SlaveMultiRemoteIOConfigProperties app1 = ctx.getBean(SlaveMultiRemoteIOConfigProperties.class);
		System.out.println(app1.remoteIOs().getRemotes().toString());
		SlaveTCP slave = ctx.getBean(SlaveTCP.class);
		slave.start();
		slave.startSlave();
		ctx.close();
    }
	
	public void run(){
		System.out.println(env.getProperty("remoteIOs"));
	}
}
