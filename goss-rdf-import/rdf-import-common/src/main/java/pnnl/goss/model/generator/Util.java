package pnnl.goss.model.generator;

public class Util {
	public static String tabifyLines(String data, String tabs){
		StringBuffer buf = new StringBuffer();
		
		for(String line : data.split("\n")){
			buf.append(tabs + line + "\n");
		}
		
		return buf.toString();
	}

}
