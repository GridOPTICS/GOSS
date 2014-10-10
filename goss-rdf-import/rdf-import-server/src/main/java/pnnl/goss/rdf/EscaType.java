package pnnl.goss.rdf;

import java.util.Collection;
import java.util.Map;

import pnnl.goss.rdf.impl.EscaTypeImpl;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface EscaType {

	public abstract void addDirectLink(String propertyName, EscaTypeImpl link);

	public abstract boolean isResourceType(Resource resourceType);

	public abstract Map<String, EscaTypeImpl> getLinks();

	public abstract Collection<EscaTypeImpl> getRefersToMe();

	public abstract Literal getLiteralValue(Property property);

	public abstract Literal getLiteralValue(String property);

	public abstract Collection<EscaTypeImpl> getDirectLinkedResources(
			Resource resource);

	public abstract EscaType getDirectLinkedResourceSingle(Resource resource);

	/**
	 * Add literal value to the escatype.  If the key contains the same
	 * datatype as a prefix then that prefix is stripped off and is assumed
	 * to be "contained" as part of the object.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void addLiteral(String key, Literal value);

	public abstract Map<String, Literal> getLiterals();

	/**
	 * @return the resource
	 */
	public abstract Resource getResource();

	/**
	 * @return the dataType
	 */
	public abstract String getDataType();

	/**
	 * @return the mrid
	 */
	public abstract String getMrid();

	/**
	 * Returns the identifiedobject.name parameter from the rdf code.  If it does not exist
	 * then returns the mrid of the element.
	 * 
	 * @return String
	 */
	public abstract String getName();

}