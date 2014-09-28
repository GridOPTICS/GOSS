package pnnl.goss.model.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

public class MetaClass {
	
	// Classes sheet
	private static final Integer CLASSES_PACKAGE_COLUMN = 0;
	private static final Integer CLASSES_CLASS_COLUMN = 1;
	private static final Integer CLASSES_INHERITANCE_COLUMN = 3;
	private static final Integer CLASSES_NAMESPACE_COLUMN = 4;
	private static final Integer CLASSES_DOCUMENTATION_COLUMN = 5;
	
	private List<MetaAttribute> attributes = new ArrayList<>();
	private String className;
	private String packageName;
	private String extendsType;
	private String classDocumentation;
	private String packageAndClass;
	private String inheritancePath;
	
	private static String packageRoot;
	
	public static void setStaticPackageRoot(String staticRoot){
		packageRoot = staticRoot;
	}
	
	public static String getStaticPackageRoot(){
		return packageRoot;
	}
	
	public String getInheritancePath() {
		return inheritancePath;
	}

	public void setInheritancePath(String inheritancePath) {
		this.inheritancePath = inheritancePath;
	}

	public boolean isValidClassDefinition(){
		return className != null && className.length() > 0 &&
				packageName != null && packageName.length() > 0;
	}
	
	public String getPackageAndClass(){
		return packageAndClass;
	}
	
	public String getExtendsType() {
		return extendsType;
	}

	public void setExtendsType(String extendsType) {
		if (extendsType.contains(this.className)){
			this.extendsType = null;
		}
		else{
			this.extendsType = extendsType;
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public String getClassDocumentation() {
		return classDocumentation;
	}

	public void setClassDocumentation(String classDocumentation) {
		this.classDocumentation = classDocumentation;
	}

	public void addAttribute(MetaAttribute attr){
		attributes.add(attr);
	}
	
	public List<MetaAttribute> getAttributes(){
		return attributes;
	}
	
	
	
	public String getClassDefinition(){
		StringBuffer buf = new StringBuffer();
		System.out.println("Package Name: " + packageName);
		buf.append("package " + packageName + ";\n\n");
		
		Set<String> imports = new HashSet<>();
		for(MetaAttribute attr: attributes){
			
			MetaDataType metaDt = attr.getDataType();
			
			if (metaDt.isEnumeration()){
				imports.add(metaDt.getJavaPackage()+"."+metaDt.getDataTypeName());				
			}
			else if (metaDt.isStandardDataType()) {
				System.out.println("DATATYPE: "+metaDt.getDataTypeName()+ " => "+ metaDt.getValueType());
				if (metaDt.getDataTypeName().contains("Date")){
					imports.add("java.util.Date");
				}
			}
			
//			if (metaDt.getEnumeratedValues().isEmpty()){
//				// Skip altogether
//				continue;
//			}
//			else if (attr.getDataType().isStandardDataType()){
//				if (attr.getDataType().getValueType().equals("DateTime")){
//					imports.add("java.util.Date");
//				}					
//			}
//			else{
//				if (attr.getDataTypePackage()==null || attr.getDataTypePackage().contains("null")){
//					System.out.println("Ignoring package import for attribute: "+attr.getAttributeName());
//				}
//				else{
//					imports.add(attr.getDataTypePackage()+"."+attr.getDataType().getDataTypeName());
//				}
//				
//			}			
		}
		
		for(String varImport: imports){
			buf.append("import " + varImport+";\n");
		}
		
		
		if (classDocumentation != null){
			buf.append("/**\n");
			buf.append(classDocumentation + "\n");
			buf.append("*/\n");
		}
		
		buf.append("public class " + className + " ");
		if (extendsType != null && extendsType.length() > 0){
			buf.append("extends " + extendsType + " ");
		}
		// End of class first line
		buf.append("{\n\t");
		
		// Loop of the attributes twice, first for the declaration
		// second for the definition of the setter, getter and adder
		// functions.
		for(MetaAttribute attr: attributes){
			if (attr.getDataType().isEnumeration() && 
					attr.getDataType().getEnumeratedValues().isEmpty()){
				continue;
			}
			buf.append(Util.tabifyLines(attr.getAttributeDeclaration(), "\t"));
		}
		
		buf.append("\n\n");
		
		for(MetaAttribute attr: attributes){
			buf.append(Util.tabifyLines(attr.getFunctionDefinitions(), "\t"));
		}
		
		buf.append("}\n");
		
		return buf.toString();
	}	
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("Class: "+ className);
		if (this.packageName != null){
			buf.append(" Package: " + packageName.trim());
		}
		if (this.extendsType != null){
			buf.append(" Extends: " + extendsType);
		}
		return buf.toString();
	}
	
	public static MetaClass create(HSSFRow row){
		
		MetaClass cls = new MetaClass();
		
		if (row == null) {
			return null;
		}
		//HSSFCell pkgCell = row.getCell(CLASSES_PACKAGE_COLUMN);
		HSSFCell pkgCell = row.getCell(CLASSES_NAMESPACE_COLUMN);
		if (pkgCell != null){
			if (pkgCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
				System.out.println("Skipping: " +pkgCell.getNumericCellValue());
			}
			else if(pkgCell.getCellType() == HSSFCell.CELL_TYPE_STRING){
				cls.setPackageName(getPackage(pkgCell.getStringCellValue()));
				cls.setClassName(row.getCell(CLASSES_CLASS_COLUMN).getStringCellValue());
				
				if (!cls.isValidClassDefinition()){
					return null;
				}
				
				cls.setInheritancePath(row.getCell(CLASSES_INHERITANCE_COLUMN).getStringCellValue());
				cls.packageAndClass = cls.getPackageName() + "." + cls.getClassName();
				if (row.getCell(CLASSES_DOCUMENTATION_COLUMN) != null){
					cls.classDocumentation = row.getCell(CLASSES_DOCUMENTATION_COLUMN).getStringCellValue();
				}
			}
//				// Use this as a test so that we can get the mapped type and see
//				// if it's already in the collection (Hopefully it never will!)
//				MetaClass tmp = new MetaClass();
//				tmp.setPackageName(getPackage(pkgCell.getStringCellValue()));
//				tmp.setClassName(row.getCell(CLASSES_CLASS_COLUMN).getStringCellValue());
//				
//				if (!tmp.isValidClassDefinition()){
//					System.out.println("Invalid class detected for row: "+ r);
//					continue;
//				}
//				
//				// Store the metaclass in our hashmap of classes if necessary.
//				MetaClass newMetaClass = metaClasses.get(tmp.getDataType());
//				if (newMetaClass == null){
//					newMetaClass = tmp;
//					metaClasses.put(newMetaClass.getDataType(), newMetaClass);
//					classNameToType.put(newMetaClass.getClassName(), newMetaClass.getDataType());
//				}
//				else{
//					System.out.println("Boo already has the datatype!! "+newMetaClass.getDataType());
//				}
//				
//				HSSFCell docCell = row.getCell(CLASSES_DOCUMENTATION_COLUMN);
//				if (docCell != null && docCell.getStringCellValue() != null){
//					newMetaClass.setClassDocumentation(docCell.getStringCellValue());
//				}
//			}
		}
		return cls;
	}
		
	/**
	 * Gets the string of the package that a class should be in.  The package is expected
	 * to be from the first column of the Classes worksheet in the xls file.
	 * 
	 * @param data Data in the cell of the first column of the classes tab in the xls file.
	 * @return The package or null if not available.
	 */
	private static String getPackage(String data){
		String clspackage = ModelGeneration.ROOT_PACKAGE;
		
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
}
