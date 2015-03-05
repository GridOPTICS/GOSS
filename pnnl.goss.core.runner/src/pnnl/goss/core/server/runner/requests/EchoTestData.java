package pnnl.goss.core.server.tester.requests;

import java.io.Serializable;

public class EchoTestData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private byte[] byteData;
	private String stringData;
	private int intData;
	private boolean boolData;
	private double doubleData;
	private float floatData;
	
	public byte[] getByteData() {
		return byteData;
	}
	public EchoTestData setByteData(byte[] byteData) {
		this.byteData = byteData;
		return this;
	}
	public String getStringData() {
		return stringData;
	}
	public EchoTestData setStringData(String stringData) {
		this.stringData = stringData;
		return this;
	}
	public int getIntData() {
		return intData;
	}
	public EchoTestData setIntData(int intData) {
		this.intData = intData;
		return this;
	}
	public boolean isBoolData() {
		return boolData;
	}
	public EchoTestData setBoolData(boolean boolData) {
		this.boolData = boolData;
		return this;
	}
	public double getDoubleData() {
		return doubleData;
	}
	public EchoTestData setDoubleData(double doubleData) {
		this.doubleData = doubleData;
		return this;
	}
	public float getFloatData() {
		return floatData;
	}
	public EchoTestData setFloatData(float floatData) {
		this.floatData = floatData;
		return this;
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	
	@Override
	public String toString() {
		return String.format("%d%f%f%s%s", intData, floatData, doubleData, stringData, bytesToHex(byteData));
	}
	
}
