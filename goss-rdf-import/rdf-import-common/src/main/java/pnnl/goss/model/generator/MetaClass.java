package pnnl.goss.model.generator;

import java.util.ArrayList;
import java.util.List;

public class MetaClass {
	
	private List<MetaAttribute> attributes = new ArrayList<>();
	private String className;
	private String packageName;
	private String extendsType;
	
	public boolean isValidClassDefinition(){
		return className != null && className.length() > 0 &&
				packageName != null && packageName.length() > 0;
	}
	
	public String getDataType(){
		return packageName + "." + className;
	}
	
	public String getExtendsType() {
		return extendsType;
	}

	public void setExtendsType(String extendsType) {
		this.extendsType = extendsType;
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

	public void addAttribute(MetaAttribute attr){
		attributes.add(attr);
	}
	
	public List<MetaAttribute> getAttributes(){
		return attributes;
	}
	
	private String tabifyLines(String data, String tabs){
		StringBuffer buf = new StringBuffer();
		
		for(String line : data.split("\n")){
			buf.append(tabs + line + "\n");
		}
		
		return buf.toString();
	}
	
	public String getClassDefinition(){
		StringBuffer buf = new StringBuffer();
		
		buf.append("package " + packageName + ";\n\n");
		
		// Do imports here.
		
		buf.append("class " + className + " ");
		if (extendsType != null && extendsType.length() > 0){
			buf.append("extends " + extendsType + " ");
		}
		// End of class first line
		buf.append("{\n\t");
		
		// Loop of the attributes twice, first for the declaration
		// second for the definition of the setter, getter and adder
		// functions.
		for(MetaAttribute attr: attributes){
			buf.append(tabifyLines(attr.getAttributeDeclaration(), "\t"));
		}
		
		buf.append("\n\n");
		
		for(MetaAttribute attr: attributes){
			buf.append(tabifyLines(attr.getFunctions(), "\t"));
		}
		
		buf.append("}\n");
		
		return buf.toString();
	}	
}
