package pnnl.goss.rdf.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;

import pnnl.goss.rdf.EscaType;

public class ProcessableItems extends HashMap<EscaType, Boolean> {

	private static final long serialVersionUID = -6653080663538791183L;
	private static Logger log = LoggerFactory.getLogger(ProcessableItems.class);
	private Map<String, Set<EscaType>> unProcessedTypes = new HashMap<>();
	private Map<String, Set<EscaType>> processedTypes = new HashMap<>();

	public void add(EscaType item){
		this.put(item, false);				
	}
	
	public void addItems(Collection<EscaType> items){
		// Only add items that haven't already been processed.
		for(EscaType item: items){
			if (!containsKey(item)){
				add(item);
			}
		}
	}
	
	public boolean isProcessed(EscaType item){
		Boolean out = get(item);
		if (out == null){
			log.error("Unprocessable item passed!!");
			return false;
		}
		
		return out;
	}
	
	public int countUnProcessed(Resource resource){
		return unProcessedTypes.get(resource.getLocalName()).size();
	}
	
	public int countProcessed(Resource resource){
		return processedTypes.get(resource.getLocalName()).size();
	}
	
	public boolean hasNextUnproccessed(Resource resource){
		String dataType = resource.getLocalName();
		if (unProcessedTypes.containsKey(dataType)){
			return unProcessedTypes.get(dataType).size() > 0;
		}
		else{
			return false;
		}
	}
	
	public EscaType nextUnProcessed(Resource resource){
		if (hasNextUnproccessed(resource)){
			return unProcessedTypes.get(resource.getLocalName()).iterator().next();
		}
		
		return null;
	}
	
	public void setUnProcessed(EscaType type){
		put(type, false);
	}

	public void setProcessed(EscaType type){
		put(type, true);		
	}
	
	@Override
	public Boolean put(EscaType key, Boolean value) {
		Boolean retValue = null;
		
		if(containsKey(key)){
			// Not the same so swap from/to processed and unprocessed
			if (!value.equals(get(key))){
				if (value){
					unProcessedTypes.get(key.getDataType()).remove(key);
					processedTypes.get(key.getDataType()).add(key);
				}
				else{
					processedTypes.get(key.getDataType()).remove(key);
					unProcessedTypes.get(key.getDataType()).add(key);
				}
				
				retValue = value;
			}
			else{
				retValue = !value;
			}
		}
		else {
			if (value == true){
				try {
					throw new Exception("First added items must be false!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
			
			retValue = super.put(key, value);
			
			// Add the type to both unprocessed and processed items so that
			// we can just add the key.
			if(!processedTypes.containsKey(key.getDataType())){
				processedTypes.put(key.getDataType(), new HashSet<EscaType>());
				unProcessedTypes.put(key.getDataType(), new HashSet<EscaType>());
			}
			
			
			if (value){
				processedTypes.get(key.getDataType()).add(key);
			}
			else{
				unProcessedTypes.get(key.getDataType()).add(key);
			}
		}
		
		return retValue;
	}
}
