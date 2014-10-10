package pnnl.goss.rdf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import pnnl.goss.rdf.impl.EscaTypeImpl;
import pnnl.goss.rdf.impl.EscaTypes;

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

	/**
	 * Describe the nodes that are getting sent back.
	 * @return
	 */
	public static EscaTypes getEsca6Node(){
		
		String[] terminalMrids = {"_597196580683024629_t",
				"_5986050453675674768", "_8231210467179973538", "_4963536783062858854"};
		String[] connectivityMrids = {"_3705119779910468614"};
		
		EscaTypes types = new EscaTypes();
		// Terminal Resource
		Resource tRes = mock(Resource.class);
		when(tRes.getLocalName()).thenReturn("Terminal");
		// Breaker Resource
		Resource bRes = mock(Resource.class);
		when(tRes.getLocalName()).thenReturn("Breaker");
		// ConnectivityNode Resource
		Resource cnRes = mock(Resource.class);
		when(tRes.getLocalName()).thenReturn("ConnectivityNode");
		
		addToEscaTypes(types, createEscaTypes(tRes, terminalMrids));
		addToEscaTypes(types, createEscaTypes(cnRes, connectivityMrids));
		
		addDirectLinks(types, "terminal", "_3705119779910468614", terminalMrids);
		
		
				
		return types;
	}
}
