package pnnl.goss.gridpack.service;

import java.io.OutputStream;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.w3c.dom.Document;

import pnnl.goss.gridpack.common.datamodel.GridpackBus;
import pnnl.goss.gridpack.common.datamodel.GridpackPowergrid;

public interface GridpackService {

	public abstract Collection<GridpackBus> getBuses0ToN(String powergridName,
			int numberOfBuses);

	public abstract Collection<GridpackBus> getBusesNToM(String powergridName,
			int startAtIndex, int numberOfBuses);

	public abstract GridpackPowergrid getGridpackGrid(String powergridName);

	public abstract String getGridpackGridWithWadl(String powergridName,
			String asPlain);

	public abstract Integer getNumberOfBuses(String powergridName);

	public abstract Integer getNumberOfBranches(String powergridName);

	public abstract void serialize(Document doc, OutputStream out)
			throws Exception;

}