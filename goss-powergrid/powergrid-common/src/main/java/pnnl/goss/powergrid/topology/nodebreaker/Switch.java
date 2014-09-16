package pnnl.goss.powergrid.topology.nodebreaker;

public interface Switch extends ConductingEquipment {
	
	public Boolean getSwitchNormalOpen();
	
	public void setSwitchNormalOpen(Boolean normalOpen);
	
	public Double getRatedCurrent();
	
	public void setRatedCurrent(Double ratedCurrent);
}
