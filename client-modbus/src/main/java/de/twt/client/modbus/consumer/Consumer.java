package de.twt.client.modbus.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import de.twt.client.modbus.common.ModbusData;
import de.twt.client.modbus.common.ModbusReadRequestDTO;
import de.twt.client.modbus.common.ModbusResponseDTO;
import de.twt.client.modbus.common.ModbusWriteRequestDTO;
import de.twt.client.modbus.common.cache.data.IModbusDataCacheManager;
import de.twt.client.modbus.common.cache.data.ModbusDataCacheManagerImpl;
import de.twt.client.modbus.common.cache.request.IModbusReadRequestCacheManager;
import de.twt.client.modbus.common.cache.request.IModbusWriteRequestCacheManager;
import de.twt.client.modbus.common.cache.request.ModbusReadRequestCacheManagerImpl;
import de.twt.client.modbus.common.cache.request.ModbusWriteRequestCacheManagerImpl;
import de.twt.client.modbus.master.MasterTCPConstants;
import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.exception.ArrowheadException;

@Component
public class Consumer {
	@Autowired
	private ArrowheadService arrowheadService;
	
	@Autowired
	protected SSLProperties sslProperties;
    
	private final IModbusWriteRequestCacheManager modbusWriteRequestsCacheManager = new ModbusWriteRequestCacheManagerImpl();
	private final IModbusReadRequestCacheManager modbusReadRequestsCacheManager = new ModbusReadRequestCacheManagerImpl();
	private final IModbusDataCacheManager modbusDataCacheManager = new ModbusDataCacheManagerImpl();
	private final Logger logger = LogManager.getLogger( Consumer.class );
	
	private List<OrchestrationResultDTO> orchestrationResults = new ArrayList<>();
	private final HashMap<String, Thread> threads = new HashMap<String, Thread>();
	private boolean stopReadingData = false;
	private boolean stopWritingData = false;
	
	
	private OrchestrationResponseDTO getServiceProvider(String serviceDefinition) {
		logger.debug("getServiceProvider: try to get the service provider...");
    	final String interfaces = sslProperties.isSslEnabled() ?
    			ConsumerModbusConstants.INTERFACE_SECURE : ConsumerModbusConstants.INTERFACE_INSECURE;
    	final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO.Builder(serviceDefinition)
    														.interfaces(interfaces)
    														.build();
    	requestedService.setServiceDefinitionRequirement(serviceDefinition);
    	
    	//orchestrationFormBuilder.
    	final Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();
    	final OrchestrationFormRequestDTO orchestrationRequest = orchestrationFormBuilder
    			.requestedService(requestedService)
				.flag(Flag.MATCHMAKING, false) //When this flag is false or not specified, then the orchestration response cloud contain more proper provider. Otherwise only one will be chosen if there is any proper.
				.flag(Flag.OVERRIDE_STORE, true) //When this flag is false or not specified, then a Store Orchestration will be proceeded. Otherwise a Dynamic Orchestration will be proceeded.
				.flag(Flag.TRIGGER_INTER_CLOUD, false) //When this flag is false or not specified, then orchestration will not look for providers in the neighbor clouds, when there is no proper provider in the local cloud. Otherwise it will. 
    			.build();
    	
    	OrchestrationResponseDTO response = null;
    	try {
    		response = arrowheadService.proceedOrchestration(orchestrationRequest);			
		} catch (final ArrowheadException ex) {
			logger.error("Failed to communicate with orchestration to get the provider infomation!");
		}
    	
    	return response;
	}
	
	
	public void writeDataThread() {
		logger.info("writeData: start writing data...");
		OrchestrationResponseDTO orchestrationResponse = getServiceProvider(ConsumerModbusConstants.WRITE_MODBZS_DATA_SERVICE_DEFINITION);
		if (orchestrationResponse == null) {
			logger.warn("No orchestration response received");
			return;
		} else if (orchestrationResponse.getResponse().isEmpty()) {
			logger.warn("No provider with service \"{}\" found during the orchestration", 
					ConsumerModbusConstants.WRITE_MODBZS_DATA_SERVICE_DEFINITION);
			return;
		}
		
		orchestrationResults = orchestrationResponse.getResponse();
		for (OrchestrationResultDTO orchestrationResult : orchestrationResults) {
			Thread thread = new Thread() {
				public void run() {
					writeDataToSlaveAddress(orchestrationResult);
				}
			};
			threads.put(ConsumerModbusConstants.THREAD_WRITE, thread);
			thread.start();
		}
	}
	
