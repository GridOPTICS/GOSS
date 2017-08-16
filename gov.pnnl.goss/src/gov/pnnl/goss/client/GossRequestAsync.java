package gov.pnnl.goss.client;


public class GossRequestAsync extends GossRequest {

	private static final long serialVersionUID = -7613047700580927505L;
	
	protected int frequency = 0;

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

}