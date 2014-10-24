package pnnl.goss.rdf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.rdf.EscaType;
import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An EscaType is a generic class for containing information about the different
 * rdf classes.  Each EscaType contains a unique mrid.  All {@link Resource}  are
 * defined in {@link Esca60Vocab}.
 * 
 * @author Craig Allwardt
 *
 */
public class DefaultEscaType extends AbstractEscaType {
	
	private static Logger log = LoggerFactory.getLogger(DefaultEscaType.class);
	
		
//	public DefaultEscaType(EscaType copy){
//		this(copy.getResource(), copy.getDataType(), copy.getMrid());
//	}

//	private DefaultEscaType(Resource resource, String dataType, String mrid){
//		this.resource = resource;
//		this.dataType = dataType;
//		this.mrid = mrid;
//	}
	
	private DefaultEscaType(){
	}
	
	
	public static AbstractEscaType construct(Resource resource, String dataType, String mrid){
		AbstractEscaType escaType = null;

		// Must match up with the order in the switch statement below.
		String[] dataTypeList = {Esca60Vocab.CONNECTIVITYNODE_OBJECT.getLocalName(),
				Esca60Vocab.TERMINAL_OBJECT.getLocalName()
		};
		
		int i= 0;
		for(i=0; i<dataTypeList.length; i++){
			if (dataTypeList[i].equals(dataType)){
				break;
			}
		}
		
		
		switch(i){
		case 0:
			escaType = new ConnectivityNode();
			break;
		case 1:
			escaType = new Terminal();
			break;
		 default:
			 escaType = new DefaultEscaType(); 
		}
		
		escaType.dataType = dataType;
		escaType.mrid = mrid;
		escaType.resource = resource;

		return escaType;
	}

	
	
}
