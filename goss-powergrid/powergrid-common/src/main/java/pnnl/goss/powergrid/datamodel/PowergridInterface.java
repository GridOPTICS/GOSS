package pnnl.goss.powergrid.datamodel;

public interface PowergridInterface {

	/**
	 * Gets the value of the powergridId property.
	 * 
	 */
	public abstract int getPowergridId();

	/**
	 * Sets the value of the powergridId property.
	 * 
	 */
	public abstract void setPowergridId(int value);

	public abstract boolean isSetPowergridId();

	/**
	 * Gets the value of the name property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getName();

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setName(String value);

	public abstract boolean isSetName();

	/**
	 * Gets the value of the coordinateSystem property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getCoordinateSystem();

	/**
	 * Sets the value of the coordinateSystem property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setCoordinateSystem(String value);

	public abstract boolean isSetCoordinateSystem();

	/**
	 * Gets the value of the mrid property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public abstract String getMrid();

	/**
	 * Sets the value of the mrid property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public abstract void setMrid(String value);

	public abstract boolean isSetMrid();

}