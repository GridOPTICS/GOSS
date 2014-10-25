package pnnl.goss.rdf.impl;

import java.io.InvalidObjectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.server.Esca60Vocab;

public class Terminal extends AbstractEscaType {
	
	private ConnectivityNode connectivityNode;
	private static Logger log = LoggerFactory.getLogger(Terminal.class);
	
	public void setConnectivityNode(ConnectivityNode node) throws InvalidObjectException{
		if(connectivityNode != null){
			throw new InvalidObjectException("ConnectivityNode has already been set!");
		}
		this.connectivityNode = node;
	}
	
	public ConnectivityNode getConnectivityNode(){
		return connectivityNode;
	}
	
	public boolean isEquipmentConnectivityNode(){
		EscaType equipment = getEquipment();
		if (equipment != null && equipment.getDataType().equals(Esca60Vocab.CONNECTIVITYNODE_OBJECT.getLocalName())){
			return true;
		}
		
		return false;
	}
	
	public boolean isEquipmentBreaker(){
		EscaType equipment = getEquipment();
		if (equipment != null && equipment.getDataType().equals(Esca60Vocab.BREAKER_OBJECT.getLocalName())){
			return true;
		}
		
		return false;
	}
	
	public EscaType getEquipment(){
		return getLink(Esca60Vocab.TERMINAL_CONDUCTINGEQUIPMENT);
	}
	
}
