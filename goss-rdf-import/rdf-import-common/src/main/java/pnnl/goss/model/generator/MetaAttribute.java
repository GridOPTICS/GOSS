package pnnl.goss.model.generator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

public class MetaAttribute {
	public static final int ATTRIB_PACKAGE_COLUMN = 0;
	public static final int ATTRIB_CLASS_COLUMN = 2;
	public static final int ATTRIB_ATTRIBUTE_COLUMN = 4;
	public static final int ATTRIB_DATA_TYPE_COLUMN = 5;
	public static final int ATTRIB_INITAL_VALUE_COLUMN = 6; 
	public static final int ATTRIBUG_NS_COLUMN = 7;
	public static final int ATTRIB_DOCUMENTATION_COLUMN = 8;
	
	private String documentation;
	private String attributeName;
	private MetaDataType dataType;
	private String initialValue;
	private String setterFunctionName;
	private String getterFunctionName;
	private String dataTypePackage;
	
	public String getDataTypePackage() {
		return dataTypePackage;
	}

	public void setDataTypePackage(String dataTypePackage) {
		this.dataTypePackage = dataTypePackage;
	}
	
	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
		String baseName = attributeName.substring(0, 1).toUpperCase()+
				attributeName.substring(1);
		this.setterFunctionName = "set"+baseName;
		this.getterFunctionName = "get"+baseName;
	}

	public MetaDataType getDataType() {
		return dataType;
	}

	public void setDataType(MetaDataType dataType) {
		this.dataType = dataType;
	}

	public String getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(String initialValue) {
		this.initialValue = initialValue;
	}
	
	public static MetaAttribute create(HSSFRow row, FindDataType dataTypeFinder, FindClass classFinder){
		HSSFCell classCell = row.getCell(ATTRIB_CLASS_COLUMN);
		HSSFCell attribCell = row.getCell(ATTRIB_ATTRIBUTE_COLUMN);
		HSSFCell dataTypeCell = row.getCell(ATTRIB_DATA_TYPE_COLUMN);
		HSSFCell documentationCell = row.getCell(ATTRIB_DOCUMENTATION_COLUMN);
		
		MetaAttribute attribute = null;
		
		if (classCell == null || classCell.getStringCellValue() == null ||
				attribCell == null || attribCell.getStringCellValue() == null ||
				dataTypeCell == null || dataTypeCell.getStringCellValue() == null){
			System.err.println("All fields that I care about are empty!");
			return null;
		}
		String className = classCell.getStringCellValue();
		MetaDataType metaDataType = dataTypeFinder.getDataType(className);
		MetaClass classType = classFinder.getClass(className);
		
		if (metaDataType != null && classType != null){
			System.err.println("Both class and datatype???");
		}
		else if (metaDataType==null && classType == null){
			System.err.println("Both class and datatype are null!");
		}
		else if (metaDataType == null){
			System.err.println("Missing dataType "+className);
		}
		else {
			
			MetaAttribute newAttrib = new MetaAttribute();
			
			newAttrib.setAttributeName(attribCell.getStringCellValue());
			String dataType = dataTypeCell.getStringCellValue();
//			if (metaDataTypes.get(dataType) != null){
//				newAttrib.setDataType(metaDataTypes.get(dataType));
//			}
//			else{
//				// Handle the case where the dataType is not an enumeration
//				// nor isn't in the DataType's sheet in the ercot xls.
//				if (classNameToType.get(dataType) != null){
//					MetaDataType metaData = new MetaDataType(metaClass);
//					newAttrib.setDataType(metaData);						
//				}
//				else{
//					// Handle the case where Enum is prefixed on the attributes sheet, 
//					// however on the DataTypes sheet the enums aren't prefixed.
//					if (dataType.startsWith("Enum")){
//						dataType = dataType.substring("Enum".length());
//						if (metaDataTypes.get(dataType) != null){
//							newAttrib.setDataType(metaDataTypes.get(dataType));
//							newAttrib.setDataTypePackage(getEnumerationPackage());
//						}
//						else{
//							MetaDataType dt = new MetaDataType();
//							dt.setDataTypeName(dataType);
//							dt.setEnumeration(true);
//							dt.setJavaPackage(getEnumerationPackage());
//							newAttrib.setDataType(dt);
//							newAttrib.setDataTypePackage(getEnumerationPackage());
//							metaDataTypes.put(dataType, dt);
//						}
//					}
//					else{
//						try{
//							MetaDataType metaData = new MetaDataType(dataType);
//							newAttrib.setDataType(metaData);
//						}
//						catch(IllegalArgumentException e){
//							// Handle the case temporarily that the datatype isn't
//							// specified in the DataType listing by auto creating
//							// with a float datatype.
//							MetaDataType metaData = new MetaDataType();
//							metaData.setDataTypeName(dataType);
//							metaData.setNamespace("cim");
//							metaData.setValueType("Float");
//							System.out.println("AUTOCREATING: "+dataType);
//							newAttrib.setDataType(metaData);
//						}
//					}
//				}					
//			}
		}
		return attribute;
	}

	public String getAttributeDeclaration(){
		StringBuffer buf = new StringBuffer();		
		
		if (documentation != null){
			buf.append("/**\n* " + documentation+ "\n*/\n");
		}
		if (dataType.getValueType() != null && dataType.getValueType().length() > 0){
			buf.append("private "+dataType.getValueType()+" "+ attributeName+ ";\n");
		}
		else{
			buf.append("private "+dataType.getDataTypeName()+" "+ attributeName+ ";\n");
		}
		
		return buf.toString();
	}
	
	public String getFunctionDefinitions(){
		StringBuffer buf = new StringBuffer();
		
		buf.append("public void ");
		if (dataType.getValueType() != null && dataType.getValueType().length() > 0){
			buf.append(setterFunctionName+"("+dataType.getValueType()+" "+attributeName+") {\n\t");
		}
		else{
			buf.append(setterFunctionName+"("+dataType.getDataTypeName()+" "+attributeName+") {\n\t");
		}
		buf.append("this."+attributeName+" = "+attributeName+";\n}\n\n");
		
		if (dataType.getValueType() != null && dataType.getValueType().length() > 0){
			buf.append("public "+dataType.getValueType()+" ");
		}
		else{
			buf.append("public "+dataType.getDataTypeName()+" ");
		}
		buf.append(getterFunctionName+"() {\n\t"); 
		buf.append("return this."+attributeName+";\n}\n\n");

		return buf.toString();
	}
	
	@Override
	public String toString(){
		
		if (dataType != null){
			return "Attribute: "+attributeName+" "+dataType.toString();
		}
		else{
			return "Attribute: "+attributeName+" ";
		}
	}
	
	
	

}
