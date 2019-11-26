package eu.arrowhead.client.modbus.publisher.constants;

public class PublisherConstants {
	//=================================================================================================
	// members

	public static final String START_INIT_EVENT_PAYLOAD= "InitStarted";
	public static final String START_RUN_EVENT_PAYLOAD= "RunStarted";
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private PublisherConstants() {
		throw new UnsupportedOperationException();
	}
}
