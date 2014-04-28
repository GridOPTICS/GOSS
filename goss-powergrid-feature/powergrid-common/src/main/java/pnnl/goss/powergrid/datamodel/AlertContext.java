package pnnl.goss.powergrid.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AlertContext implements Serializable {
	
	private static final long serialVersionUID = 941584033189627086L;
	private HashSet<AlertContextItem> alertContextItems = new HashSet<AlertContextItem>();
	
	public void addContextElement(AlertContextItem newItem){
		alertContextItems.add(newItem);
	}
	
	public List<AlertContextItem> getContextItems(){
		return Collections.unmodifiableList(new ArrayList<AlertContextItem>(alertContextItems));
	}	
	
	public void setContextItems(List<AlertContextItem> items){
		alertContextItems.clear();
		alertContextItems.addAll(items);
	}
}
