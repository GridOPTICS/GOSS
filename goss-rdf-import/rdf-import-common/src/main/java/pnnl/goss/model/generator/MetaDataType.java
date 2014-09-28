package pnnl.goss.model.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

import pnnl.goss.model.generator.ModelGeneration.DataTypeSheets;

public class MetaDataType {
	
	private boolean isEnumeration;
	/**
	 * Standard datatype is a System datatype that is added by default.
	 */
	private boolean isStandardDataType;
	private String valueType;
	private String dataTypeName;
	private String namespace;
	private String javaPackage;
	private String documentation;
	private Set<String> enumeratedValues = new HashSet<>();
	
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
	// DataType - Enum sheet
	private static final Integer DATATYPEENUM_PACKAGE_COLUMN = 0;
	private static final Integer DATATYPEENUM_ENUM_TYPE_COLUMN = 1;
	private static final Integer DATATYPEENUM_ENUM_VALUE_COLUMN = 2;
	private static final Integer DATATYPEENUM_DOCUMENTATION_CoLUMN = 3;
	private static final Integer DATATYPEENUM_NS_CoLUMN = 4;
	
	public MetaDataType(){
		
	}
	
	public static String getEnumerationPackage(){
		return ModelGeneration.ROOT_PACKAGE.concat(".enumerations");
	}
	
	public MetaDataType(String dataTypeName){
		this.dataTypeName = dataTypeName;
		this.valueType = getJavaType(dataTypeName);
		if (valueType != null){
			isStandardDataType = true;
		}
	}
	
	public MetaDataType(MetaClass metaClass){
		this.dataTypeName = metaClass.getPackageAndClass();
		this.namespace = metaClass.getPackageName()
				.substring(metaClass.getPackageName()
						.lastIndexOf("."));
	}
	
	public void addEnumeratedValue(String value){
		enumeratedValues.add(value);
	}
	
	public boolean containsEnumeratedValue(String value){
		return enumeratedValues.contains(value);
	}
	
	public List<String> getEnumeratedValues(){
		return new ArrayList(this.enumeratedValues);
	}
	
	public boolean isEnumeration() {
		return isEnumeration;
	}
	public void setEnumeration(boolean isEnumeration) {
		this.isEnumeration = isEnumeration;
	}
	public String getJavaPackage() {
		return javaPackage;
	}

	public void setJavaPackage(String javaPackage) {
		this.javaPackage = javaPackage;
	}

	public String getDataTypeName() {
		return dataTypeName;
	}
	public void setDataTypeName(String dataTypeName) {
		this.dataTypeName = dataTypeName;
		try{
			String vType = getJavaType(dataTypeName);
			this.isStandardDataType = true;
		}
		catch(IllegalArgumentException e){
			
		}
	}
	public String getValueType() {
		return valueType;
	}
	
	/**
	 * Returns the standard java object type or throws exception if the
	 * type is not specified as "Java Type"
	 * @param valueTypeTest
	 * @return
	 */
	public String getJavaType(String valueTypeTest) throws IllegalArgumentException{
		String vt = null;
		switch (valueTypeTest){
		case "Bool":
		case "Boolean":
			vt = "Boolean";
			break;
		case "ShortInt":
		case "Short":
		case "Integer":
			vt = "Integer";
			break;
		case "LongInt":
			vt = "Long";
			break;			
		case "DoubleFloat":
		case "Float":
			vt = "Float";
			break;
		case "ULong":
		case "ULongLong":
			vt = "Long";
			break;
		case "DateTime":
		case "Time":
			vt = "Date";
			break;
		default:
			if (valueTypeTest.startsWith("String")){
				vt = "String";
				break;
			}
			throw new IllegalArgumentException("This constructor must use standard java type not: " + dataTypeName);
		}
		
		return vt;
	}

	public void setValueType(String valueType) {
		// If valueType is not a java type then IllegalArgument is thrown.  We catch
		// it here so that we can put the user defined valueType.
		try{ 
			this.valueType = getJavaType(valueType);
		}
		catch(IllegalArgumentException e){
			this.valueType = valueType;
		}
	}

