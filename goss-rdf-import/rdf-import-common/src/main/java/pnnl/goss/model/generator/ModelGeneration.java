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

public class ModelGeneration {
	
	/**
	 * All generated classes will be under this root.
	 */
	private static final String ROOT_PACKAGE = "pnnl.goss.nb";
	
	/**
	 * The root of the java source folder.
	 */
	private static final String ROOT_FOLDER = "src/main/java";
	
	// Classes sheet
	private static final Integer CLASSES_PACKAGE_COLUMN = 0;
	private static final Integer CLASSES_CLASS_COLUMN = 1;
	private static final Integer CLASSES_INHERITANCE_COLUMN = 3;
	private static final Integer CLASSES_NAMESPACE_COLUMN = 4;
	private static final Integer CLASSES_DOCUMENTATION_COLUMN = 5;
	// Attributes sheet
	private static final Integer ATTRIB_CLASS_COLUMN = 2;
	private static final Integer ATTRIB_ATTRIBUTE_COLUMN = 4;
	private static final Integer ATTRIB_DATA_TYPE_COLUMN = 5;
	private static final Integer ATTRIB_INITIAL_VALUE_COLUMN = 6;
	private static final Integer ATTRIB_DOCUMENTATION_COLUMN = 8;
	// DataType sheet
	private static final Integer DATATYPE_PACKAGE_COLUMN = 0;
	private static final Integer DATATYPE_DATA_TYPE_CoLUMN = 1;
	private static final Integer DATATYPE_NS_CoLUMN = 2;
	private static final Integer DATATYPE_DOCUMENTATION_CoLUMN = 3;
	// DataType Value & Unit sheet
	private static final Integer DATATYPEVALUE_PACKAGE_CoLUMN = 0;
	private static final Integer DATATYPEVALUE_DATA_TYPE_CoLUMN = 2;
	private static final Integer DATATYPEVALUE_DATA_TYPE_NAME_CoLUMN = 1;
	private static final Integer DATATYPEVALUE_NS_CoLUMN = 4;
	
	
	
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
	private static Map<String, MetaDataType> metaDataType = new HashMap<>();
	
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
	 * Gets the string of the package that a class should be in.  The package is expected
	 * to be from the first column of the Classes worksheet in the xls file.
	 * 
	 * @param data Data in the cell of the first column of the classes tab in the xls file.
	 * @return The package or null if not available.
	 */
	private static String getPackage(String data){
		String clspackage = ROOT_PACKAGE;
		
		// This used the package data from the first column to generate the package
		if (data.length() > 3){
			if(data.startsWith("ETXSCADA")){
				clspackage += ".ext.scada";
			}
			else if(data.startsWith("CIMSCADA")){
				clspackage += ".scada";
			}
			else if(data.startsWith("CIM") || data.startsWith("EXT")){
				
				if (data.startsWith("EXT")){
					clspackage += "ext";
				}
				
				for(int i=3; i < data.length();i++){
					if(Character.isUpperCase(data.charAt(i))){
						clspackage += "."+Character.toString(Character.toLowerCase(data.charAt(i)));
					}
					else{
						clspackage += data.charAt(i);
					}
				}
			}
			else{
				clspackage = null;
			}
		}
		else{
			clspackage += "." + data.toLowerCase();
		}
		
		return clspackage;
	}
	
