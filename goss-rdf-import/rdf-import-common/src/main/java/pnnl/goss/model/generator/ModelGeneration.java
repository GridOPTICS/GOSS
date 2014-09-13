package pnnl.goss.model.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
	 * @throws IOException 
	 */
	public static void generateModels(File existingFile) throws IOException{
		System.out.println("Generating models ...");
		HSSFWorkbook wb = readFile(existingFile);

		System.out.println("Data dump:\n");

		for (int k = 0; k < wb.getNumberOfSheets(); k++) {
			HSSFSheet sheet = wb.getSheetAt(k);
			int rows = sheet.getPhysicalNumberOfRows();
			System.out.println("Sheet " + k + " \"" + wb.getSheetName(k) + "\" has " + rows
					+ " row(s).");
			for (int r = 0; r < rows; r++) {
				HSSFRow row = sheet.getRow(r);
				if (row == null) {
					continue;
				}

				int cells = row.getPhysicalNumberOfCells();
				System.out.println("\nROW " + row.getRowNum() + " has " + cells
						+ " cell(s).");
				for (int c = 0; c < cells; c++) {
					HSSFCell cell = row.getCell(c);
					String value = null;
					
					if(cell == null){
						System.out.println("CELL col (c)=" + c + " is null!");
						continue;
					}

					switch (cell.getCellType()) {

						case HSSFCell.CELL_TYPE_FORMULA:
							value = "FORMULA value=" + cell.getCellFormula();
							break;

						case HSSFCell.CELL_TYPE_NUMERIC:
							value = "NUMERIC value=" + cell.getNumericCellValue();
							break;

						case HSSFCell.CELL_TYPE_STRING:
							value = "STRING value=" + cell.getStringCellValue();
							break;

						default:
					}
					
					System.out.println("CELL col=" + cell.getColumnIndex() + " VALUE="
								+ value);
					
				}
			}
		}
		createPackageDir("pnnl.goss.cim");
		System.out.println("Generation complete");
	}
	
	/**
	 * creates an {@link HSSFWorkbook} the specified OS filename.
	 */
	private static HSSFWorkbook readFile(File xlsFile) throws IOException {
		return new HSSFWorkbook(new FileInputStream(xlsFile));
	}
	
	public static void main(String[] args) throws URISyntaxException, IOException {
		
		// Root of the resources path at src/main/resources
		URL url = ModelGeneration.class.getClassLoader().getResource("ERCOT_DataDictionary_1.7.xls");
		File file = new File(url.toURI());
		if (file.exists()){
			generateModels(file);
		}

	}

}
