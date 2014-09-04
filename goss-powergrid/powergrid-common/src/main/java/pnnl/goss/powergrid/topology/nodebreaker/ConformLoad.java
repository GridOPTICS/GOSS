package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Column;
import javax.persistence.Entity;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

@Entity
public class ConformLoad extends IdentifiedObject implements NodeBreakerDataType  {

	@Column
	protected String dataType;
	@Column
	private Double energyConsumerpfixed;
	@Column
	private Double energyConsumerpfixedPct;
	@Column
	private Double energyConsumerpVexp;
	@Column
	private Double energyConsumerpFexp;
	@Column
	private Double energyConsumerqfixed;
	@Column
	private Double energyConsumerqfixedPct;
	@Column
	private Double energyConsumerqVexp;
	@Column
	private Double energyConsumerqFexp;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
		
	public Double getEnergyConsumerpfixed() {
		return energyConsumerpfixed;
	}

	public void setEnergyConsumerpfixed(Double energyConsumerpfixed) {
		this.energyConsumerpfixed = energyConsumerpfixed;
	}

	public Double getEnergyConsumerpfixedPct() {
		return energyConsumerpfixedPct;
	}

	public void setEnergyConsumerpfixedPct(Double energyConsumerpfixedPct) {
		this.energyConsumerpfixedPct = energyConsumerpfixedPct;
	}

	public Double getEnergyConsumerpVexp() {
		return energyConsumerpVexp;
	}

	public void setEnergyConsumerpVexp(Double energyConsumerpVexp) {
		this.energyConsumerpVexp = energyConsumerpVexp;
	}

	public Double getEnergyConsumerpFexp() {
		return energyConsumerpFexp;
	}

	public void setEnergyConsumerpFexp(Double energyConsumerpFexp) {
		this.energyConsumerpFexp = energyConsumerpFexp;
	}

	public Double getEnergyConsumerqfixed() {
		return energyConsumerqfixed;
	}

	public void setEnergyConsumerqfixed(Double energyConsumerqfixed) {
		this.energyConsumerqfixed = energyConsumerqfixed;
	}

	public Double getEnergyConsumerqfixedPct() {
		return energyConsumerqfixedPct;
	}

	public void setEnergyConsumerqfixedPct(Double energyConsumerqfixedPct) {
		this.energyConsumerqfixedPct = energyConsumerqfixedPct;
	}

	public Double getEnergyConsumerqVexp() {
		return energyConsumerqVexp;
	}

	public void setEnergyConsumerqVexp(Double energyConsumerqVexp) {
		this.energyConsumerqVexp = energyConsumerqVexp;
	}

	public Double getEnergyConsumerqFexp() {
		return energyConsumerqFexp;
	}

	public void setEnergyConsumerqFexp(Double energyConsumerqFexp) {
		this.energyConsumerqFexp = energyConsumerqFexp;
	}
}
