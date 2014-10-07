package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;

@Entity
@Table(name=TAP_CHANGER)
@AttributeOverride(name="mrid", column=@Column(name=TAP_CHANGER_MRID))
@IndexCollection(columns={@Index(name=DATA_TYPE)})
public class TapChanger extends IdentifiedObject implements NodeBreakerDataType  {

	private static final long serialVersionUID = -8984289204990380623L;

	@Column(name=DATA_TYPE)
	protected String dataType;
	@Column
	private String tculControlMode;
	@Column
	private Integer lowStep;
	@Column
	private Integer normalStep;
	@Column
	private Integer highStep;
	@Column
	private Integer neutralStep;
	@Column
	private Double stepVoltageIncrement;
	@Column
	private String transformerWinding;
	
	public TapChanger(){
		dataType = TAP_CHANGER;
	}

	@Override
	public String getDataType() {
		return dataType;
	}

	@Override
	public void setDataType(String dataType) {
		this.dataType = dataType;
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
