package pnnl.goss.gridpack.common.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Branch")
public class GridpackBranch {
	private String branchMrid;
	private Integer fromBusNumber;
	private Integer tobusNumber;
	private List<TransmissionElementLine> transmissionLines = new ArrayList<TransmissionElementLine>();
	private List<TransmissionElementTransformer> transmissionTransformers = new ArrayList<TransmissionElementTransformer>();
	
	public String getBranchMrid() {
		return branchMrid;
	}
	public void setBranchMrid(String branchMrid) {
		this.branchMrid = branchMrid;
	}
	@XmlElement(name="BRANCH_FROMBUS")
	public Integer getFromBusNumber() {
		return fromBusNumber;
	}
	public void setFromBusNumber(Integer fromBusNumber) {
		this.fromBusNumber = fromBusNumber;
	}
	@XmlElement(name="BRANCH_TOBUS")
	public Integer getTobusNumber() {
		return tobusNumber;
	}
	public void setTobusNumber(Integer tobusNumber) {
		this.tobusNumber = tobusNumber;
	}
	
	@XmlElement(name="TransmissionElements")
	public TransmissionElements getTransmissionElements(){
		return new TransmissionElements(transmissionLines, transmissionTransformers);
	}
	
	public void addItem(TransmissionElementLine line){
		transmissionLines.add(line);
	}
	
	public void addItem(TransmissionElementTransformer transformer){
		transmissionTransformers.add(transformer);
	}
	
	public List<TransmissionElementLine> getTransmissionElementLines(){
		return this.transmissionLines;
	}
	
	public List<TransmissionElementTransformer> getTransmissionElementTransformers(){
		return this.transmissionTransformers;
	}

}
