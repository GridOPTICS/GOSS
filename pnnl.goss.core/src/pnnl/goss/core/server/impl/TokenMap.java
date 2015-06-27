package pnnl.goss.core.server.impl;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.server.TokenIdentifierMap;

@Component
public class TokenMap implements TokenIdentifierMap{
	
	private static final long ONE_MINUTE_IN_MILLIS=60000;
	
	private class MapItem{
		public MapItem(String ipAddress, String token, String identifier){
			this.lastRequested = new Date();
			this.token = token;
			this.ipAddress = ipAddress;
			this.identifier = identifier;
		}
		
		public void updateTime(){
			lastRequested = new Date();
		}		
		public Date lastRequested;
		public String token;
		public String ipAddress;
		public String identifier;
	}
	
	private Map<String, MapItem> registeredTokens = new ConcurrentHashMap<>();
	private int timeoutMinutes = 5;
	
	@Override
	public String registerIdentifier(String ip, String identifier) {
		String token = UUID.randomUUID().toString();
		registerIdentifier(ip, token, identifier);
		return token;
	}

	@Override
	public void registerIdentifier(String ip, String token, String identifier) {
		MapItem item = new MapItem(ip, token, identifier);
		registeredTokens.put(token, item);
	}

	@Override
	public String getIdentifier(String ip, String token) {
		String identifier = null;
		if (isValid(ip, token)){
			identifier = registeredTokens.get(token).identifier;
		}
		return identifier;
	}
	
	private boolean isValid(String ip, String token){
		boolean valid = false;
		
		if (registeredTokens.containsKey(token)){
			MapItem item = registeredTokens.get(token);
			
			if (item.ipAddress.equals(ip) && item.token.equals(token)){
				Date beforeTime = new Date(new Date().getTime() + timeoutMinutes * ONE_MINUTE_IN_MILLIS);
			
				if (item.lastRequested.before(beforeTime)){
					item.updateTime();
					valid = true;
				}			
			}
		}
		
		return valid;
	}
	
	
}