	/**
	 * Adds the meta-classes super class for all classes in the  xls sheet.
	 * 
	 * @param classesSheet
	 */
	private static void addInheritancePath(HSSFSheet classesSheet){
		// First row is header
		for(int r=1; r < classesSheet.getPhysicalNumberOfRows(); r++){
			HSSFRow row = classesSheet.getRow(r);
			if (row == null) {
				continue;
			}
			
			HSSFCell classCell = row.getCell(CLASSES_CLASS_COLUMN); //.getStringCellValue()
			if (classCell != null && classCell.getStringCellValue()!= null){
				String className = classCell.getStringCellValue();
				if (classNameToType.containsKey(className)){
					// Pull the meta class out and set the superclass to the first
					// item in the Inheritance Path list.
					MetaClass meta = metaClasses.get(classNameToType.get(className));
					String inherString = row.getCell(CLASSES_INHERITANCE_COLUMN).getStringCellValue();
					String extendsClassName = inherString.split(";")[0];
					meta.setExtendsType(classNameToType.get(extendsClassName));
					
				}
				
			}
		}
	}
	/**
	 * Populates the internal metaDataTypes map from the DataTypes tab or the 
	 * 'DataTypes - Value&Unit' tab.  
	 * 
	 * @param dataTypeSheet
	 * @param isValueAndUnitSheet
	 */
	private static void createMetaDataTypes(HSSFSheet dataTypeSheet, boolean isValueAndUnitSheet){
		// First row is header
		for(int r=1; r < dataTypeSheet.getPhysicalNumberOfRows(); r++){
			HSSFRow row = dataTypeSheet.getRow(r);
			if (row == null) {
				continue;
			}
			
			if (isValueAndUnitSheet){
				HSSFCell packageCell = row.getCell(DATATYPEVALUE_PACKAGE_CoLUMN); 
				if (packageCell != null && packageCell.getStringCellValue()!= null){
					
					HSSFCell namespaceCell = row.getCell(DATATYPEVALUE_NS_CoLUMN);
					HSSFCell dataTypeNameCell = row.getCell(DATATYPEVALUE_DATA_TYPE_NAME_CoLUMN);
					HSSFCell dataTypeCell = row.getCell(DATATYPEVALUE_DATA_TYPE_CoLUMN);
					String dataTypeName = dataTypeNameCell.getStringCellValue();
					
					String namespace = namespaceCell.getStringCellValue();
					
					MetaDataType meta = new MetaDataType();
					
					meta.setDataTypeName(dataTypeName);
					meta.setNamespace(namespace);
					if (dataTypeCell != null){
						String dataTypeValue = dataTypeCell.getStringCellValue();
						meta.setValueType(dataTypeValue);
					}
					
	
					metaDataType.put(meta.getDataTypeName(), meta);
					
				}
			}
			else{
				HSSFCell packageCell = row.getCell(DATATYPE_PACKAGE_COLUMN); 
				if (packageCell != null && packageCell.getStringCellValue()!= null){
					boolean isEnum = packageCell.getStringCellValue().contains("Enum");
					
					HSSFCell namespaceCell = row.getCell(CLASSES_NAMESPACE_COLUMN);
					HSSFCell dataTypeCell = row.getCell(DATATYPE_DATA_TYPE_CoLUMN); 
					String dataTypeName = dataTypeCell.getStringCellValue();
					String namespace = namespaceCell.getStringCellValue();
					
					MetaDataType meta = new MetaDataType();
					
					meta.setDataTypeName(dataTypeName);
					meta.setNamespace(namespace);
					meta.setEnumeration(isEnum);
	
					metaDataType.put(meta.getDataTypeName(), meta);
					
				}
			}
		}
	}
	
