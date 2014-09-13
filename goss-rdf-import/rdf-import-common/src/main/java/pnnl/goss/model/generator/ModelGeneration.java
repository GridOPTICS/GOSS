package pnnl.goss.model.generator;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
	 * Creates a package directory under ROOT_FOLDER.
	 * 
	 * If the folder already exists then it is not created.
	 * 
	 * @param classPackage A package that shoudl be created.
	 */
	public static void createPackageDir(String classPackage){
		System.out.println("Creating package: "+classPackage);
		String packageDir = classPackage.replace(".", "/");
		Path dir = Paths.get(ROOT_FOLDER, packageDir);

		// Already exists
		if (dir.toFile().isDirectory()){
			return;
		}
		
		if (!dir.toFile().mkdirs()){
			System.out.println("Failed to create: "+dir.toAbsolutePath());
		}
	}
	
	/**
	 * Generates a class
	 * 
	 * @param classPackage the package the generated class will belong to.
	 * @param className the name of the class to be generated.
	 * @param extendsClass a baseclass if necessary (null or "" if not)
	 * @param attributeTypeMap a Map of attribute names -> datatype (full package notation).
	 */
	public static void createClassFile(String classPackage, String className,
			String extendsClass, Map<String, String> attributeTypeMap){
		
	}

	/**
	 * Starts the generation of the models that are in the xls file
	 * 
	 * @param existingFile The downloaded xls file from ERCOT.
	 */
	public static void generateModels(File existingFile){
		System.out.println("Generating models ...");
		createPackageDir("pnnl.goss.cim");
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
