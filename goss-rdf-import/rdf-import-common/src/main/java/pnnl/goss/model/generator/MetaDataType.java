package pnnl.goss.model.generator;

import java.util.HashSet;
import java.util.Set;

public class MetaDataType {
	
	private boolean isEnumeration;
	private boolean isStandardDataType;
	private String dataTypeName;
	private String namespace;
	private String documentation;
	private Set<String> enumeratedValues = new HashSet<>();
	
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