	private void writeDataToSlaveAddress(OrchestrationResultDTO orchestrationResult) {
		final String slaveAddress = orchestrationResult.getMetadata().get(ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS);
		while (!stopWritingData) {
			if (modbusWriteRequestsCacheManager.isImplemented(slaveAddress)) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.debug("Consumer.writeDataToSlaveAddress: the writing thread does not make a pause.");
				}
				continue;
			}
			writeOneDataToSlaveAddress(orchestrationResult);
		}
	}
	
	//TODO: slaveaddress is wrong
	private void writeOneDataToSlaveAddress(OrchestrationResultDTO orchestrationResult){
		final String slaveAddress = orchestrationResult.getMetadata().get(ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS);
		ModbusWriteRequestDTO request = modbusWriteRequestsCacheManager.getWriteRequestToImplement(slaveAddress);
		final HttpMethod httpMethod = HttpMethod.valueOf(orchestrationResult.getMetadata().get(ConsumerModbusConstants.HTTP_METHOD));
		final String providerAddress = orchestrationResult.getProvider().getAddress();
		final int providerPort = orchestrationResult.getProvider().getPort();
    	final String serviceUri = orchestrationResult.getServiceUri().replace("slaveAddress", slaveAddress);
    	final String interfaceName = orchestrationResult.getInterfaces().get(0).getInterfaceName(); //Simplest way of choosing an interface.
    	final String token = orchestrationResult.getAuthorizationTokens() == null ? 
    			null : orchestrationResult.getAuthorizationTokens().get(getInterface());
    	final String[] queryParamSlaveAddress = 
    		{ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS, slaveAddress};
		boolean isSuccess = arrowheadService.consumeServiceHTTP(boolean.class, httpMethod, providerAddress, providerPort,
				serviceUri, interfaceName, token, request, queryParamSlaveAddress);
		if (!isSuccess) {
			logger.warn("The writing reuquest is not successfully implemented by the provider!");
			return;
		}
		modbusDataCacheManager.setCoils(slaveAddress, request.getAddress(), request.getCoils());
		modbusDataCacheManager.setHoldingRegisters(slaveAddress, request.getAddress(), request.getHoldingRegisters());
		logger.debug("Provider response");
	}
	
	public void readDataThread() {
		logger.info("readData: start reading data...");
		OrchestrationResponseDTO orchestrationResponse = getServiceProvider(ConsumerModbusConstants.READ_MODBZS_DATA_SERVICE_DEFINITION);
		if (orchestrationResponse == null) {
			logger.warn("No orchestration response received");
			return;
		} else if (orchestrationResponse.getResponse().isEmpty()) {
			logger.warn("No provider with service \"{}\" found during the orchestration", 
					ConsumerModbusConstants.READ_MODBZS_DATA_SERVICE_DEFINITION);
			return;
		}
		logger.debug("readData: start consuming");
		orchestrationResults = orchestrationResponse.getResponse();
		for (OrchestrationResultDTO orchestrationResult : orchestrationResults) {
			Thread thread = new Thread() {
				public void run() {
					readDataFromSlaveAddress(orchestrationResult);
				}
			};
			threads.put(ConsumerModbusConstants.THREAD_READ, thread);
			thread.start();
		}
	}
	
	private void readDataFromSlaveAddress(OrchestrationResultDTO orchestrationResult) {
		final String slaveAddress = orchestrationResult.getMetadata().get(ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS);
		while (!stopReadingData) {
			if (modbusReadRequestsCacheManager.isEmpty(slaveAddress)) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.warn("Consumer.writeDataToSlaveAddress: the reading thread does not make a pause.");
				}
				continue;
			}
			logger.info("readDataFromSlaveAddress: start one consume");
			readOneDataFromSlaveAddress(orchestrationResult);
			modbusReadRequestsCacheManager.deleteFirstReadRequest(slaveAddress);
		}
	}
	
	private void readOneDataFromSlaveAddress(OrchestrationResultDTO orchestrationResult){
		final String slaveAddress = orchestrationResult.getMetadata().get("slaveAddress");
		ModbusReadRequestDTO request = modbusReadRequestsCacheManager.getFirstReadRequest(slaveAddress);
		final HttpMethod httpMethod = HttpMethod.valueOf(orchestrationResult.getMetadata().get(ConsumerModbusConstants.HTTP_METHOD));
		final String providerAddress = orchestrationResult.getProvider().getAddress();
		final int providerPort = orchestrationResult.getProvider().getPort();
    	final String serviceUri = orchestrationResult.getServiceUri().replace("slaveAddress", slaveAddress);
    	final String interfaceName = orchestrationResult.getInterfaces().get(0).getInterfaceName(); //Simplest way of choosing an interface.
    	final String token = orchestrationResult.getAuthorizationTokens() == null ? 
    			null : orchestrationResult.getAuthorizationTokens().get(getInterface());
    	final String[] queryParamSlaveAddress = 
    		{ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS, slaveAddress};
    	ModbusResponseDTO response = arrowheadService.consumeServiceHTTP(ModbusResponseDTO.class, httpMethod, providerAddress, providerPort, serviceUri,
				interfaceName, token, request, queryParamSlaveAddress);
    	logger.info(response.getE().get(0).getDiscreteInputs().get(0));
    	writeDataToModbusDataCache(response, slaveAddress);
	}
	
	
	private String getInterface() {
    	return sslProperties.isSslEnabled() ? ConsumerModbusConstants.INTERFACE_SECURE : ConsumerModbusConstants.INTERFACE_INSECURE;
    }
	
	private void writeDataToModbusDataCache(ModbusResponseDTO response, String slaveAddress) {
		logger.debug("write date to modbus data cache...");
		if (response == null) {
			logger.warn("The cosumer does not get the response from the provider.");
			return;
		}
		if (response.getE().size() == 0) {
			logger.warn("The provider does not work properly! The consumer does not get the modbus data!");
			return;
		}
		ModbusData modbusData = response.getE().get(0);
		modbusDataCacheManager.setCoils(slaveAddress, modbusData.getCoils());
		modbusDataCacheManager.setDiscreteInputs(slaveAddress, modbusData.getDiscreteInputs());
		modbusDataCacheManager.setHoldingRegisters(slaveAddress, modbusData.getHoldingRegisters());
		modbusDataCacheManager.setInputRegisters(slaveAddress, modbusData.getInputRegisters());
	}
	
}