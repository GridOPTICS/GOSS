package pnnl.goss.model.generator;

import java.net.URI;
import java.net.URL;

public class ModelGeneration {

	
	public static void main(String[] args) {
		
		// Root of the resources path at src/main/resources
		URL uri = ModelGeneration.class.getClassLoader().getResource("ERCOT_DataDictionary_1.7.xls");
		System.out.println(uri.toString());

	}

}
