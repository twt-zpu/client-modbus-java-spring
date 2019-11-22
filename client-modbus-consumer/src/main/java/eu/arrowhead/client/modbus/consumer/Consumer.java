package eu.arrowhead.client.modbus.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.modbus.cache.data.IModbusDataCacheManager;
import eu.arrowhead.client.modbus.cache.data.ModbusDataCacheManagerImpl;
import eu.arrowhead.client.modbus.cache.request.IModbusReadRequestCacheManager;
import eu.arrowhead.client.modbus.cache.request.IModbusWriteRequestCacheManager;
import eu.arrowhead.client.modbus.cache.request.ModbusReadRequestCacheManagerImpl;
import eu.arrowhead.client.modbus.cache.request.ModbusWriteRequestCacheManagerImpl;
import eu.arrowhead.client.modbus.common.ModbusData;
import eu.arrowhead.client.modbus.common.ModbusReadRequestDTO;
import eu.arrowhead.client.modbus.common.ModbusResponseDTO;
import eu.arrowhead.client.modbus.common.ModbusWriteRequestDTO;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.exception.ArrowheadException;

@Component
public class Consumer {
	@Autowired
	private ArrowheadService arrowheadService;
	
	@Autowired
	protected SSLProperties sslProperties;
    
	private final IModbusWriteRequestCacheManager writingRequestsCache = new ModbusWriteRequestCacheManagerImpl();
	private final IModbusReadRequestCacheManager readingRequestsCache = new ModbusReadRequestCacheManagerImpl();
	private final IModbusDataCacheManager dataCache = new ModbusDataCacheManagerImpl();
	private final Logger logger = LogManager.getLogger( Consumer.class );
	
