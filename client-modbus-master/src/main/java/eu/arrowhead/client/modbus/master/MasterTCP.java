package eu.arrowhead.client.modbus.master;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

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

import eu.arrowhead.client.modbus.cache.data.IModbusDataCacheManager;
import eu.arrowhead.client.modbus.cache.data.ModbusDataCacheManagerImpl;
import eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager;
import eu.arrowhead.client.modbus.cache.request.ModbusWriteRequestCacheManagerImpl;
import eu.arrowhead.client.modbus.common.ModbusWriteRequestDTO;
import eu.arrowhead.client.modbus.master.config.ModbusTCPConfigProperties;

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
	
	public void readDataThread() {
		new Thread() {
			public void run() {
				while(!stopReadingData){
					long startTime=System.currentTimeMillis(); 
					readData();
					long endTime=System.currentTimeMillis(); 
					long intervalTime = endTime - startTime;
					if (intervalTime > config.getPeriodTime()) {
						logger.warn("MasterTCP.readDataThread: the running time of one period is longer than the setting period time.");
					}
				}
			}
		}.start();
	}
	
	public void readData() {
		Map<String, String> dataToRead = config.getData().getRead();
		for(Map.Entry<String, String> entry: dataToRead.entrySet()) {
			String[] ranges = entry.getValue().split(",");
			for(String range : ranges) {
				String[] tmp = range.split("-");
				if (tmp.length == 0) {
					continue;
				}
				int offset = Integer.parseInt(tmp[0]);
				int quantity = 0;
				if (tmp.length > 1){
					quantity = Integer.parseInt(tmp[1]) - offset + 1;
				}
				try {
					readData(entry.getKey(), offset, quantity);
				} catch (ModbusProtocolException | ModbusNumberException
						| ModbusIOException e) {
					// e.printStackTrace();
					logger.warn("MasterTCP.readData: There is no value at the selected address in slave!");
				}
			}
			
		}
	}
	
	public void readData(String type, int offset, int quantity) 
			throws ModbusProtocolException, ModbusNumberException, ModbusIOException {
		if (!master.isConnected()){
			master.connect();
		}
		switch(type) {
		case "coils": 
			dataCache.setCoils(slaveAddress, offset, master.readCoils(slaveId, offset, quantity)); break;
		case "discreteInputs": 
			dataCache.setDiscreteInputs(slaveAddress, offset, master.readDiscreteInputs(slaveId, offset, quantity)); break;
		case "holdingRegisters":
			dataCache.setHoldingRegisters(slaveAddress, offset, master.readHoldingRegisters(slaveId, offset, quantity)); break;
		case "inputRegisters": 
			dataCache.setInputRegisters(slaveAddress, offset, master.readInputRegisters(slaveId, offset, quantity));break;
		default: break;
		}
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
	
	public void writeDataThread() {
		new Thread() {
			public void run() {
				while(!stopWritingData){
					long startTime=System.currentTimeMillis(); 
					ModbusWriteRequestDTO request = writngRequestCache.getFirstReadRequest(slaveAddress);
					if (request == null) {
						continue;
					}
					writeFirstRequestData(request);
					writngRequestCache.deleteFirstReadRequest(slaveAddress);
					long endTime=System.currentTimeMillis(); 
					long intervalTime = endTime - startTime;
					if (intervalTime > config.getPeriodTime()) {
						logger.warn("MasterTCP.writeDataThread: the running time of one period is longer than the setting period time.");
					}
				}
			}
		}.start();
		
		
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
		slaveAddress = config.getSlaveAddress();
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
        tcpParameters.setPort(config.getSlavePort());
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
