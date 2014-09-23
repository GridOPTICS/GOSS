package pnnl.goss.gridpack.common.datamodel;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.datamodel.Branch;
import pnnl.goss.powergrid.datamodel.Transformer;

public class TransmissionElementTransformer extends TransmissionElement{
	
	// Transformer Data
	private double ratio;
	private int icont;
	private double tapPosition;
	private double angle;
	private double rma;
	private double rmi;
	private double vma;

	private TransmissionElementTransformer(){
		super();
	}
		
	private TransmissionElementTransformer(PowergridModel gridModel, Branch branch, Transformer transformer){
		super(gridModel,branch, transformer, null);
		
		ratio = transformer.getRatio();
		angle = transformer.getAngle();
		icont = transformer.getIcont();
		rma = transformer.getRma();
		rmi = transformer.getRmi();
		vma = transformer.getVma();
	}
	
	private static Transformer findTransformer(List<Transformer> transformers, int branchId){
		for(Transformer transformer: transformers){
			if (transformer.getBranchId().equals(branchId)){
				return transformer;
			}
		}
		
		return null;
	}
	
	public static TransmissionElement buildFromObject(PowergridModel gridModel, Branch branch) {
		log.debug("Building GridpackBranch from branchid: ", branch.getBranchId());
		Transformer transformer = findTransformer(gridModel.getTransformers(), branch.getBranchId());
		
		return new TransmissionElementTransformer(gridModel, branch, transformer); 
	}
	
	/**
	 * @return the ratio
	 */
	@XmlElement(name = "BRANCH_RATIO")
	public double getRatio() {
		return ratio;
	}

	/**
	 * @param ratio the ratio to set
	 */
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	/**
	 * @return the icont
	 */
	@XmlElement(name = "TRANSFORMER_CONT1")
	public int getIcont() {
		return icont;
	}

	/**
	 * @param icont the icont to set
	 */
	public void setIcont(int icont) {
		this.icont = icont;
	}

	/**
	 * @return the tapPosition
	 */
	@XmlElement(name = "TRANSFORMER_TAP")
	public double getTapPosition() {
		return tapPosition;
	}

	/**
	 * @param tapPosition the tapPosition to set
	 */
	public void setTapPosition(double tapPosition) {
		this.tapPosition = tapPosition;
	}

	/**
	 * @return the angle
	 */
	@XmlElement(name = "BRANCH_ANG")
	public double getAngle() {
		return angle;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}

	/**
	 * @return the rma
	 */
	@XmlElement(name = "TRANSFORMER_RMA1")
	public double getRma() {
		return rma;
	}

	/**
	 * @param rma the rma to set
	 */
	public void setRma(double rma) {
		this.rma = rma;
	}

	/**
	 * @return the rmi
	 */
	@XmlElement(name = "TRANSFORMER_RMI1")
	public double getRmi() {
		return rmi;
	}

	/**
	 * @param rmi the rmi to set
	 */
	public void setRmi(double rmi) {
		this.rmi = rmi;
	}

	/**
	 * @return the vma
	 */
	@XmlElement(name = "TRANSFORMER_VMA1")
	public double getVma() {
		return vma;
	}

	/**
	 * @param vma the vma to set
	 */
	public void setVma(double vma) {
		this.vma = vma;
	}

}