	private List<OrchestrationResultDTO> orchestrationResults = new ArrayList<>();
	private List<Thread> readDataThreads = new ArrayList<>();
	private List<Thread> writeDataThreads = new ArrayList<>();
	private boolean stopReadingData = false;
	private boolean stopWritingData = false;
	
	
	public void writeData() {
		OrchestrationResponseDTO orchestrationResponse = getServiceProvider(ConsumerModbusConstants.WRITE_MODBZS_DATA_SERVICE_DEFINITION);
		if (orchestrationResponse == null) {
			logger.info("No orchestration response received");
			return;
		} else if (orchestrationResponse.getResponse().isEmpty()) {
			logger.info("No provider with service \"{}\" found during the orchestration", 
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
			writeDataThreads.add(thread);
			thread.start();
		}
	}
	
	private OrchestrationResponseDTO getServiceProvider(String serviceDefinition) {
    	final Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();
    	
    	final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
    	requestedService.setServiceDefinitionRequirement(serviceDefinition);
    	
    	orchestrationFormBuilder.requestedService(requestedService)
    							.flag(Flag.MATCHMAKING, false) //When this flag is false or not specified, then the orchestration response cloud contain more proper provider. Otherwise only one will be chosen if there is any proper.
    							.flag(Flag.OVERRIDE_STORE, true) //When this flag is false or not specified, then a Store Orchestration will be proceeded. Otherwise a Dynamic Orchestration will be proceeded.
    							.flag(Flag.TRIGGER_INTER_CLOUD, false); //When this flag is false or not specified, then orchestration will not look for providers in the neighbor clouds, when there is no proper provider in the local cloud. Otherwise it will. 
    	
    	final OrchestrationFormRequestDTO orchestrationRequest = orchestrationFormBuilder.build();
    	
    	OrchestrationResponseDTO response = null;
    	try {
    		response = arrowheadService.proceedOrchestration(orchestrationRequest);			
		} catch (final ArrowheadException ex) {
			logger.error("Failed to communicate with orchestration to get the provider infomation!");
		}
    	
    	return response;
	}
	
	private void writeDataToSlaveAddress(OrchestrationResultDTO orchestrationResult) {
		while (!stopWritingData) {
			final String slaveAddress = orchestrationResult.getMetadata().get(ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS);
			if (writingRequestsCache.isEmpty(slaveAddress)) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.debug("Consumer.writeDataToSlaveAddress: the writing thread does not make a pause.");
				}
				continue;
			}
			writeOneDataToSlaveAddress(orchestrationResult);
			writingRequestsCache.deleteFirstReadRequest(slaveAddress);
		}
	}
	
	private void writeOneDataToSlaveAddress(OrchestrationResultDTO orchestrationResult){
		final String slaveAddress = orchestrationResult.getMetadata().get("slaveAddress");
		ModbusWriteRequestDTO request = writingRequestsCache.getFirstReadRequest(slaveAddress);
		final HttpMethod httpMethod = HttpMethod.valueOf(orchestrationResult.getMetadata().get(ConsumerModbusConstants.HTTP_METHOD));
		final String providerAddress = orchestrationResult.getProvider().getAddress();
		final int providerPort = orchestrationResult.getProvider().getPort();
    	final String serviceUri = orchestrationResult.getServiceUri().replace("slaveAddress", slaveAddress);
    	final String interfaceName = orchestrationResult.getInterfaces().get(0).getInterfaceName(); //Simplest way of choosing an interface.
    	final String token = orchestrationResult.getAuthorizationTokens() == null ? 
    			null : orchestrationResult.getAuthorizationTokens().get(getInterface());
    	final String[] queryParamSlaveAddress = 
    		{orchestrationResult.getMetadata().get(ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS), slaveAddress};
		arrowheadService.consumeServiceHTTP(boolean.class, httpMethod, providerAddress, providerPort, serviceUri,
				interfaceName, token, request, queryParamSlaveAddress);
		logger.info("Provider response");
	}
	
	public void readData() {
		OrchestrationResponseDTO orchestrationResponse = getServiceProvider(ConsumerModbusConstants.READ_MODBZS_DATA_SERVICE_DEFINITION);
		if (orchestrationResponse == null) {
			logger.warn("No orchestration response received");
			return;
		} else if (orchestrationResponse.getResponse().isEmpty()) {
			logger.warn("No provider with service \"{}\" found during the orchestration", 
					ConsumerModbusConstants.READ_MODBZS_DATA_SERVICE_DEFINITION);
			return;
		}
		
		orchestrationResults = orchestrationResponse.getResponse();
		for (OrchestrationResultDTO orchestrationResult : orchestrationResults) {
			Thread thread = new Thread() {
				public void run() {
					readDataFromSlaveAddress(orchestrationResult);
				}
			};
			readDataThreads.add(thread);
			thread.start();
		}
	}
	
	private void readDataFromSlaveAddress(OrchestrationResultDTO orchestrationResult) {
		while (!stopReadingData) {
			final String slaveAddress = orchestrationResult.getMetadata().get(ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS);
			if (readingRequestsCache.isEmpty(slaveAddress)) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.warn("Consumer.writeDataToSlaveAddress: the writing thread does not make a pause.");
				}
				continue;
			}
			readOneDataFromSlaveAddress(orchestrationResult);
			writingRequestsCache.deleteFirstReadRequest(slaveAddress);
		}
	}
	
	private void readOneDataFromSlaveAddress(OrchestrationResultDTO orchestrationResult){
		final String slaveAddress = orchestrationResult.getMetadata().get("slaveAddress");
		ModbusReadRequestDTO request = readingRequestsCache.getFirstReadRequest(slaveAddress);
		final HttpMethod httpMethod = HttpMethod.valueOf(orchestrationResult.getMetadata().get(ConsumerModbusConstants.HTTP_METHOD));
		final String providerAddress = orchestrationResult.getProvider().getAddress();
		final int providerPort = orchestrationResult.getProvider().getPort();
    	final String serviceUri = orchestrationResult.getServiceUri().replace("slaveAddress", slaveAddress);
    	final String interfaceName = orchestrationResult.getInterfaces().get(0).getInterfaceName(); //Simplest way of choosing an interface.
    	final String token = orchestrationResult.getAuthorizationTokens() == null ? 
    			null : orchestrationResult.getAuthorizationTokens().get(getInterface());
    	final String[] queryParamSlaveAddress = 
    		{orchestrationResult.getMetadata().get(ConsumerModbusConstants.REQUEST_PARAM_KEY_SLAVEADDRESS), slaveAddress};
    	ModbusResponseDTO response = arrowheadService.consumeServiceHTTP(ModbusResponseDTO.class, httpMethod, providerAddress, providerPort, serviceUri,
				interfaceName, token, request, queryParamSlaveAddress);
		
    	writeDataToModbusDataCache(response, slaveAddress);
	}
	
	
	private String getInterface() {
    	return sslProperties.isSslEnabled() ? ConsumerModbusConstants.INTERFACE_SECURE : ConsumerModbusConstants.INTERFACE_INSECURE;
    }
	
	private void writeDataToModbusDataCache(ModbusResponseDTO response, String slaveAddress) {
		if (response == null) {
			logger.warn("The cosumer does not get the response from the provider.");
			return;
		}
		if (response.getE().size() == 0) {
			logger.warn("The provider does not work properly! The consumer does not get the modbus data!");
			return;
		}
		ModbusData modbusData = response.getE().get(0);
		dataCache.setCoils(slaveAddress, modbusData.getCoils());
		dataCache.setDiscreteInputs(slaveAddress, modbusData.getDiscreteInputs());
		dataCache.setHoldingRegisters(slaveAddress, modbusData.getHoldingRegisters());
		dataCache.setInputRegisters(slaveAddress, modbusData.getInputRegisters());
		logger.info("Provider response");
	}
}
