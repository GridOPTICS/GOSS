package pnnl.goss.model.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetaClass {
	
	private List<MetaAttribute> attributes = new ArrayList<>();
	private String className;
	private String packageName;
	private String extendsType;
	private String classDocumentation;
	
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
}
