package de.twt.client.modbus.slave;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.data.DataHolder;
import com.intelligt.modbus.jlibmodbus.data.ModbusCoils;
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataAddressException;
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataValueException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlave;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlaveFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import com.intelligt.modbus.jlibmodbus.utils.FrameEvent;
import com.intelligt.modbus.jlibmodbus.utils.FrameEventListener;
import com.intelligt.modbus.jlibmodbus.utils.ModbusSlaveTcpObserver;
import com.intelligt.modbus.jlibmodbus.utils.TcpClientInfo;

import de.twt.client.modbus.common.ModbusReadRequestDTO;
import de.twt.client.modbus.common.ModbusWriteRequestDTO;
import de.twt.client.modbus.common.cache.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.IModbusReadRequestCacheManager;
import de.twt.client.modbus.common.cache.IModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.cache.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.cache.ModbusReadRequestCacheManagerImpl;
import de.twt.client.modbus.common.cache.ModbusWriteRequestCacheManagerImpl;
import de.twt.client.modbus.common.constants.ModbusConstants;


public class SlaveTCP {
    private SlaveTCPConfig slaveTCPConfig;
	private IModbusDataCacheManager modbusDataCacheManager = new ModbusDataCacheManagerImpl();
	private IModbusReadRequestCacheManager modbusReadRequestCacheManager = new ModbusReadRequestCacheManagerImpl();
	private IModbusWriteRequestCacheManager modbusWriteRequestCacheManager = new ModbusWriteRequestCacheManagerImpl();
	private ModbusSlave slave;
	private TcpParameters tcpParameters = new TcpParameters();
	private ModbusCoils hc;
	private ModbusCoils hcd;
	private ModbusHoldingRegisters hr;
	private ModbusHoldingRegisters hri;
	private final MyOwnDataHolder dh = new MyOwnDataHolder();
	
	private final Logger logger = LogManager.getLogger(SlaveTCP.class);
	
	public SlaveTCP(SlaveTCPConfig slaveTCPConfig) {
		this.slaveTCPConfig = slaveTCPConfig;
		init();
	}
	
