package pnnl.goss.core.server.tester;

import java.util.HashSet;
import java.util.Set;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.Request;
import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.server.tester.requests.EchoBlacklistCheckRequest;

@Component
public class EchoBlacklistedWordsHandler implements AuthorizationHandler {
	
	private final Set<String> wordSet = new HashSet<>();
	
	public EchoBlacklistedWordsHandler() {
		wordSet.add("This");
		wordSet.add("That");
		wordSet.add("Code");
	}

	@Override
	public boolean isAuthorized(Request request, Set<String> permissions) {
		
		EchoBlacklistCheckRequest echo = (EchoBlacklistCheckRequest) request;
		
		if (!permissions.contains("words:all")) {
			
			for (String word: wordSet){
				if (echo.getMessage().toUpperCase().contains(word.toUpperCase())){
					System.out.println("Message cannot contain word: " + word);
					return false;
				}
			}			
		}
		
		return true;
	}
}
