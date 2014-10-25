package pnnl.goss.rdf.server;

import java.io.File;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;

import com.hp.hpl.jena.rdf.model.Model;

import pnnl.goss.powergrid.dao.PowergridDao;
import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.rdf.DuplicatePowergridException;
import pnnl.goss.rdf.services.EscaToPowergridService;


@Component
@Provides
public class EscaToPowergridServiceImpl implements EscaToPowergridService {

	private PowergridDao powergridDao;
	
	public void setPowergridDao(@Property PowergridDao dao){
		powergridDao = dao;
	}
	
	@Override
	public Powergrid createPowergrid(String pgName, Model esca60Model)
			throws DuplicatePowergridException {
		// TODO Auto-generated method stub
		return null;
	}

}
