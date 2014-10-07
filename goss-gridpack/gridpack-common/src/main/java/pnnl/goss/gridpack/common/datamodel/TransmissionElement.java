package pnnl.goss.gridpack.common.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.datamodel.Branch;
import pnnl.goss.powergrid.datamodel.Bus;
import pnnl.goss.powergrid.datamodel.Line;
import pnnl.goss.powergrid.datamodel.Transformer;

@XmlRootElement(name="Branch")
public class TransmissionElement {
	static Logger log = LoggerFactory.getLogger(TransmissionElement.class);
	
	protected TransmissionElement() {

	}
	
	/**
	 * Field buses - A list of buses connected to this branch (from, to)
	 */
	private List<GridpackBus> buses;
	
	/**
	 * Field isTransformer - A flag to determine whether or not this branch is a transformer.
	 */
	private boolean isTransformer;
	
	// Branch data
	private int branchId;
	private int fromBusNumber;
	private int toBusNumber;
	private int indexNum;
	
	private int ckt;
	private double r;
	private double x;
	private double rating;
	private double rateA;
	private double rateB;
	private double rateC;
	private int status;
	private double p;
	private double q;
	private String mrid;
		
	private boolean isSwitched = false;
	
		
	protected TransmissionElement(PowergridModel gridModel, Branch branch, Transformer transformer, Line line) {
		branchId = branch.getBranchId();
		fromBusNumber = branch.getFromBusNumber();
		toBusNumber = branch.getToBusNumber();
		buses = new ArrayList<GridpackBus>();
				
		log.debug("from-to bus numbers: "+fromBusNumber+ "-"+toBusNumber);
		Bus pgBusFrom = gridModel.getBus(fromBusNumber);
		if(pgBusFrom != null){
			GridpackBus fromBus = GridpackBus.buildFromObject(gridModel.getBus(fromBusNumber));
			buses.add(fromBus);
		}
		
		Bus pgBusTo = gridModel.getBus(toBusNumber);
		if(pgBusTo != null){
			GridpackBus toBus = GridpackBus.buildFromObject(gridModel.getBus(toBusNumber));
			buses.add(toBus);
		}		
		
		indexNum = branch.getIndexNum();
		// TODO Determine ckt true type.
		//ckt = Integer.parseInt() branch.getCkt();
		r = branch.getR();
		x = branch.getX();
		rating = branch.getRating();
		rateA = branch.getRateA();
		rateB = branch.getRateB();
		rateC = branch.getRateC();
		status = branch.getStatus();
		p = branch.getP();
		q = branch.getQ();
		mrid = branch.getMrid();
				
		// This is a transformer
		if(transformer != null){
			
			isTransformer = true;
		}
		// This is a line
		else if(line != null){
			
		}
	}
	
//	/**
//	 * @return the buses
//	 */
//	@XmlElementWrapper(name = "ConnectedBuses")
//	@XmlElement(name = "ConnectedBus", type = GridpackBus.class)
//	public List<GridpackBus> getBuses() {
//		return buses;
//	}
//
//	/**
//	 * @param buses the buses to set
//	 */
//	public void setBuses(List<GridpackBus> buses) {
//		this.buses = buses;
//	}

	/**
	 * @return the branchId
	 */
	@XmlElement(name = "BRANCH_ID")
	public int getBranchId() {
		return branchId;
	}

	/**
	 * @param branchId the branchId to set
	 */
	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}

	/**
	 * @return the isTransformer
	 */
	// @XmlElement(name = "IS_TRANSFORMER")
	@XmlTransient
	public boolean isTransformer() {
		return isTransformer;
	}

	/**
	 * @param isTransformer the isTransformer to set
	 */
	public void setTransformer(boolean isTransformer) {
		this.isTransformer = isTransformer;
	}

	
	/**
	 * @return the fromBusNumber
	 */
	@XmlTransient // This property is now reported up one level in GridPackBranch
	public int getFromBusNumber() {
		return fromBusNumber;
	}

	/**
	 * @param fromBusNumber the fromBusNumber to set
	 */
	public void setFromBusNumber(int fromBusNumber) {
		this.fromBusNumber = fromBusNumber;
	}

	/**
	 * @return the toBusNumber
	 */
	@XmlTransient // This property is now reported up one level in GridPackBranch
	public int getToBusNumber() {
		return toBusNumber;
	}

	/**
	 * @param toBusNumber the toBusNumber to set
	 */
	public void setToBusNumber(int toBusNumber) {
		this.toBusNumber = toBusNumber;
	}

	/**
	 * @return the indexNum
	 */
	@XmlElement(name = "BRANCH_INDEX")
	public int getIndexNum() {
		return indexNum;
	}

	/**
	 * @param indexNum the indexNum to set
	 */
	public void setIndexNum(int indexNum) {
		this.indexNum = indexNum;
	}

	/**
	 * @return the ckt
	 */
	@XmlElement(name = "BRANCH_CKT")
	public int getCkt() {
		return ckt;
	}

	/**
	 * @param ckt the ckt to set
	 */
	public void setCkt(int ckt) {
		this.ckt = ckt;
	}

	/**
	 * @return the r
	 */
	@XmlElement(name = "BRANCH_R")
	public double getR() {
		return r;
	}

	/**
	 * @param r the r to set
	 */
	public void setR(double r) {
		this.r = r;
	}

	/**
	 * @return the x
	 */
	@XmlElement(name = "BRANCH_X")
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the rating
	 */
	@XmlElement(name = "BRANCH_RATING")
	public double getRating() {
		return rating;
	}

	/**
	 * @param rating the rating to set
	 */
	public void setRating(double rating) {
		this.rating = rating;
	}

	/**
	 * @return the rateA
	 */
	@XmlElement(name = "BRANCH_RATING_A")
	public double getRateA() {
		return rateA;
	}

	/**
	 * @param rateA the rateA to set
	 */
	public void setRateA(double rateA) {
		this.rateA = rateA;
	}

	/**
	 * @return the rateB
	 */
	@XmlElement(name = "BRANCH_RATING_B")
	public double getRateB() {
		return rateB;
	}

	/**
	 * @param rateB the rateB to set
	 */
	public void setRateB(double rateB) {
		this.rateB = rateB;
	}

	/**
	 * @return the rateC
	 */
	@XmlElement(name = "BRANCH_RATING_C")
	public double getRateC() {
		return rateC;
	}

	/**
	 * @param rateC the rateC to set
	 */
	public void setRateC(double rateC) {
		this.rateC = rateC;
	}

	/**
	 * @return the status
	 */
	@XmlElement(name = "BRANCH_STATUS")
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	@XmlElement(name="BRANCH_SWITCHED")
	public boolean isSwitched() {
		return isSwitched;
	}

	public void setSwitched(boolean isSwitched) {
		this.isSwitched = isSwitched;
	}

	/**
	 * @return the p
	 */
	@XmlElement(name = "BRANCH_FLOW_P")
	public double getP() {
		return p;
	}

	/**
	 * @param p the p to set
	 */
	public void setP(double p) {
		this.p = p;
	}

	/**
	 * @return the q
	 */
	@XmlElement(name = "BRANCH_FLOW_Q")
	public double getQ() {
		return q;
	}

	/**
	 * @param q the q to set
	 */
	public void setQ(double q) {
		this.q = q;
	}
	
}
