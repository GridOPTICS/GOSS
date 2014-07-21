package pnnl.goss.rdf.services;

import java.io.File;

import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.rdf.DuplicatePowergridException;

public interface EscaToPowergridService {

	/**
	 * Create a powergrid from the passed file and returns it to the caller.
	 * 
	 * @param pgName - The name of the powergrid
	 * @param esca60File - Path to the esca60 file on the server.
	 * @return
	 */
	Powergrid createPowergrid(String pgName, File esca60File) throws DuplicatePowergridException;
	
	
}
