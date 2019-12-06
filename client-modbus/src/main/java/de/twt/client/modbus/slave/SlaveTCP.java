package de.twt.client.modbus.slave;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
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

import de.twt.client.modbus.common.ModbusWriteRequestDTO;
import de.twt.client.modbus.common.cache.data.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.data.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.cache.request.IModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.cache.request.ModbusWriteRequestCacheManagerImpl;
import de.twt.client.modbus.slave.config.SlaveRemoteIOs;
import de.twt.client.modbus.slave.config.SlaveRemoteIOs.RemoteIOData;
import de.twt.client.modbus.slave.config.SlaveRemoteIOs.RemoteIOData.Range;

@Component
public class SlaveTCP {
	
	@Autowired
    private SlaveRemoteIOs remoteIOs;
	
	@Value("${slave.memoryRange}")
	private int range;
	
	@Value("${slave.port}")
	private int slavePort;
	
	@Value("${slave.readModule}")
	private String slaveReadModule;
	
	private IModbusDataCacheManager cache = new ModbusDataCacheManagerImpl();
	private IModbusWriteRequestCacheManager writeRequestsCache = new ModbusWriteRequestCacheManagerImpl(); 	
	private ModbusSlave slave;
	private TcpParameters tcpParameters = new TcpParameters();
	private ModbusCoils hc;
	private ModbusCoils hcd;
	private ModbusHoldingRegisters hr;
	private ModbusHoldingRegisters hri;
	private final MyOwnDataHolder dh = new MyOwnDataHolder();
	
	private final Logger logger = LogManager.getLogger(SlaveTCP.class);
	
	public int getRange(){
		return range;
	}
	
	@PostConstruct
	public void init(){
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
		for (RemoteIOData remoteIO: remoteIOs.getRemoteIOs()){
			cache.createModbusData(remoteIO.getAddress());
		}
		hc = new ModbusCoils(range);
		hcd = new ModbusCoils(range);
		hr = new ModbusHoldingRegisters(range); 
		hri = new ModbusHoldingRegisters(range);
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
	    tcpParameters.setPort(slavePort);
	}

