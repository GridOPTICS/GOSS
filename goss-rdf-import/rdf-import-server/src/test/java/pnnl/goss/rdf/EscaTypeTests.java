package pnnl.goss.rdf;

import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.*;
import pnnl.goss.rdf.impl.EscaTypeImpl;

import com.hp.hpl.jena.rdf.model.Resource;

import static org.mockito.Mockito.*;

public class EscaTypeTests {
	
	@Test
	public void addingDirectLinksAddsABackReference(){
		Resource res = mock(Resource.class);
		EscaType linked = mock(EscaType.class);
		
		when(linked.getDataType()).thenReturn("Terminal");
		when(linked.getResource()).thenReturn(res);
		when(res.getLocalName()).thenReturn("Terminal");
								
		EscaType original = new EscaTypeImpl(res, "Terminal", "M1");
		original.addDirectLink("property", linked);
		
		verify(linked, atLeastOnce()).addRefersToMe(original);
		
		boolean found = false;
		for(EscaType t: original.getDirectLinkedResources(linked.getResource())){
			if (t.equals(linked)){
				found = true;
				break;
			}
		}
		assertTrue("Wasn't found", found);
		
	}

}
