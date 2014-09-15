package pnnl.goss.model.generator;

import java.util.HashSet;
import java.util.Set;

public class MetaDataType {
	
	private boolean isEnumeration;
	/**
	 * Standard datatype is a System datatype that is added by default.
	 */
	private boolean isStandardDataType;
	private String valueType;
	private String dataTypeName;
	private String namespace;
	private String documentation;
	private Set<String> enumeratedValues = new HashSet<>();
	
	public MetaDataType(){
		
	}
	
	public MetaDataType(String dataTypeName){
		this.dataTypeName = dataTypeName;
		
		switch (dataTypeName){
		case "Bool":
			isStandardDataType = true;
			valueType = "Boolean";
			break;
		case "ShortInt":
		case "Short":
		case "Integer":
			isStandardDataType = true;
			valueType = "Integer";
			break;
		case "DoubleFloat":
			isStandardDataType = true;
			valueType = "DoubleFloat";
			break;
		case "ULong":
		case "ULongLong":
			isStandardDataType = true;
			valueType = "Long";
			break;
		default:
			throw new IllegalArgumentException("This constructor must use standard java type not: " + dataTypeName);
		}
			

	}
	
	public MetaDataType(MetaClass metaClass){
		this.dataTypeName = metaClass.getDataType();
		this.namespace = metaClass.getPackageName()
				.substring(metaClass.getPackageName()
						.lastIndexOf("."));
	}
	
	public void addEnumeratedValue(String value){
		enumeratedValues.add(value);
	}
	
	public Set<String> getEnumeratedValues(){
		return this.enumeratedValues;
	}
	
	public boolean isEnumeration() {
		return isEnumeration;
	}
	public void setEnumeration(boolean isEnumeration) {
		this.isEnumeration = isEnumeration;
	}
	public String getDataTypeName() {
		return dataTypeName;
	}
	public void setDataTypeName(String dataTypeName) {
		this.dataTypeName = dataTypeName;
		// Determine if the class describes a standard java datatype
		if (dataTypeName.contains("Integer") || 
				dataTypeName.contains("String") ||
				dataTypeName.contains("Long") ||
				dataTypeName.contains("Boolean") ||
				dataTypeName.contains("Short")){
			isStandardDataType = true;
			System.out.println(dataTypeName +" is a standard datatype");
		}
	}
	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
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
		return isStandardDataType;
	}
	
	
}
