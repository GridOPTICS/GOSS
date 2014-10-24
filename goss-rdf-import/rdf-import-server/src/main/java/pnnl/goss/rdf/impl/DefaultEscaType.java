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

	private DefaultEscaType(Resource resource, String dataType, String mrid){
		this.resource = resource;
		this.dataType = dataType;
		this.mrid = mrid;
	}
	
	
	
	public static AbstractEscaType construct(Resource resource, String dataType, String mrid){
		AbstractEscaType escaType = null;
		
		if(dataType.equals(Esca60Vocab.CONNECTIVITYNODE_OBJECT.getLocalName())){
			escaType = new ConnectivityNode();
			escaType.dataType = dataType;
			escaType.mrid = mrid;
			escaType.resource = resource;
		}
		else{
			escaType = new DefaultEscaType(resource, dataType, mrid);
		}
		
		return escaType;
	}

	
	
}