	public void setEnumeratedValues(Set<String> enumeratedValues) {
		this.enumeratedValues = enumeratedValues;
	}

	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getDocumentation() {
		return documentation;
	}
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}
	public boolean isStandardDataType() {
		boolean isIt = false;
		try{
			if (valueType != null){
				String vtCheck = getJavaType(valueType);				
				isIt = (vtCheck != null);
			}
			else{
				System.out.println("valueType is null for datatype: " + this.dataTypeName);
			}
		}
		catch(IllegalArgumentException e){
			isIt = false;
		}
		return isIt;
	}
		
	public String getEnumeration(){
		if(!isEnumeration){
			throw new IllegalAccessError("Datatype is not an enumeration!");
		}
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("package ");
		buf.append(javaPackage);
		buf.append(";\n\n");
		
		buf.append("public enum ");
		buf.append(dataTypeName);
		buf.append(" {\n");
		boolean first = true;
		for(String item: enumeratedValues){
			if(first){
				buf.append("\t");
				buf.append(item);
				first = false;
			}
			else{
				buf.append(",\n\t");
				buf.append(item);
			}			
		}
		buf.append("\n}");
		
		return buf.toString();
	}
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("DATA_TYPE: "+ this.dataTypeName);
		if (this.valueType != null){
			buf.append(" ValueType: " + this.valueType);
		}
		if (this.isEnumeration){
			buf.append(" Is Enumeration: Yes");
			buf.append(" Values: ");
			for(String s: this.enumeratedValues){
				buf.append(" "+s);
			}
		}
		if (this.isStandardDataType){
			buf.append(" Is Standard Datatype: Yes");
		}
		
		return buf.toString();
		
	}
	
	public static MetaDataType create(HSSFRow row, DataTypeSheets sheetType){
		
		if (row == null) {
			return null;
		}
		
		MetaDataType cls = new MetaDataType();
		
		HSSFCell packageCell= null;
		switch (sheetType){
		case DataTypeAndUnits:
			System.out.println("DatatypeandUnits");
			packageCell = row.getCell(DATATYPEVALUE_PACKAGE_CoLUMN); 
			if (packageCell != null && packageCell.getStringCellValue()!= null){
				
				HSSFCell namespaceCell = row.getCell(DATATYPEVALUE_NS_CoLUMN);
				HSSFCell dataTypeNameCell = row.getCell(DATATYPEVALUE_DATA_TYPE_NAME_CoLUMN);
				HSSFCell dataTypeCell = row.getCell(DATATYPEVALUE_DATA_TYPE_CoLUMN);
				String dataTypeName = dataTypeNameCell.getStringCellValue();
				
				String namespace = namespaceCell.getStringCellValue();
				
				cls.setDataTypeName(dataTypeName);
				cls.setNamespace(namespace);
				
				if (dataTypeCell != null){
					String dataTypeValue = dataTypeCell.getStringCellValue();
					cls.setValueType(dataTypeValue);
				}				
			}
			break;
		case DataType:
			System.out.println("It's Datatype Enumeration");
			packageCell = row.getCell(DATATYPE_PACKAGE_COLUMN); 
			if (packageCell != null && packageCell.getStringCellValue()!= null){
				boolean isEnum = packageCell.getStringCellValue().contains("Enum");
				
				HSSFCell namespaceCell = row.getCell(DATATYPE_NS_CoLUMN);
				HSSFCell dataTypeCell = row.getCell(DATATYPE_DATA_TYPE_CoLUMN); 
				String dataTypeName = dataTypeCell.getStringCellValue();
				String namespace = namespaceCell.getStringCellValue();
				
				cls.setDataTypeName(dataTypeName);
				cls.setNamespace(namespace);
				cls.setEnumeration(isEnum);
				

				// metaDataType.put(meta.getDataTypeName(), meta);
			}
			break;
		case DataTypeEnums:
			System.out.println("DatatypeEnum");
			packageCell = row.getCell(DATATYPEENUM_PACKAGE_COLUMN); 
			if (packageCell != null && packageCell.getStringCellValue()!= null){
									
				HSSFCell namespaceCell = row.getCell(DATATYPEENUM_NS_CoLUMN);
				HSSFCell enumTypeCell = row.getCell(DATATYPEENUM_ENUM_TYPE_COLUMN);
				HSSFCell enumValueCell = row.getCell(DATATYPEENUM_ENUM_VALUE_COLUMN);
				
				String enumTypeName = enumTypeCell.getStringCellValue();
				String enumValue = enumValueCell.getStringCellValue();
				String namespace = namespaceCell.getStringCellValue();
				
				cls.setDataTypeName(enumTypeName);
				cls.setNamespace(namespace);
				cls.setEnumeration(true);
				cls.setJavaPackage(getEnumerationPackage());
				cls.addEnumeratedValue(enumValue);
//				MetaDataType meta = null; 
//				// Handle the addition of enumeration values
//				if(metaDataType.containsKey(enumTypeName)){
//					meta = metaDataType.get(enumTypeName);
//					meta.addEnumeratedValue(enumValue);
//					meta.setJavaPackage(getEnumerationPackage());
//					meta.setEnumeration(true);
//				}
//				else{
//					meta = new MetaDataType();
//					
//					// metaDataType.put(meta.getDataTypeName(), meta);
//				}
			}
			break;
		}	
	
		if (cls != null){
			System.out.println("Created datatype: "+cls.getDataTypeName());
		}
		
		return cls;
	}
	
}
