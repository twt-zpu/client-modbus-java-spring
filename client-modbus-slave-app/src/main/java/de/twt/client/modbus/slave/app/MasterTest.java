package de.twt.client.modbus.slave.app;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;

import de.twt.client.modbus.common.ModbusWriteRequestDTO;
import de.twt.client.modbus.common.cache.data.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.data.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.cache.request.IModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.cache.request.ModbusWriteRequestCacheManagerImpl;
import de.twt.client.modbus.common.constants.MasterConstants;
import de.twt.client.modbus.master.config.ModbusTCPConfigProperties;
import de.twt.client.modbus.master.config.ModbusTCPConfigProperties.Data.Range;
import de.twt.client.modbus.master.config.ModbusTCPConfigProperties.Data.Read;

@Component
public class MasterTest {
	
	private static final Logger logger = LoggerFactory.getLogger(MasterTest.class);
	
	private ModbusMaster master;
	private String slaveAddress = "127.0.0.1";
	private int slaveId = 1;
	private int port = 505;
	
	public void readData(String type, List<Range> ranges) {
		for (int idx = 0; idx < ranges.size(); idx++) {
			int offset = ranges.get(idx).getStart();
			int quantity = ranges.get(idx).getEnd() - offset + 1;
			try {
				readData(type, offset, quantity);
			} catch (ModbusProtocolException | ModbusNumberException
					| ModbusIOException e) {
				// e.printStackTrace();
				logger.warn("MasterTCP.readData: There is no value at the selected address in slave!");
			}
		}
	}
	
	public void readData(String type, int offset, int quantity) 
			throws ModbusProtocolException, ModbusNumberException, ModbusIOException {
		if (!master.isConnected()){
			master.connect();
		}
		switch(type) {
		case MasterConstants.MODBUS_MASTER_DATA_TYPE_COIL: 
			master.readCoils(slaveId, offset, quantity); break;
		case MasterConstants.MODBUS_MASTER_DATA_TYPE_DISCRETE_INPUT: 
			master.readDiscreteInputs(slaveId, offset, quantity); break;
		case MasterConstants.MODBUS_MASTER_DATA_TYPE_HOLDING_REGISTER:
			master.readHoldingRegisters(slaveId, offset, quantity); break;
		case MasterConstants.MODBUS_MASTER_DATA_TYPE_INPUT_REGISTER: 
			master.readInputRegisters(slaveId, offset, quantity);break;
		default: break;
		}
	}

	public void writeCoils(int address, int quantity, boolean[] coils) 
			throws ModbusProtocolException, ModbusNumberException, ModbusIOException{
		if (!master.isConnected()){
			master.connect();
		}
		boolean[] coilsWrite = new boolean[quantity];
		for (int idx = 0; idx < quantity; idx++){
			coilsWrite[idx] = coils[idx];
		}
		master.writeMultipleCoils(slaveId, address, coilsWrite);
	
	}
	
	public void writeHoldingRegisters(int address, int quantity, int[] registers) 
			throws ModbusProtocolException, ModbusNumberException, ModbusIOException{	
		if (!master.isConnected()){
			master.connect();
		}
		int[] registersWrite = new int[quantity];
		for (int idx = 0; idx < quantity; idx++){
			registersWrite[idx] = registers[idx];
		}
		master.writeMultipleRegisters(slaveId, address, registersWrite);
	}
	
	private TcpParameters setTCPParameters(){
		TcpParameters tcpParameters = new TcpParameters();
		String[] nums = slaveAddress.split("\\.");
		byte[] ip = {0, 0, 0, 0};
		if (nums.length == 4){
			for (int idx = 0; idx < nums.length ; idx++)
				ip[idx] = Byte.valueOf(nums[idx]);
		} else {
			logger.error("MasterTCP: the slave address in properties file is not set correctly!");
		}
		try {
			tcpParameters.setHost(InetAddress.getByAddress(ip));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			logger.error("MasterTCP: the slave address in properties file is not set correctly!");
		}
        tcpParameters.setKeepAlive(true);
        tcpParameters.setPort(port);
        return tcpParameters;
	}
	
	public void setModbusMaster(){
		TcpParameters tcpParameters = setTCPParameters();
		master = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
        Modbus.setAutoIncrementTransactionId(true);
        try {
	        if (!master.isConnected()) {
	        	master.connect();
	        }
	        logger.info("MasterTCP.setModbusMaster: master is connected with slave ({}).", slaveAddress);
        } catch (ModbusIOException e) {
			// TODO Auto-generated catch block
			logger.error("MasterTCP.setModbusMaster: master cannot be connected with slave ({}).", slaveAddress);
			e.printStackTrace();
		}
	}
	
	
}
