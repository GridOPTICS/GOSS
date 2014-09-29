//package pnnl.goss.rdf.services;
//
//import java.io.File;
//
//import com.hp.hpl.jena.rdf.model.Model;
//
//import pnnl.goss.powergrid.datamodel.Powergrid;
//import pnnl.goss.rdf.DuplicatePowergridException;
//
//public interface EscaToPowergridService {
//
//	/**
//	 * Create a powergrid from the passed file and returns it to the caller.
//	 * 
//	 * @param pgName - The name of the powergrid
//	 * @param rdfModel - A populated esca60 cim rdf model
//	 * @return The Powergrid that was created
//	 */
//	Powergrid createPowergrid(String pgName, Model rdfModel) throws DuplicatePowergridException;
//	
//	
//}
