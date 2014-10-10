package pnnl.goss.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.impl.EscaTypeImpl;
import pnnl.goss.rdf.server.Esca60Vocab;

/**
 * The goal of this class is to be able to group differing VoltageLevel classes
 * under substation elements.
 * 
 * @author D3M614
 *
 */
public class VoltageLevelGrouper {

	private static Logger log = LoggerFactory.getLogger(VoltageLevelGrouper.class);
	private EscaType substation;
	private Map<String, EscaType> voltageLevels = new HashMap<>();
	
	public VoltageLevelGrouper(EscaType substation){
		this.substation = substation;
		//this.buildVoltageLevelGroups();
	}
	
	public Collection<EscaType> getVoltageLevelObjects(){
		return Collections.unmodifiableCollection(voltageLevels.values());
	}
	
	public Collection<Double> getVoltageLevels(){
		List<Double> nominalLevels = new ArrayList<>();
		for(EscaType t: voltageLevels.values()){
			for (EscaType b: t.getDirectLinkedResources(Esca60Vocab.BASEVOLTAGE_OBJECT)){
				nominalLevels.add(Double.parseDouble(b.getLiteralValue(Esca60Vocab.BASEVOLTAGE_NOMINALVOLTAGE).toString()));
			}
		}
		return Collections.unmodifiableCollection(nominalLevels);
	}
	
	public EscaType getSubstation(){
		return substation;
	}
	
	public void addVoltageLevel(EscaType vl){
		voltageLevels.put(vl.getMrid(), vl);
	}
	
//	private void buildVoltageLevelGroups(){
//		for(EscaType t: substation.getLinks().values()){
//			log.debug("Substation linked to: " + t.getDataType());
//			
//			if (t.getDataType().equals(Esca60Vocab.VOLTAGELEVEL_OBJECT.getLocalName())){
//				voltageLevels.put(t.getMrid(), t);
//			}
//		}
//	}
}
