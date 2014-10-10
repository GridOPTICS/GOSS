package pnnl.goss.rdf;

import static org.mockito.Mockito.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.Resource;

import pnnl.goss.rdf.impl.EscaTypes;

public class EscaTypesTests {

	@Test
	public void afterAddingElementsCanRetrieveByResourceType(){
		
		Resource cnRes = mock(Resource.class);
		Resource tRes = mock(Resource.class);
		
		when(cnRes.getLocalName()).thenReturn("ConnectivityNode");
		when(tRes.getLocalName()).thenReturn("Terminal");
		
		EscaType cn1 = mock(EscaType.class);
		EscaType cn2 = mock(EscaType.class);
		EscaType t1 = mock(EscaType.class);
		
		when(cn1.getMrid()).thenReturn("cn1");
		when(cn2.getMrid()).thenReturn("cn2");
		when(cn1.getDataType()).thenReturn("ConnectivityNode");
		when(cn2.getDataType()).thenReturn("ConnectivityNode");
		
		when(t1.getDataType()).thenReturn("Terminal");
		
		// Now to the behavior tests.
		EscaTypes types = new EscaTypes();
		
		types.put(cn1.getMrid(), cn1);
		types.put(cn2.getMrid(), cn2);
		types.put(t1.getMrid(), t1);
		
		assertEquals(2, types.where(cnRes).size());
		assertEquals(1, types.where(tRes).size());
		
		assertTrue(types.where(tRes).contains(t1));
		assertTrue(types.where(cnRes).contains(cn1));
		assertTrue(types.where(cnRes).contains(cn2));		
	}
	
	@Test
	public void typesAreReturnedCorrectly(){
		
		EscaTypes types = new EscaTypes();
		
		for(int i=0; i<5; i++){
			EscaType t = mock(EscaType.class);
			when(t.getDataType()).thenReturn("Terminal");
			types.put("T"+i, t);
		}
		
		assertTrue(types.types().contains("Terminal"));
		for(int i=0; i<5; i++){
			EscaType t = mock(EscaType.class);
			when(t.getDataType()).thenReturn("Breaker");
			types.put("B"+i, t);
		}
		assertTrue(types.types().contains("Breaker"));
		assertEquals(2, types.types().size());
	}
	
}
