package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class TapChanger {
	
	@Id
	private String mrid;
	
	@Embedded
	private IdentifiedObject identifiedObject;

	private String tculControlMode;
	private Integer lowStep;
	private Integer normalStep;
	private Integer highStep;
	private Integer neutralStep;
	private Double stepVoltageIncrement;
	private String transformerWinding;	
	
	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
	}

	public String getTculControlMode() {
		return tculControlMode;
	}

	public void setTculControlMode(String tculControlMode) {
		this.tculControlMode = tculControlMode;
	}

	public Integer getLowStep() {
		return lowStep;
	}

	public void setLowStep(Integer lowStep) {
		this.lowStep = lowStep;
	}

	public Integer getNormalStep() {
		return normalStep;
	}

	public void setNormalStep(Integer normalStep) {
		this.normalStep = normalStep;
	}

	public Integer getHighStep() {
		return highStep;
	}

	public void setHighStep(Integer highStep) {
		this.highStep = highStep;
	}

	public Double getStepVoltageIncrement() {
		return stepVoltageIncrement;
	}

	public void setStepVoltageIncrement(Double stepVoltageIncrement) {
		this.stepVoltageIncrement = stepVoltageIncrement;
	}

	public Integer getNeutralStep() {
		return neutralStep;
	}

	public void setNeutralStep(Integer neutralStep) {
		this.neutralStep = neutralStep;
	}

	public String getTransformerWinding() {
		return transformerWinding;
	}

	public void setTransformerWinding(String transformerWinding) {
		this.transformerWinding = transformerWinding;
	}
}
