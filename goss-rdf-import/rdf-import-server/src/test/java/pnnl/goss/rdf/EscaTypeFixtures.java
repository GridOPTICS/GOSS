package pnnl.goss.rdf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import pnnl.goss.rdf.impl.EscaTypeImpl;
import pnnl.goss.rdf.impl.EscaTypes;
import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class is meant to support the building of networks/mock networks for use
 * within the NetworkTests class and other testing classes.
 * 
 * @author Craig Allwardt
 *
 */
public class EscaTypeFixtures {
	
	private static List<EscaType> createEscaTypes(Resource res, String[] mrids){
		List<EscaType> types = new ArrayList<>();
		
		for(int i=0; i<mrids.length; i++){
			EscaType t=new EscaTypeImpl(res, res.getLocalName(), mrids[i]);
			types.add(t);
		}
		
		return types;
	}
	
	private static void addToEscaTypes(EscaTypes types, List<EscaType> escaTypes){
		for(EscaType t: escaTypes){
			types.put(t.getMrid(), t);
		}
	}
	
	private static void addDirectLinks(EscaTypes types, String property, String fromMrid, String[] toMrids){
		EscaType type = types.get(fromMrid);
		
		for(int i=0; i<toMrids.length; i++){
			type.addDirectLink(property, types.get(toMrids[i]));
		}
	}
	
	private static void addDirectLink(EscaTypes types, String mridFrom, String mridTo, String propertyName){
		EscaType fromEsca = types.get(mridFrom);
		EscaType toEsca = types.get(mridTo);
		fromEsca.addDirectLink(propertyName, toEsca);
	}

	/**
	 * Describe the nodes that are getting sent back.
	 * @return
	 */
	public static EscaTypes getEsca6Node(){
		
		String[] terminals = {"_597196580683024629_t",
									"_5986050453675674768", 
									"_8231210467179973538", 
									"_4963536783062858854"};
		
		String[] breakers = {"_1278064856843146094", "_8425939105332198299"};
		String[] connectivityNodes = {"_3705119779910468614"};
		String[] voltageLevels={"_5279025887447170952"};
		
		EscaTypes types = new EscaTypes();
		// Terminal Resource
		Resource tRes = mock(Resource.class);
		when(tRes.getLocalName()).thenReturn("Terminal");
		// Breaker Resource
		Resource bRes = mock(Resource.class);
		when(bRes.getLocalName()).thenReturn("Breaker");
		// ConnectivityNode Resource
		Resource cnRes = mock(Resource.class);
		when(cnRes.getLocalName()).thenReturn(Esca60Vocab.TERMINAL_CONNECTIVITYNODE.getLocalName());
		// VoltageLevel Resource
		Resource vlRes = mock(Resource.class);
		when(vlRes.getLocalName()).thenReturn("VoltageLevel");
		
		// Link the terminals to the connectivity node directly
		addDirectLink(types, "_597196580683024629_t", "_3705119779910468614", Esca60Vocab.TERMINAL_CONNECTIVITYNODE.getLocalName());
		addDirectLink(types, "_5986050453675674768",  "_3705119779910468614", Esca60Vocab.TERMINAL_CONNECTIVITYNODE.getLocalName());
		addDirectLink(types, "_8231210467179973538",  "_3705119779910468614", Esca60Vocab.TERMINAL_CONNECTIVITYNODE.getLocalName());
		addDirectLink(types, "_4963536783062858854",  "_3705119779910468614", Esca60Vocab.TERMINAL_CONNECTIVITYNODE.getLocalName());
		
		// Link from terminal to the breaker
		addDirectLink(types, "_5986050453675674768", "_8425939105332198299", Esca60Vocab.TERMINAL_CONDUCTINGEQUIPMENT.getLocalName());
				
		
		addToEscaTypes(types, createEscaTypes(tRes, terminals));
		addToEscaTypes(types, createEscaTypes(cnRes, connectivityNodes));
		addToEscaTypes(types, createEscaTypes(vlRes, voltageLevels));
		addToEscaTypes(types, createEscaTypes(bRes, breakers));
		
		
		
				
		return types;
	}
}
