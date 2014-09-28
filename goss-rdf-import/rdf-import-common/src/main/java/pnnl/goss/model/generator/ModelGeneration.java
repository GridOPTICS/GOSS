package pnnl.goss.model.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ModelGeneration implements FindDataType, FindClass {
	
	/**
	 * All generated classes will be under this root.
	 */
	public static final String ROOT_PACKAGE = "pnnl.goss.nb";
	
	/**
	 * The root of the java source folder.
	 */
	public static final String ROOT_FOLDER = "src/main/java";
		
	public enum DataTypeSheets{
		DataType,
		DataTypeAndUnits,
		DataTypeEnums
	}
	
	/**
	 * Maps the name (Equipment) to type (pnnl.goss.cim.core.Equipment)
	 */
	private static Map<String, String> classNameToType = new HashMap<>();
	/**
	 * Maps the packaged namespace class to the metaclass 
	 */
	private static Map<String, MetaClass> metaClasses = new HashMap<>();
	/**
	 * Maps the datatype name to a metadatatype structure.
	 */
	private static Map<String, MetaDataType> metaDataTypes = new HashMap<>();
	
	
	/**
	 * Creates a package directory under ROOT_FOLDER.
	 * 
	 * If the folder already exists then it is not created.
	 * 
	 * @param classPackage A package that should be created.
	 * @return The directory that was attempted to make
	 */
	private static String createPackageDir(String classPackage){
		System.out.println("Creating package: "+classPackage);
		String packageDir = classPackage.replace(".", "/");
		Path dir = Paths.get(ROOT_FOLDER, packageDir);

		// Already exists
		if (dir.toFile().isDirectory()){
			return dir.toAbsolutePath().toString();
		}
		
		if (!dir.toFile().mkdirs()){
			System.out.println("Failed to create: "+dir.toAbsolutePath());
			return null;
		}
		return dir.toString(); //.toAbsolutePath().toString();
	}
	
	
	
	/**
	 * Adds the meta-classes super class + for all classes in the  xls sheet.
	 * 
	 * @param classesSheet
	 */
//	private static void addInheritancePath(HSSFSheet classesSheet){
//		// First row is header
//		for(int r=1; r < classesSheet.getPhysicalNumberOfRows(); r++){
//			HSSFRow row = classesSheet.getRow(r);
//			if (row == null) {
//				continue;
//			}
//			
//			
//			
//			HSSFCell classCell = row.getCell(CLASSES_CLASS_COLUMN); //.getStringCellValue()
//			if (classCell != null && classCell.getStringCellValue()!= null){
//				String className = classCell.getStringCellValue();
//				if (classNameToType.containsKey(className)){
//					// Pull the meta class out and set the superclass to the first
//					// item in the Inheritance Path list.
//					MetaClass meta = metaClasses.get(classNameToType.get(className));
//					String inherString = row.getCell(CLASSES_INHERITANCE_COLUMN).getStringCellValue();
//					String extendsClassName = inherString.split(";")[0];
//					meta.setExtendsType(classNameToType.get(extendsClassName));
//					
//				}
//				
//			}
//		}
//	}
	
	
	
	/**
	 * Populates the internal metaDataTypes map from the DataTypes tab, 'DataType Enums, or the 
	 * 'DataTypes - Value&Unit' tab.  
	 * 
	 * @param dataTypeSheet
	 * @param sheetType
	 */
	private static void createMetaDataTypes(HSSFSheet dataTypeSheet, DataTypeSheets sheetType){
		// First row is header
		for(int r=1; r < dataTypeSheet.getPhysicalNumberOfRows(); r++){
			HSSFRow row = dataTypeSheet.getRow(r);
			MetaDataType dataType = MetaDataType.create(row,  sheetType);
			if (dataType != null){
				if (!metaDataTypes.containsKey(dataType.getDataTypeName())){
					metaDataTypes.put(dataType.getDataTypeName(), dataType);
				}
				else{
					MetaDataType dataTypeInCollection = metaDataTypes.get(dataType.getDataTypeName());
					if (dataTypeInCollection.isEnumeration()){
						if (!dataTypeInCollection.containsEnumeratedValue(dataType.getEnumeratedValues().get(0))){
							dataTypeInCollection.addEnumeratedValue(dataType.getEnumeratedValues().get(0));
						}
						else{
							System.err.println("Already has enumerated value type: "+dataType.getEnumeratedValues().get(0));
						}
					}
					else{
						System.out.println("Contains "+dataType.getDataTypeName() +" already.");
					}
				}
			}
		}		
	}
	
	/**
	 * Creates the meta-classes from the classes sheet.  After this method
	 * is called all of the classes in the object model will be in the
	 * metaClasses Map by datatype.  Each class name will also be in the
	 * classNameToDataType map for easy retrieval and reference.dataTypeShee
	 * 
	 * @param classesSheet
	 */
	private static void createMetaClasses(HSSFSheet classesSheet){
		// First row is header
		for(int r=1; r < classesSheet.getPhysicalNumberOfRows(); r++){
			HSSFRow row = classesSheet.getRow(r);
			if (row == null) {
				continue;
			}
			
			MetaClass cls = MetaClass.create(row);
			
			if (!metaClasses.containsKey(cls.getPackageAndClass())){
				metaClasses.put(cls.getPackageAndClass(), cls);
			}
			
			
//			//HSSFCell pkgCell = row.getCell(CLASSES_PACKAGE_COLUMN);
//			HSSFCell pkgCell = row.getCell(CLASSES_NAMESPACE_COLUMN);
//			if (pkgCell != null){
//				if (pkgCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
//					System.out.println("Skipping: " +pkgCell.getNumericCellValue());
//				}
//				else if(pkgCell.getCellType() == HSSFCell.CELL_TYPE_STRING){
//					// Use this as a test so that we can get the mapped type and see
//					// if it's already in the collection (Hopefully it never will!)
//					MetaClass tmp = new MetaClass();
//					tmp.setPackageName(getPackage(pkgCell.getStringCellValue()));
//					tmp.setClassName(row.getCell(CLASSES_CLASS_COLUMN).getStringCellValue());
//					
//					if (!tmp.isValidClassDefinition()){
//						System.out.println("Invalid class detected for row: "+ r);
//						continue;
//					}
//					
//					// Store the metaclass in our hashmap of classes if necessary.
//					MetaClass newMetaClass = metaClasses.get(tmp.getPackageAndClass());
//					if (newMetaClass == null){
//						newMetaClass = tmp;
//						metaClasses.put(newMetaClass.getPackageAndClass(), newMetaClass);
//						classNameToType.put(newMetaClass.getClassName(), newMetaClass.getPackageAndClass());
//					}
//					else{
//						System.out.println("Boo already has the datatype!! "+newMetaClass.getPackageAndClass());
//					}
//					
//					HSSFCell docCell = row.getCell(CLASSES_DOCUMENTATION_COLUMN);
//					if (docCell != null && docCell.getStringCellValue() != null){
//						newMetaClass.setClassDocumentation(docCell.getStringCellValue());
//					}
//				}
//			}
		}
		
		// Now that all of the classes have been pulled out of the worksheet we can
		// attempt to add the rest of the information for inheritance etc.
		//addInheritancePath(classesSheet);
	}
	
	private void createAttributes(HSSFSheet attribSheet){
		// First row is header
		for(int r=1; r < attribSheet.getPhysicalNumberOfRows(); r++){
			HSSFRow row = attribSheet.getRow(r);
			if (row == null) {
				continue;
			}
			
			MetaAttribute attrib = MetaAttribute.create(row, this, this);
			
//			HSSFCell classCell = row.getCell(ATTRIB_CLASS_COLUMN);
//			HSSFCell attribCell = row.getCell(ATTRIB_ATTRIBUTE_COLUMN);
//			HSSFCell dataTypeCell = row.getCell(ATTRIB_DATA_TYPE_COLUMN);
//			HSSFCell documentationCell = row.getCell(ATTRIB_DOCUMENTATION_COLUMN);
//			
//			if (classCell == null || classCell.getStringCellValue() == null ||
//					attribCell == null || attribCell.getStringCellValue() == null ||
//					dataTypeCell == null || dataTypeCell.getStringCellValue() == null){
//				continue;
//			}
//			
//			String className = classCell.getStringCellValue();
//			if (classNameToType.containsKey(className)){
//				MetaClass metaClass = metaClasses.get(classNameToType.get(className));
//				MetaAttribute newAttrib = new MetaAttribute();
//				
//				newAttrib.setAttributeName(attribCell.getStringCellValue());
//				String dataType = dataTypeCell.getStringCellValue();
//				if (metaDataTypes.get(dataType) != null){
//					newAttrib.setDataType(metaDataTypes.get(dataType));
//				}
//				else{
//					// Handle the case where the dataType is not an enumeration
//					// nor isn't in the DataType's sheet in the ercot xls.
//					if (classNameToType.get(dataType) != null){
//						MetaDataType metaData = new MetaDataType(metaClass);
//						newAttrib.setDataType(metaData);						
//					}
//					else{
//						// Handle the case where Enum is prefixed on the attributes sheet, 
//						// however on the DataTypes sheet the enums aren't prefixed.
//						if (dataType.startsWith("Enum")){
//							dataType = dataType.substring("Enum".length());
//							if (metaDataTypes.get(dataType) != null){
//								newAttrib.setDataType(metaDataTypes.get(dataType));
//								newAttrib.setDataTypePackage(getEnumerationPackage());
//							}
//							else{
//								MetaDataType dt = new MetaDataType();
//								dt.setDataTypeName(dataType);
//								dt.setEnumeration(true);
//								dt.setJavaPackage(getEnumerationPackage());
//								newAttrib.setDataType(dt);
//								newAttrib.setDataTypePackage(getEnumerationPackage());
//								metaDataTypes.put(dataType, dt);
//							}
//						}
//						else{
//							try{
//								MetaDataType metaData = new MetaDataType(dataType);
//								newAttrib.setDataType(metaData);
//							}
//							catch(IllegalArgumentException e){
//								// Handle the case temporarily that the datatype isn't
//								// specified in the DataType listing by auto creating
//								// with a float datatype.
//								MetaDataType metaData = new MetaDataType();
//								metaData.setDataTypeName(dataType);
//								metaData.setNamespace("cim");
//								metaData.setValueType("Float");
//								System.out.println("AUTOCREATING: "+dataType);
//								newAttrib.setDataType(metaData);
//							}
//						}
//					}					
//				}
//				
//				if (classNameToType.get(dataType) == null){
//					System.out.println("null package for datatype '"+dataType+"'");
//				}
//				else{
//					newAttrib.setDataTypePackage(classNameToType.get(dataType));
//				}
//				
//				if (documentationCell != null){
//					newAttrib.setDocumentation(documentationCell.getStringCellValue());
//				}
//				
//				metaClass.addAttribute(newAttrib);
//			}
		}
	}

	/**
	 * Starts the generation of the models that are in the xls file
	 * 
	 * @param existingFile The downloaded xls file from ERCOT.
	 * @throws IOException 
	 */
	private void generateModels(File existingFile) throws IOException{
		System.out.println("Generating models ...");
		HSSFWorkbook wb = readFile(existingFile);
		
		createMetaDataTypes(wb.getSheet("DataTypes"), DataTypeSheets.DataType);
		createMetaDataTypes(wb.getSheet("DataTypes - Value&Unit"), DataTypeSheets.DataTypeAndUnits);
		createMetaDataTypes(wb.getSheet("DataTypes - Enum"), DataTypeSheets.DataTypeEnums);
		createMetaClasses(wb.getSheet("Classes"));		
		createAttributes(wb.getSheet("Attributes"));
			
		// Loop and make .java files from the class meta files.
		for(MetaClass meta: metaClasses.values()){
			String packageDir = createPackageDir(meta.getPackageName());
			File classFile = new File(packageDir);
			
			try{
				String fullJavaFilePath = classFile.toString() + "/" + meta.getClassName()+".java";
				System.out.println("Creating java file: "+fullJavaFilePath);
				FileWriter writer = new FileWriter(fullJavaFilePath);
				BufferedWriter out = new BufferedWriter(writer);
				out.write(meta.getClassDefinition());
				out.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		// Loop and make .java files from the class meta files.
		for(MetaDataType meta: metaDataTypes.values()){
			if (meta.isEnumeration()){
				String packageDir = createPackageDir(MetaDataType.getEnumerationPackage());
				File classFile = new File(packageDir);
				
				try{
					if (!meta.getEnumeratedValues().isEmpty()){
						String fullJavaFilePath = classFile.toString() + "/" + meta.getDataTypeName()+".java";
						System.out.println("Creating java enumeration file: "+fullJavaFilePath);
						FileWriter writer = new FileWriter(fullJavaFilePath);
						BufferedWriter out = new BufferedWriter(writer);
						out.write(meta.getEnumeration());
						out.close();
					}
					else{
						System.out.println("There aren't any values for enumeration: "+meta.getDataTypeName());
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("Generation complete");
	}
	
	/**
	 * creates an {@link HSSFWorkbook} the specified OS filename.
	 */
	private static HSSFWorkbook readFile(File xlsFile) throws IOException {
		return new HSSFWorkbook(new FileInputStream(xlsFile));
	}
	
	private static String tabifyLines(String data, String tabs){
		StringBuffer buf = new StringBuffer();
		
		for(String line : data.split("\n")){
			buf.append(tabs + line + "\n");
		}
		
		return buf.toString();
	}
	
	private void writeOutputFiles(File classesFile, File dataTypesFile, File classNameFile){
		FileWriter writer;
		try {
			writer = new FileWriter(classesFile);
			BufferedWriter out = new BufferedWriter(writer);
			
			for(MetaClass cls: metaClasses.values()){
				out.write(cls.toString()+"\n");
				for(MetaAttribute attr: cls.getAttributes()){
					out.write(tabifyLines(attr.toString(), "\t"));
				}
				out.write("\n");
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			writer = new FileWriter(classNameFile);
			BufferedWriter out = new BufferedWriter(writer);
			ArrayList<String> clsNames = new ArrayList<>();
			for(MetaClass cls: metaClasses.values()){
				clsNames.add(cls.getClassName());
			}
			java.util.Collections.sort(clsNames);
			for(String s: clsNames){
				out.write(s+"\n");
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			writer = new FileWriter(dataTypesFile);
			BufferedWriter out = new BufferedWriter(writer);
			
			for(MetaDataType dt: metaDataTypes.values()){
				out.write(dt.toString()+"\n");
			}
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws URISyntaxException, IOException {
		
		// Root of the resources path at src/main/resources
		URL xlsFileUri = ModelGeneration.class.getClassLoader().getResource("ERCOT_DataDictionary_1.7.xls");
		File xlsFile = new File(xlsFileUri.toURI());
		if (xlsFile.exists()){
			ModelGeneration model = new ModelGeneration();
			model.generateModels(xlsFile);
			//generateModels(xlsFile);
			File classesFile = new File("created-classes.txt");
			File classNamesFile = new File("created-class-names.txt");
			File dataTypesFile = new File("created-datatypes.txt");
			File attributesFile = new File("created-attributes.txt");
			
			model.writeOutputFiles(classesFile, dataTypesFile, classNamesFile);
			
		}

	}



	@Override
	public MetaDataType getDataType(String dataTypeName) {
		return metaDataTypes.get(dataTypeName);
	}



	@Override
	public MetaClass getClass(String className) {
		return metaClasses.get(className);
	}

}