	/**
	 * Creates the meta-classes from the classes sheet.  After this method
	 * is called all of the classes in the object model will be in the
	 * metaClasses Map by datatype.  Each class name will also be in the
	 * classNameToDataType map for easy retrieval and reference.
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
			//HSSFCell pkgCell = row.getCell(CLASSES_PACKAGE_COLUMN);
			HSSFCell pkgCell = row.getCell(CLASSES_NAMESPACE_COLUMN);
			if (pkgCell != null){
				if (pkgCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
					System.out.println("Skipping: " +pkgCell.getNumericCellValue());
				}
				else if(pkgCell.getCellType() == HSSFCell.CELL_TYPE_STRING){
					// Use this as a test so that we can get the mapped type and see
					// if it's already in the collection (Hopefully it never will!)
					MetaClass tmp = new MetaClass();
					tmp.setPackageName(getPackage(pkgCell.getStringCellValue()));
					tmp.setClassName(row.getCell(CLASSES_CLASS_COLUMN).getStringCellValue());
					
					if (!tmp.isValidClassDefinition()){
						System.out.println("Invalid class detected for row: "+ r);
						continue;
					}
					
					// Store the metaclass in our hashmap of classes if necessary.
					MetaClass newMetaClass = metaClasses.get(tmp.getDataType());
					if (newMetaClass == null){
						newMetaClass = tmp;
						metaClasses.put(newMetaClass.getDataType(), newMetaClass);
						classNameToType.put(newMetaClass.getClassName(), newMetaClass.getDataType());
					}
					else{
						System.out.println("Boo already has the datatype!! "+newMetaClass.getDataType());
					}
					
					HSSFCell docCell = row.getCell(CLASSES_DOCUMENTATION_COLUMN);
					if (docCell != null && docCell.getStringCellValue() != null){
						newMetaClass.setClassDocumentation(docCell.getStringCellValue());
					}
				}
			}
		}
		
		// Now that all of the classes have been pulled out of the worksheet we can
		// attempt to add the rest of the information for inheritance etc.
		addInheritancePath(classesSheet);
	}
	
	private static void createAttributes(HSSFSheet attribSheet){
		// First row is header
		for(int r=1; r < attribSheet.getPhysicalNumberOfRows(); r++){
			HSSFRow row = attribSheet.getRow(r);
			if (row == null) {
				continue;
			}
			HSSFCell classCell = row.getCell(ATTRIB_CLASS_COLUMN);
			HSSFCell attribCell = row.getCell(ATTRIB_ATTRIBUTE_COLUMN);
			HSSFCell dataTypeCell = row.getCell(ATTRIB_DATA_TYPE_COLUMN);
			HSSFCell documentationCell = row.getCell(ATTRIB_DOCUMENTATION_COLUMN);
			
			if (classCell == null || classCell.getStringCellValue() == null ||
					attribCell == null || attribCell.getStringCellValue() == null ||
					dataTypeCell == null || dataTypeCell.getStringCellValue() == null){
				continue;
			}
			
			String className = classCell.getStringCellValue();
			if (classNameToType.containsKey(className)){
				MetaClass metaClass = metaClasses.get(classNameToType.get(className));
				MetaAttribute newAttrib = new MetaAttribute();
				
				newAttrib.setAttributeName(attribCell.getStringCellValue());
				String dataType = dataTypeCell.getStringCellValue();
				if (metaDataType.get(dataType) != null){
					newAttrib.setDataType(metaDataType.get(dataType));
				}
				else{
					// Handle the case where the dataType is not an enumeration
					// nor isn't in the DataType's sheet in the ercot xls.
					if (classNameToType.get(dataType) != null){
						MetaDataType metaData = new MetaDataType(metaClass);
						newAttrib.setDataType(metaData);						
					}
					else{
						// Handle the case where Enum is prefixed on the attributes sheet, 
						// however on the DataTypes sheet the enums aren't prefixed.
						if (dataType.startsWith("Enum")){
							dataType = dataType.substring("Enum".length());
							if (metaDataType.get(dataType) != null){
								newAttrib.setDataType(metaDataType.get(dataType));
							}
							else{
								System.out.println("Here man!");
							}
						}
						else{
							try{
								MetaDataType metaData = new MetaDataType(dataType);
								newAttrib.setDataType(metaData);
							}
							catch(IllegalArgumentException e){
								// Handle the case temporarily that the datatype isn't
								// specified in the DataType listing by auto creating
								// with a float datatype.
								MetaDataType metaData = new MetaDataType();
								metaData.setDataTypeName(dataType);
								metaData.setNamespace("cim");
								metaData.setValueType("Float");
								System.out.println("AUTOCREATING: "+dataType);
								newAttrib.setDataType(metaData);
							}
						}
						//try{
							
//						}
//						// Catch the case where datatype is not a standard type.
//						catch(IllegalArgumentException e){
//							
//						}
					}
					
				}
				
				if (classNameToType.get(dataType) == null){
					System.out.println("null package for datatype '"+dataType+"'");
				}
				newAttrib.setDataTypePackage(classNameToType.get(dataType));
				
				if (documentationCell != null){
					newAttrib.setDocumentation(documentationCell.getStringCellValue());
				}
				
				metaClass.addAttribute(newAttrib);
			}
		}
	}

	/**
	 * Starts the generation of the models that are in the xls file
	 * 
	 * @param existingFile The downloaded xls file from ERCOT.
	 * @throws IOException 
	 */
	private static void generateModels(File existingFile) throws IOException{
		System.out.println("Generating models ...");
		HSSFWorkbook wb = readFile(existingFile);
		
		createMetaDataTypes(wb.getSheet("DataTypes"), false);
		createMetaDataTypes(wb.getSheet("DataTypes - Value&Unit"), true);
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
	
	private static void writeOutputFiles(File classesFile, File dataTypesFile, File classNameFile){
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
			
			for(MetaDataType dt: metaDataType.values()){
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
			generateModels(xlsFile);
			File classesFile = new File("created-classes.txt");
			File classNamesFile = new File("created-class-names.txt");
			File dataTypesFile = new File("created-datatypes.txt");
			File attributesFile = new File("created-attributes.txt");
			
			writeOutputFiles(classesFile, dataTypesFile, classNamesFile);
		}

	}

}