	private void setDataHolder() throws IllegalDataAddressException, IllegalDataValueException{
		dh.addEventListener(new ModbusEventListener() {
            @Override
			public void onReadMultipleCoils(int address, int quantity) {
				// System.out.print("onReadMultipleCoils: address " + address + ", quantity " + quantity + "\n");
            	if (slaveReadModule == SlaveTCPConstants.SERVICE_READ_MODULE) {
            		waitForModbusDataCacheUpdate();
            	}
				readData(SlaveTCPConstants.MODBUS_DATA_TYPE_COIL, address, quantity);
			}
            
            @Override
            public void onReadMultipeDiscreteInputs(int address, int quantity){
            	// System.out.print("onReadMultipeDiscreteInputs: address " + address + ", quantity " + quantity + "\n");
            	if (slaveReadModule == SlaveTCPConstants.SERVICE_READ_MODULE) {
            		waitForModbusDataCacheUpdate();
            	}
				readData(SlaveTCPConstants.MODBUS_DATA_TYPE_DISCRETE_INPUT, address, quantity);
            }
            
            @Override
            public void onReadSingleHoldingResgister(int address) {
            	onReadMultipleHoldingRegisters(address, 1);
            }
            
            @Override
            public void onReadMultipleHoldingRegisters(int address, int quantity) {
            	// System.out.print("onReadMultipleHoldingRegisters: address " + address + ", value " + value + "\n");
            	if (slaveReadModule == SlaveTCPConstants.SERVICE_READ_MODULE) {
            		waitForModbusDataCacheUpdate();
            	}
				readData(SlaveTCPConstants.MODBUS_DATA_TYPE_HOLDING_REGISTER, address, quantity);
            }
            
            @Override
            public void onReadMultipleInputRegisters(int address, int quantity){
            	// System.out.print("onReadMultipleInputRegisters: address " + address + ", quantity " + quantity + "\n");
            	if (slaveReadModule == SlaveTCPConstants.SERVICE_READ_MODULE) {
            		waitForModbusDataCacheUpdate();
            	}
				readData(SlaveTCPConstants.MODBUS_DATA_TYPE_INPUT_REGISTER, address, quantity);
            }
            
            // TODO: read request
            private void waitForModbusDataCacheUpdate() {
            	return;
            }
            
			private void readData(String dataType, int address, int quantity) {
            	for(int index = 0; index < quantity; index++){
					int offsetSlave = address + index;
					RemoteIOData remoteIO = filterReomteIOData(offsetSlave);
					if (remoteIO == null){
						continue;
					}
					int offsetCache = offsetSlave - remoteIO.getOffset();
					try {
						switch(dataType) {
						case SlaveTCPConstants.MODBUS_DATA_TYPE_COIL: 
							hc.set(offsetSlave, cache.getCoils(remoteIO.getAddress()).get(offsetCache)); break;
						case SlaveTCPConstants.MODBUS_DATA_TYPE_DISCRETE_INPUT: 
							hcd.set(offsetSlave, cache.getDiscreteInputs(remoteIO.getAddress()).get(offsetCache)); break;
						case SlaveTCPConstants.MODBUS_DATA_TYPE_HOLDING_REGISTER: 
							hr.set(offsetSlave, cache.getHoldingRegisters(remoteIO.getAddress()).get(offsetCache)); break;
						case SlaveTCPConstants.MODBUS_DATA_TYPE_INPUT_REGISTER: 
							hri.set(offsetSlave, cache.getInputRegisters(remoteIO.getAddress()).get(offsetCache)); break;
						default: logger.warn("There is no such a data type ({}) in slave.", dataType); break;
						}
						hri.set(offsetSlave, cache.getInputRegisters(remoteIO.getAddress()).get(offsetCache));
					} catch (IllegalDataAddressException
							| IllegalDataValueException e) {
						e.printStackTrace();
					}
				}
            }
            
            @Override
            public void onWriteToSingleCoil(int address, boolean value) {
				// System.out.print("onWriteToSingleCoil: address " + address + ", value " + value + "\n");
            	RemoteIOData remoteIO = filterReomteIOData(address);
            	int offsetCache = address - remoteIO.getOffset();
            	ModbusWriteRequestDTO request = new ModbusWriteRequestDTO();
            	request.setCoil(offsetCache, value);
            	writeRequestsCache.putReadRequest(remoteIO.getAddress(), request);
            	// cache.setCoil(remoteIOData.address, offsetCache, value);
            }
            
            @Override
            public void onWriteToMultipleCoils(int address, int quantity, boolean[] values) {
                System.out.print("onWriteToMultipleCoils: address " + address + ", quantity " + quantity + "\n");
            	for (int idx = 0; idx < quantity; idx++){
            		int offsetSlave = address + idx;
            		int startaddress = idx;
            		// find the remote io data which contains the address
					RemoteIOData remoteIO = filterReomteIOData(offsetSlave);
					if (remoteIO == null){
						continue;
					}
					// select the relevant values
					Range range = remoteIO.getRange(offsetSlave);
					boolean[] valuesTmp;
					if ((address + quantity) > range.getEnd()){
						valuesTmp = new boolean[range.getEnd() - idx +1];
						idx = range.getEnd();
						
					} else {
						valuesTmp = new boolean[address + quantity - idx];
						idx = quantity - 1;
					}
					for (int idxTmp = 0; idxTmp < valuesTmp.length; idxTmp++) {
						valuesTmp[idxTmp] = values[startaddress + idxTmp];
					}
					// set the writing request cache
					int offsetCache = offsetSlave - remoteIO.getOffset();
					ModbusWriteRequestDTO request = new ModbusWriteRequestDTO();
					request.setCoils(offsetCache, quantity, valuesTmp);
					writeRequestsCache.putReadRequest(remoteIO.getAddress(), request);
					
            	}
            }

            @Override
            public void onWriteToSingleHoldingRegister(int address, int value) {
                // System.out.print("onWriteToSingleHoldingRegister: address " + address + ", value " + value + "\n");
            	RemoteIOData remoteIO = filterReomteIOData(address);
				if (remoteIO == null){
					return;
				}
            	int offsetCache = address - remoteIO.getOffset();
            	ModbusWriteRequestDTO request = new ModbusWriteRequestDTO();
            	request.setHoldingRegister(offsetCache, value);
            	writeRequestsCache.putReadRequest(remoteIO.getAddress(), request);
            	// cache.setHoldingRegister(remoteIOData.address, offsetCache, value);
            }

            @Override
            public void onWriteToMultipleHoldingRegisters(int address, int quantity, int[] values) {
                // System.out.print("onWriteToMultipleHoldingRegisters: address " + address + ", quantity " + quantity + "\n");
            	for (int idx = 0; idx < quantity; idx++){
            		int offsetSlave = address + idx;
            		int startaddress = idx;
            		// find the remote io data which contains the address
					RemoteIOData remoteIO = filterReomteIOData(offsetSlave);
					if (remoteIO == null){
						continue;
					}
					// select the relevant values
					Range range = remoteIO.getRange(offsetSlave);
					int[] valuesTmp;
					if ((address + quantity) > range.getEnd()){
						valuesTmp = new int[range.getEnd() - idx +1];
						idx = range.getEnd();
						
					} else {
						valuesTmp = new int[address + quantity - idx];
						idx = quantity - 1;
					}
					for (int idxTmp = 0; idxTmp < valuesTmp.length; idxTmp++) {
						valuesTmp[idxTmp] = values[startaddress + idxTmp];
					}
					// set the writing request cache
					int offsetCache = offsetSlave - remoteIO.getOffset();
					ModbusWriteRequestDTO request = new ModbusWriteRequestDTO();
					request.setHoldingRegisters(offsetCache, quantity, valuesTmp);
					writeRequestsCache.putReadRequest(remoteIO.getAddress(), request);
            	}
            }
            
            private RemoteIOData filterReomteIOData(int address){
            	for (RemoteIOData remoteIO: remoteIOs.getRemoteIOs()){
            		for (Range range: remoteIO.getRanges()){
            			if (address >= range.getStart() && address <= range.getEnd()){
            				return remoteIO;
            			}
            		}
            	}
            	return null;
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

