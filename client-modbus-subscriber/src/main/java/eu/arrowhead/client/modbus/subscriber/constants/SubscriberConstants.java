package eu.arrowhead.client.modbus.subscriber.constants;

public class SubscriberConstants {
	//=================================================================================================
	// members
	public static final String MODBUS_DATA_URI = "/" + "modbusData";
	
	public static final String DEFAULT_EVENT_NOTIFICATION_BASE_URI = "/notify";
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SubscriberConstants() {
		throw new UnsupportedOperationException();
	}
}
