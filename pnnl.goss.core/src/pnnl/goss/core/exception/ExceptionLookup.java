package pnnl.goss.core.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;

import com.northconcepts.exception.ConnectionCode;
import com.northconcepts.exception.ErrorCode;
import com.northconcepts.exception.ErrorText;

@Component
public class ExceptionLookup implements ErrorText{
	
	private Map<String, String> lookupMap;
	
	private void initialize(){
		if (lookupMap != null) return;
		
		lookupMap = new HashMap<>();
		
		lookupMap.put(getKey(ConnectionCode.class, ConnectionCode.SESSION_ERROR),
				"Could not create a valid session");
		
	}
	
	@Start
	public void start(){
		initialize();
	}
	
	@Stop
	public void stop() {
		lookupMap.clear();
		lookupMap = null;
	}
	
	
	private String getKey(Class<? extends ErrorCode> codeClass, ErrorCode code){
		return codeClass.getSimpleName()+"__"+code;
	}
	
	@Override
	public String getText(ErrorCode code) {
		String key = getKey(code.getClass(), code);
		return Optional.ofNullable((String)lookupMap.get(key))
				.orElse("An unknown error code: " + code+ "dedtected") ;
	}
}
