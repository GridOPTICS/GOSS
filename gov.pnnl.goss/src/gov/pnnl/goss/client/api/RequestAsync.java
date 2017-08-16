package gov.pnnl.goss.client.api;

public interface RequestAsync extends Request {
	int getFrequency();
	void setFrequency(int frequency);
}
