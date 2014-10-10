package pnnl.goss.rdf;

import static org.mockito.Mockito.*;
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

	/**
	 * Describe the nodes that are getting sent back.
	 * @return
	 */
	public static EscaTypes getEsca6Node(){
		
		
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
		
		// Create 10 terminals T1..T10
		for(int i=0; i<10; i++){
			EscaType t = mock(EscaType.class);
			// When caller asks about the resource return the mocked resource for
			// this specific type.
			when(t.getResource()).thenReturn(tRes);
			when(t.getDataType()).thenReturn(t.getResource().getLocalName());
			
			types.put("T"+i+1, mock(EscaType.class));
		}
		
		for(int i=0; i<5; i++){
			EscaType t = mock(EscaType.class);
			
			// When caller asks about the resource return the mocked resource for
			// this specific type.
			when(t.getResource()).thenReturn(bRes);
			when(t.getDataType()).thenReturn(t.getResource().getLocalName());
			
			types.put("B"+i+1,mock(EscaType.class));
		}
		
		for(int i=0; i<5; i++){
			EscaType t = mock(EscaType.class);
			
			// When caller asks about the resource return the mocked resource for
			// this specific type.
			when(t.getResource()).thenReturn(cnRes);
			when(t.getDataType()).thenReturn(t.getResource().getLocalName());
			
			types.put("CN"+i+1,mock(EscaType.class));
		}
		
				
		return types;
	}
}
