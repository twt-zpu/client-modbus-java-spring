package de.twt.client.modbus.master;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

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
public class MasterTCP {
	@Autowired
	private ModbusTCPConfigProperties config;
	
	private static final Logger logger = LoggerFactory.getLogger(MasterTCP.class);
	private IModbusWriteRequestCacheManager writngRequestCache = new ModbusWriteRequestCacheManagerImpl();
	private IModbusDataCacheManager dataCache = new ModbusDataCacheManagerImpl();
	private ModbusMaster master;
	private String slaveAddress;
	private int slaveId = 1;
	private boolean stopReadingData = false;
	private boolean stopWritingData = false;
	
	public void readDataThreadForEvent() {
		logger.debug("start reading data thread...");
		new Thread() {
			public void run() {
				while(!stopReadingData){
					long startTime=System.currentTimeMillis(); 
					readData();
					long endTime=System.currentTimeMillis(); 
					long intervalTime = endTime - startTime;
					if (intervalTime > config.getPeriodTime()) {
						logger.warn("MasterTCP.readDataThread: the running time of one period is longer than the setting period time.");
					} else {
						try {
							TimeUnit.MILLISECONDS.sleep(config.getPeriodTime() - intervalTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}
	
	public void readData() {
		logger.debug("start reading data...");
		Read dataToRead = config.getData().getRead();
		readData(MasterConstants.MODBUS_MASTER_DATA_TYPE_COIL, dataToRead.getCoils());
		readData(MasterConstants.MODBUS_MASTER_DATA_TYPE_DISCRETE_INPUT, dataToRead.getDiscreteInputs());
		readData(MasterConstants.MODBUS_MASTER_DATA_TYPE_HOLDING_REGISTER, dataToRead.getHoldingRegisters());
		readData(MasterConstants.MODBUS_MASTER_DATA_TYPE_INPUT_REGISTER, dataToRead.getInputRegisters());
	}
	
	private void readData(String type, List<Range> ranges) {
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
	
	private void readData(String type, int offset, int quantity) 
			throws ModbusProtocolException, ModbusNumberException, ModbusIOException {
		if (!master.isConnected()){
			master.connect();
		}
		switch(type) {
		case MasterConstants.MODBUS_MASTER_DATA_TYPE_COIL: 
			dataCache.setCoils(slaveAddress, offset, master.readCoils(slaveId, offset, quantity)); break;
		case MasterConstants.MODBUS_MASTER_DATA_TYPE_DISCRETE_INPUT: 
			dataCache.setDiscreteInputs(slaveAddress, offset, master.readDiscreteInputs(slaveId, offset, quantity)); break;
		case MasterConstants.MODBUS_MASTER_DATA_TYPE_HOLDING_REGISTER:
			dataCache.setHoldingRegisters(slaveAddress, offset, master.readHoldingRegisters(slaveId, offset, quantity)); break;
		case MasterConstants.MODBUS_MASTER_DATA_TYPE_INPUT_REGISTER: 
			dataCache.setInputRegisters(slaveAddress, offset, master.readInputRegisters(slaveId, offset, quantity));break;
		default: break;
		}
	}
	
	// TODO: wait time definition, delete first request (uncomment)
	public void writeDataThread() {
		logger.debug("start writing data thread...");
		new Thread() {
			public void run() {
				while(!stopWritingData){
					long startTime=System.currentTimeMillis(); 
					ModbusWriteRequestDTO request = writngRequestCache.getFirstReadRequest(slaveAddress);
					if (request == null) {
						continue;
					}
					writeFirstRequestData(request);
					// writngRequestCache.deleteFirstReadRequest(slaveAddress);
					long endTime=System.currentTimeMillis(); 
					long intervalTime = endTime - startTime;
					if (intervalTime > config.getPeriodTime()) {
						logger.warn("MasterTCP.writeDataThread: the running time of one period is longer than the setting period time.");
					}
					try {
						TimeUnit.MILLISECONDS.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public void writeFirstRequestData(ModbusWriteRequestDTO request){
		int address = request.getAddress();
		int quantity = request.getQuantity();
		boolean[] coils = request.getCoils();
		int[] registers = request.getHoldingRegisters();
		
		try {
			if (coils.length != 0) {
				writeCoils(address, quantity, coils);
			}
			if (registers.length != 0) {
					writeHoldingRegisters(address, quantity, registers);
			}
		} catch (ModbusProtocolException | ModbusNumberException
				| ModbusIOException e) {
			e.printStackTrace();
			logger.warn("MasterTCP.writeFirstRequestData: the data from the write request is not written to the slave and dace cache!");
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
		dataCache.setCoils(slaveAddress, address, coilsWrite);
		logger.info("coils data: {}", dataCache.getCoils("127.0.0.1").get(512));
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
		dataCache.setHoldingRegisters(slaveAddress, address, registersWrite);
	}
	
	private TcpParameters setTCPParameters(){
		TcpParameters tcpParameters = new TcpParameters();
		slaveAddress = config.getSlave().getAddress();
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
        tcpParameters.setPort(config.getSlave().getPort());
        return tcpParameters;
	}
	
	public void setupModbusMaster(){
		TcpParameters tcpParameters = setTCPParameters();
		master = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
        Modbus.setAutoIncrementTransactionId(true);
        try {
	        if (!master.isConnected()) {
	        	master.connect();
	        }
	        logger.info("MasterTCP.setupModbusMaster: master is connected with slave ({}).", slaveAddress);
        } catch (ModbusIOException e) {
			// TODO Auto-generated catch block
			logger.error("MasterTCP.setupModbusMaster: master cannot be connected with slave ({}).", slaveAddress);
			e.printStackTrace();
		}
	}
	
}
