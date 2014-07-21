package pnnl.goss.rdf;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pnnl.goss.rdf.server.Esca60Vocab;

import com.hp.hpl.jena.rdf.model.Model;

public class TestCreatePowergridFromEscaService {

	private File esca60File;
	private Model rdfModel;
	
	@BeforeClass 
	public void setupClass(){
		URL url = Thread.currentThread().getContextClassLoader().getResource("esca60_cim.xml");
		esca60File = new File(url.getPath());
		rdfModel = Esca60Vocab.readModel(esca60File.getAbsoluteFile());
	}
	
	@Before
	public void setup(){
		
	}
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
