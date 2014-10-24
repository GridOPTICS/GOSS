package pnnl.goss.rdf;

import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.*;
import pnnl.goss.rdf.impl.DefaultEscaType;

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
								
		EscaType original = new DefaultEscaType(res, "Terminal", "M1");
		original.addDirectLink("property", linked);
		
		verify(linked, atLeastOnce()).addRefersToMe(original);
		
		assertEquals(1, original.getDirectLinkedResources(linked.getResource()).size());
		assertTrue(original.getDirectLinkedResources(linked.getResource()).contains(linked));
		assertEquals(1, original.getLinks().size());
		assertEquals(original.getLinks().get("property"), linked);		
	}

}
