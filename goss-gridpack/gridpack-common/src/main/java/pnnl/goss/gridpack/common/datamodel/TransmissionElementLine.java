package pnnl.goss.gridpack.common.datamodel;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.datamodel.Branch;
import pnnl.goss.powergrid.datamodel.Line;
import pnnl.goss.powergrid.datamodel.Transformer;

public class TransmissionElementLine extends TransmissionElement {
	
	// Line Data
	private double gI;
	private double bI;
	private double gJ;
	private double bJ;
	private double bCap;
	
	private TransmissionElementLine(){
		super();
	}
		
	private TransmissionElementLine(PowergridModel gridModel, Branch branch, Line line){
		super(gridModel,branch, null, line);
		
		gI = line.getGi();
		bI = line.getBi();
		gJ = line.getGj();
		bJ = line.getBj();
		bCap = line.getBcap();
	}
	
	private static Line findLine(List<Line>lines, int branchId){
		for(Line line:lines){
			if (line.getBranchId().equals(branchId)){
				return line;
			}
		}
		
		return null;
	}	
		
	public static TransmissionElement buildFromObject(PowergridModel gridModel, Branch branch) {
		log.debug("Building GridpackBranch from branchid: ", branch.getBranchId());
		Line line = findLine(gridModel.getLines(), branch.getBranchId());
		
		return new TransmissionElementLine(gridModel, branch, line); 
	}
	
	/**
	 * @return the gI
	 */
	@XmlElement(name = "BRANCH_SHUNT_ADMTTNC_GI")
	public double getgI() {
		return gI;
	}

	/**
	 * @param gI the gI to set
	 */
	public void setgI(double gI) {
		this.gI = gI;
	}

	/**
	 * @return the bI
	 */
	@XmlElement(name = "BRANCH_SHUNT_ADMTTNC_BI")
	public double getbI() {
		return bI;
	}

	/**
	 * @param bI the bI to set
	 */
	public void setbI(double bI) {
		this.bI = bI;
	}

	/**
	 * @return the gJ
	 */
	@XmlElement(name = "BRANCH_SHUNT_ADMTTNC_GJ")
	public double getgJ() {
		return gJ;
	}

	/**
	 * @param gJ the gJ to set
	 */
	public void setgJ(double gJ) {
		this.gJ = gJ;
	}

	/**
	 * @return the bJ
	 */
	@XmlElement(name = "BRANCH_SHUNT_ADMTTNC_BJ")
	public double getbJ() {
		return bJ;
	}

	/**
	 * @param bJ the bJ to set
	 */
	public void setbJ(double bJ) {
		this.bJ = bJ;
	}

	/**
	 * @return the bCap
	 */
	@XmlElement(name = "BRANCH_B")
	public double getbCap() {
		return bCap;
	}

	/**
	 * @param bCap the bCap to set
	 */
	public void setbCap(double bCap) {
		this.bCap = bCap;
	}
}
