package pnnl.goss.model.generator;

public class MetaAttribute {
	
	private String getterDocumentation;
	private String setterDocumentation;
	private String baseName;
	private String basenameLowerFirst;
	private boolean isCollection;
	private String fullType;
	
	public String getGetterDocumentation() {
		return getterDocumentation;
	}
	public void setGetterDocumentation(String getterDocumentation) {
		this.getterDocumentation = getterDocumentation;
	}
	public String getSetterDocumentation() {
		return setterDocumentation;
	}
	public void setSetterDocumentation(String setterDocumentation) {
		this.setterDocumentation = setterDocumentation;
	}
	public String getBaseName() {
		return baseName;
	}
	public void setBaseName(String baseName) {
		this.baseName = baseName;
		this.basenameLowerFirst = baseName.substring(0, 1).toLowerCase() 
				+ baseName.substring(1);
	}
	public boolean isCollection() {
		return isCollection;
	}
	public void setCollection(boolean isCollection) {
		this.isCollection = isCollection;
	}
	public String getFullType() {
		return fullType;
	}
	public void setFullType(String fullType) {
		this.fullType = fullType;
	}
	
	public String getAttributeDeclaration(){
		StringBuffer buf = new StringBuffer();		
		
		buf.append("private ");
		
		if (isCollection){
			
		}
		else{
			buf.append(baseName + " ");
		}
		buf.append(this.basenameLowerFirst + " = ");
		
		if(isCollection){
			
		}
		buf.append(";\n");
		
		return buf.toString();
	}
	
	public String getFunctions(){
		StringBuffer buf = new StringBuffer();
		
		//buf.append(getAddDefinition());
		buf.append(getSetterDefinition());
		//buf.append(getGetterDefinition());
		return buf.toString();
	}
	
	public String getAddDefinition(){
		return null;
	}
	
	private String getGetterDefinition(){
		return null;
	}
	
	private String getSetterDefinition(){
		StringBuffer buf = new StringBuffer();
		
		buf.append("/**\n");
		buf.append("* " + setterDocumentation + "\n");
		buf.append("*/\n");
		buf.append("public ");
		if (isCollection){
			
		}
		else{
			buf.append(baseName + "set"+ baseName + "("+ baseName + " " + basenameLowerFirst + "){\n");			
		}
		
		buf.append("\tthis."+basenameLowerFirst+" = "+basenameLowerFirst+";");
		buf.append("}\n\n");
		
		return buf.toString();
	}
	

}
