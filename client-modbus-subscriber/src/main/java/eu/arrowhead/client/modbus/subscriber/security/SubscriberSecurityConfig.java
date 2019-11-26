package eu.arrowhead.client.modbus.subscriber.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import eu.arrowhead.client.library.config.DefaultSecurityConfig;
import eu.arrowhead.client.library.util.ClientCommonConstants;
import eu.arrowhead.common.CommonConstants;

@Configuration
@EnableWebSecurity
public class SubscriberSecurityConfig extends DefaultSecurityConfig {
	
	//=================================================================================================
	// members
	
	@Value(ClientCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
	private boolean tokenSecurityFilterEnabled;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	private SubscriberTokenSecurityFilter tokenSecurityFilter;
	private SubscriberNotificationAccessControlFilter notificationFilter;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void configure( final HttpSecurity http) throws Exception {
		super.configure(http);
			
		notificationFilter = new SubscriberNotificationAccessControlFilter();
		http.addFilterBefore( notificationFilter , SecurityContextHolderAwareRequestFilter.class );
		
		if (sslEnabled && tokenSecurityFilterEnabled) {
			tokenSecurityFilter = new SubscriberTokenSecurityFilter();
			http.addFilterAfter(tokenSecurityFilter, SecurityContextHolderAwareRequestFilter.class);			
		}
	}

	//-------------------------------------------------------------------------------------------------
	public SubscriberTokenSecurityFilter getTokenSecurityFilter() {
		return tokenSecurityFilter;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SubscriberNotificationAccessControlFilter getNotificationFilter() {
		return notificationFilter;
	}
	
}
