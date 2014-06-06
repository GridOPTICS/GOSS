package pnnl.goss.gridpack.common.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.datamodel.Branch;
import pnnl.goss.powergrid.datamodel.Bus;
import pnnl.goss.powergrid.datamodel.Line;
import pnnl.goss.powergrid.datamodel.Transformer;

@XmlRootElement(name="Branch")
public class GridpackBranch {
	static Logger log = LoggerFactory.getLogger(GridpackBranch.class);
	
	/**
	 * Field buses - A list of buses connected to this branch (from, to)
	 */
	private List<GridpackBus> buses;
	
	/**
	 * Field isTransformer - A flag to determine whether or not this branc is a transformer.
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
	
	// Line Data
	private double gI;
	private double bI;
	private double gJ;
	private double bJ;
	private double bCap;
	
	// Transformer Data
	private double ratio;
	private int icont;
	private double tapPosition;
	private double angle;
	private double rma;
	private double rmi;
	private double vma;
	
	private GridpackBranch(){
		
	}
		
	private GridpackBranch(PowergridModel gridModel, Branch branch, Transformer transformer, Line line) {
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
			ratio = transformer.getRatio();
			angle = transformer.getAngle();
			icont = transformer.getIcont();
			rma = transformer.getRma();
			rmi = transformer.getRmi();
			vma = transformer.getVma();
			isTransformer = true;
		}
		// This is a line
		else if(line != null){
			gI = line.getGi();
			bI = line.getBi();
			gJ = line.getGj();
			bJ = line.getBj();
			bCap = line.getBcap();
		}
	}
	
	private static Line findLine(List<Line>lines, int branchId){
		for(Line line:lines){
			if (line.getBranchId().equals(branchId)){
				return line;
			}
		}
		
		return null;
	}
	
	private static Transformer findTransformer(List<Transformer> transformers, int branchId){
		for(Transformer transformer: transformers){
			if (transformer.getBranchId().equals(branchId)){
				return transformer;
			}
		}
		
		return null;
	}

	public static GridpackBranch buildFromObject(PowergridModel gridModel, Branch branch) {
		log.debug("Building GridpackBranch from branchid: ", branch.getBranchId());
		Line line = findLine(gridModel.getLines(), branch.getBranchId());
		Transformer transformer = findTransformer(gridModel.getTransformers(), branch.getBranchId());
		return new GridpackBranch(gridModel, branch, transformer, line); 
	}

	/**
	 * @return the buses
	 */
	@XmlElementWrapper(name = "ConnectedBuses")
	@XmlElement(name = "ConnectedBus", type = GridpackBus.class)
	public List<GridpackBus> getBuses() {
		return buses;
	}

	/**
	 * @param buses the buses to set
	 */
	public void setBuses(List<GridpackBus> buses) {
		this.buses = buses;
	}

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
	@XmlElement(name = "IS_TRANSFORMER")
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
	@XmlElement(name = "BRANCH_FROMBUS")	 
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
	@XmlElement(name = "BRANCH_TOBUS")
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
