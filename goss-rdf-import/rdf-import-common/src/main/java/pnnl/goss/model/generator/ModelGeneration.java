package pnnl.goss.model.generator;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ModelGeneration {
	
	/**
	 * All generated classes will be under this root.
	 */
	private static final String ROOT_PACKAGE = "pnnl.goss.cim";
	
	/**
	 * The root of the java source folder.
	 */
	private static final String ROOT_FOLDER = "src/main/java";

	/**
	 * Starts the generation of the models that are in the xls file
	 * 
	 * @param existingFile The downloaded xls file from ERCOT.
	 */
	public static void generateModels(File existingFile){
		System.out.println("Generating models ...");
		
		System.out.println("Generation complete");
	}
	
	public static void main(String[] args) throws URISyntaxException {
		
		// Root of the resources path at src/main/resources
		URL url = ModelGeneration.class.getClassLoader().getResource("ERCOT_DataDictionary_1.7.xls");
		File file = new File(url.toURI());
		if (file.exists()){
			generateModels(file);
		}

	}

}