	private void init(){
		logger.debug("init slave tcp...");
		initModbusDataCache();
		try {
			setSlave();
		} catch (IllegalDataAddressException | IllegalDataValueException
				| UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
 	public void startSlave(){
 		logger.debug("slave starts...");
		try {
			slave.listen();
		} catch (ModbusIOException e) {
			e.printStackTrace();
		}
	}
 	
 	private void initModbusDataCache(){
		modbusDataCacheManager.createModbusData(slaveTCPConfig.getRemoteIO().getAddress());
		int memoryRange = slaveTCPConfig.getMemoryRange();
		hc = new ModbusCoils(memoryRange);
		hcd = new ModbusCoils(memoryRange);
		hr = new ModbusHoldingRegisters(memoryRange); 
		hri = new ModbusHoldingRegisters(memoryRange);
	}
 	
	private void setSlave() throws IllegalDataAddressException, IllegalDataValueException, UnknownHostException{
		logger.debug("set slave parameters...");
		setTCPConnection();
		slave = ModbusSlaveFactory.createModbusSlaveTCP(tcpParameters);
		slave.setServerAddress(Modbus.TCP_DEFAULT_ID);
        slave.setBroadcastEnabled(true);
        slave.setReadTimeout(1000);
        Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);
        setDataHolder();
        setFrameEventListener();
        setObserver();
        slave.setServerAddress(1);
	}
	
	private void setTCPConnection() throws UnknownHostException{
	    tcpParameters.setHost(InetAddress.getLocalHost());
	    tcpParameters.setKeepAlive(true);
	    tcpParameters.setPort(slaveTCPConfig.getPort());
	}

	private void setDataHolder() throws IllegalDataAddressException, IllegalDataValueException{
		dh.addEventListener(new ModbusEventListener() {
            @Override
			public void onReadMultipleCoils(int address, int quantity) {
				// System.out.print("onReadMultipleCoils: address " + address + ", quantity " + quantity + "\n");
            	if (slaveTCPConfig.getReadModule().equalsIgnoreCase(SlaveTCPConstants.SERVICE_READ_MODULE)) {
            		waitForModbusDataCacheUpdate(ModbusConstants.MODBUS_DATA_TYPE_COIL, address, quantity);
            	}
				readData(ModbusConstants.MODBUS_DATA_TYPE_COIL, address, quantity);
			}
            
            @Override
            public void onReadMultipeDiscreteInputs(int address, int quantity){
            	// System.out.print("onReadMultipeDiscreteInputs: address " + address + ", quantity " + quantity + "\n");
            	if (slaveTCPConfig.getReadModule().equalsIgnoreCase(SlaveTCPConstants.SERVICE_READ_MODULE)) {
            		waitForModbusDataCacheUpdate(ModbusConstants.MODBUS_DATA_TYPE_DISCRETE_INPUT, address, quantity);
            	}
				readData(ModbusConstants.MODBUS_DATA_TYPE_DISCRETE_INPUT, address, quantity);
            }
            
            @Override
            public void onReadSingleHoldingResgister(int address) {
            	onReadMultipleHoldingRegisters(address, 1);
            }
            
            @Override
            public void onReadMultipleHoldingRegisters(int address, int quantity) {
            	// System.out.print("onReadMultipleHoldingRegisters: address " + address + ", value " + value + "\n");
            	if (slaveTCPConfig.getReadModule().equalsIgnoreCase(SlaveTCPConstants.SERVICE_READ_MODULE)) {
            		waitForModbusDataCacheUpdate(ModbusConstants.MODBUS_DATA_TYPE_HOLDING_REGISTER, address, quantity);
            	}
				readData(ModbusConstants.MODBUS_DATA_TYPE_HOLDING_REGISTER, address, quantity);
            }
            
            @Override
            public void onReadMultipleInputRegisters(int address, int quantity){
            	// System.out.print("onReadMultipleInputRegisters: address " + address + ", quantity " + quantity + "\n");
            	if (slaveTCPConfig.getReadModule().equalsIgnoreCase(SlaveTCPConstants.SERVICE_READ_MODULE)) {
            		waitForModbusDataCacheUpdate(ModbusConstants.MODBUS_DATA_TYPE_INPUT_REGISTER, address, quantity);
            	}
				readData(ModbusConstants.MODBUS_DATA_TYPE_INPUT_REGISTER, address, quantity);
            }
            
            // TODO: collect all requests in one request
            private void waitForModbusDataCacheUpdate(String type, int offset, int quantity) {
            	logger.info("wait for data start...");
            	ModbusReadRequestDTO request = new ModbusReadRequestDTO();
            	HashMap<Integer, Integer> addressMap = new HashMap<Integer, Integer>();
            	addressMap.put(offset,  quantity);
            	switch(type) {
            	case ModbusConstants.MODBUS_DATA_TYPE_COIL: 
            		request.setCoilsAddressMap(addressMap); break;
            	case ModbusConstants.MODBUS_DATA_TYPE_DISCRETE_INPUT: 
            		request.setDiscreteInputsAddressMap(addressMap); break;
            	case ModbusConstants.MODBUS_DATA_TYPE_HOLDING_REGISTER: 
            		request.setHoldingRegistersAddressMap(addressMap); break;
            	case ModbusConstants.MODBUS_DATA_TYPE_INPUT_REGISTER: 
            		request.setInputRegistersAddressMap(addressMap); break;
            	}
            	
            	String slaveAddress = slaveTCPConfig.getRemoteIO().getAddress();
            	modbusReadRequestCacheManager.putReadRequest(slaveAddress, request);
            	int period = 0;
            	while (modbusReadRequestCacheManager.isIDExist(slaveAddress, request.getID())) {
            		if (period++ > 1000) {
            			logger.info("waitForModbusDataCacheUpdate: the request is not finished. use the default values in modbus data cache.");
        				break;
            		}
            		
            		try {
						TimeUnit.MILLISECONDS.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
            	}
            	logger.info(modbusDataCacheManager.getDiscreteInputs(slaveAddress).get(0));
            }
            
			private void readData(String dataType, int address, int quantity) {
				String slaveAddress = slaveTCPConfig.getRemoteIO().getAddress();
            	for(int index = 0; index < quantity; index++){
					int offset = address + index;
					try {
						switch(dataType) {
						case ModbusConstants.MODBUS_DATA_TYPE_COIL: 
							hc.set(offset, modbusDataCacheManager.getCoils(slaveAddress).get(offset)); break;
						case ModbusConstants.MODBUS_DATA_TYPE_DISCRETE_INPUT: 
							hcd.set(offset, modbusDataCacheManager.getDiscreteInputs(slaveAddress).get(offset)); break;
						case ModbusConstants.MODBUS_DATA_TYPE_HOLDING_REGISTER: 
							hr.set(offset, modbusDataCacheManager.getHoldingRegisters(slaveAddress).get(offset)); break;
						case ModbusConstants.MODBUS_DATA_TYPE_INPUT_REGISTER: 
							hri.set(offset, modbusDataCacheManager.getInputRegisters(slaveAddress).get(offset)); break;
						default: logger.warn("There is no such a data type ({}) in slave.", dataType); break;
						}
					} catch (IllegalDataAddressException
							| IllegalDataValueException e) {
						e.printStackTrace();
					}
				}
            }
            
            @Override
            public void onWriteToSingleCoil(int address, boolean value) {
				// System.out.print("onWriteToSingleCoil: address " + address + ", value " + value + "\n");
            	String slaveAddress = slaveTCPConfig.getRemoteIO().getAddress();
            	ModbusWriteRequestDTO request = new ModbusWriteRequestDTO();
            	request.setCoil(address, value);
            	modbusWriteRequestCacheManager.putWriteRequest(slaveAddress, request);
            }
            
            @Override
            public void onWriteToMultipleCoils(int address, int quantity, boolean[] values) {
                // System.out.print("onWriteToMultipleCoils: address " + address + ", quantity " + quantity + "\n");
                String slaveAddress = slaveTCPConfig.getRemoteIO().getAddress();
                ModbusWriteRequestDTO request = new ModbusWriteRequestDTO();
                request.setCoils(address, quantity, values.clone());
				modbusWriteRequestCacheManager.putWriteRequest(slaveAddress, request);
            }

            @Override
            public void onWriteToSingleHoldingRegister(int address, int value) {
                // System.out.print("onWriteToSingleHoldingRegister: address " + address + ", value " + value + "\n");
            	String slaveAddress = slaveTCPConfig.getRemoteIO().getAddress();
            	ModbusWriteRequestDTO request = new ModbusWriteRequestDTO();
            	request.setHoldingRegister(address, value);
            	modbusWriteRequestCacheManager.putWriteRequest(slaveAddress, request);
            }

            @Override
            public void onWriteToMultipleHoldingRegisters(int address, int quantity, int[] values) {
                // System.out.print("onWriteToMultipleHoldingRegisters: address " + address + ", quantity " + quantity + "\n");
            	String slaveAddress = slaveTCPConfig.getRemoteIO().getAddress();
				ModbusWriteRequestDTO request = new ModbusWriteRequestDTO();
				request.setHoldingRegisters(address, quantity, values);
				modbusWriteRequestCacheManager.putWriteRequest(slaveAddress, request);
            }
        });
        slave.setDataHolder(dh);
        slave.getDataHolder().setCoils(hc);
        slave.getDataHolder().setDiscreteInputs(hcd);
        slave.getDataHolder().setHoldingRegisters(hr);
        slave.getDataHolder().setInputRegisters(hri);
	}
	
	private void setFrameEventListener(){
		FrameEventListener listener = new FrameEventListener() {
            @Override
            public void frameSentEvent(FrameEvent event) {
                // System.out.println("frame sent " + DataUtils.toAscii(event.getBytes()));
            }

            @Override
            public void frameReceivedEvent(FrameEvent event) {
                // System.out.println("frame recv " + DataUtils.toAscii(event.getBytes()));
            }
        };
        slave.addListener(listener);
	}
	
	@SuppressWarnings("deprecation")
	private void setObserver(){
		Observer o = new ModbusSlaveTcpObserver() {
            @Override
            public void clientAccepted(TcpClientInfo info) {
                System.out.println("Client connected " + info.getTcpParameters().getHost());
            }

            @Override
            public void clientDisconnected(TcpClientInfo info) {
                System.out.println("Client disconnected " + info.getTcpParameters().getHost());
            }
        };
        slave.addObserver(o);
	}
	
	public interface ModbusEventListener {
        void onReadMultipleCoils(int address, int quantity);
        
        void onReadMultipeDiscreteInputs(int address, int quantity);
        
        void onReadSingleHoldingResgister(int address);
        
        void onReadMultipleHoldingRegisters(int address, int quantity);
        
        void onReadMultipleInputRegisters(int address, int quantity);

        void onWriteToSingleCoil(int address, boolean value);
        
        void onWriteToMultipleCoils(int address, int quantity, boolean[] values);

        void onWriteToSingleHoldingRegister(int address, int value);

        void onWriteToMultipleHoldingRegisters(int address, int quantity, int[] values);
    }

    public static class MyOwnDataHolder extends DataHolder {

        final List<ModbusEventListener> modbusEventListenerList = new ArrayList<ModbusEventListener>();

        public MyOwnDataHolder() {
        }

        public void addEventListener(ModbusEventListener listener) {
            modbusEventListenerList.add(listener);
        }

        public boolean removeEventListener(ModbusEventListener listener) {
            return modbusEventListenerList.remove(listener);
        }

        @Override
        public void writeHoldingRegister(int offset, int value) throws IllegalDataAddressException, IllegalDataValueException {
            for (ModbusEventListener l : modbusEventListenerList) {
                l.onWriteToSingleHoldingRegister(offset, value);
            }
            super.writeHoldingRegister(offset, value);
        }

        @Override
        public void writeHoldingRegisterRange(int offset, int[] range) throws IllegalDataAddressException, IllegalDataValueException {
            for (ModbusEventListener l : modbusEventListenerList) {
                l.onWriteToMultipleHoldingRegisters(offset, range.length, range);
            }
            super.writeHoldingRegisterRange(offset, range);
        }

        @Override
        public void writeCoil(int offset, boolean value) throws IllegalDataAddressException, IllegalDataValueException {
            for (ModbusEventListener l : modbusEventListenerList) {
                l.onWriteToSingleCoil(offset, value);
            }
            super.writeCoil(offset, value);
        }

        @Override
        public void writeCoilRange(int offset, boolean[] range) throws IllegalDataAddressException, IllegalDataValueException {
            for (ModbusEventListener l : modbusEventListenerList) {
                l.onWriteToMultipleCoils(offset, range.length, range);
            }
            super.writeCoilRange(offset, range);
        }
        
        @Override
        public boolean[] readCoilRange(int offset, int quantity) throws IllegalDataAddressException, IllegalDataValueException{
        	for (ModbusEventListener l : modbusEventListenerList) {
                l.onReadMultipleCoils(offset, quantity);
            }
        	boolean[] values = super.readCoilRange(offset, quantity);
            return values;
        }
        
        @Override
        public boolean[] readDiscreteInputRange(int offset, int quantity) throws IllegalDataAddressException, IllegalDataValueException {
        	for (ModbusEventListener l : modbusEventListenerList) {
                l.onReadMultipeDiscreteInputs(offset, quantity);
            }
        	boolean[] values = super.readDiscreteInputRange(offset, quantity);
        	return values;
        }
        
        @Override
        public int readHoldingRegister(int offset) throws IllegalDataAddressException {
        	for (ModbusEventListener l : modbusEventListenerList) {
                l.onReadSingleHoldingResgister(offset);
            }
        	int value = super.readHoldingRegister(offset);
        	return value;
        }
        
        @Override
        public int[] readHoldingRegisterRange(int offset, int quantity) throws IllegalDataAddressException {
        	for (ModbusEventListener l : modbusEventListenerList) {
                l.onReadMultipleHoldingRegisters(offset, quantity);
            }
        	int[] values = super.readHoldingRegisterRange(offset, quantity);
        	return values;
        }
        
        @Override
        public int[] readInputRegisterRange(int offset, int quantity) throws IllegalDataAddressException {
        	for (ModbusEventListener l : modbusEventListenerList) {
                l.onReadMultipleInputRegisters(offset, quantity);
            }
        	int[] values = super.readInputRegisterRange(offset, quantity);
        	return values;
        }
    }
	
}

