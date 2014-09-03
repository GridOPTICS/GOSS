package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;

@Entity
public class ConformLoad {
	
	@Id
	private String mrid;
	
	@Embedded
	private IdentifiedObject identifiedObject;
	
	private Double energyConsumerpfixed;
	private Double energyConsumerpfixedPct;
	private Double energyConsumerpVexp;
	private Double energyConsumerpFexp;
	
	private Double energyConsumerqfixed;
	private Double energyConsumerqfixedPct;
	private Double energyConsumerqVexp;
	private Double energyConsumerqFexp;
	
	public IdentifiedObject getIdentifiedObject() {
		return identifiedObject;
	}

	public void setIdentifiedObject(IdentifiedObject identifiedObject) {
		this.identifiedObject = identifiedObject;
		mrid = identifiedObject.getIdentMrid();
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
