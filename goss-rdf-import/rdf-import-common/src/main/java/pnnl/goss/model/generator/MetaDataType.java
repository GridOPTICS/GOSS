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
	private String javaPackage;
	private String documentation;
	private Set<String> enumeratedValues = new HashSet<>();
	
	public MetaDataType(){
		
	}
	
	public MetaDataType(String dataTypeName){
		this.dataTypeName = dataTypeName;
		this.valueType = getJavaType(dataTypeName);
		if (valueType != null){
			isStandardDataType = true;
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
	public String getJavaType(String valueTypeTest){
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
			vt = "DateTime";
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
				String result = getJavaType(valueType);				
				isIt = true;
			}
			else{
				System.out.println("Why?");
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
	
}
