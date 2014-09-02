package pnnl.goss.powergrid.server.impl;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.dao.PowergridDao;
import pnnl.goss.powergrid.datamodel.AlertContext;
import pnnl.goss.powergrid.datamodel.Area;
import pnnl.goss.powergrid.datamodel.Branch;
import pnnl.goss.powergrid.datamodel.Bus;
import pnnl.goss.powergrid.datamodel.Line;
import pnnl.goss.powergrid.datamodel.Load;
import pnnl.goss.powergrid.datamodel.Machine;
import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.powergrid.datamodel.Substation;
import pnnl.goss.powergrid.datamodel.SwitchedShunt;
import pnnl.goss.powergrid.datamodel.Transformer;
import pnnl.goss.powergrid.datamodel.Zone;
import pnnl.goss.powergrid.topology.nodebreaker.Breaker;

public class PowergridPersist implements PowergridDao {

	/**
	 * The connenction to be used for retrieval/storage of entity data.
	 */
	String persistenceUnit;
	
	EntityManagerFactory emf;

    EntityManager em;
	
	public PowergridPersist(String persistenceUnit){
		this.persistenceUnit = persistenceUnit;
		if (emf == null)
        {
            emf = Persistence.createEntityManagerFactory(this.persistenceUnit);
        }
	}
	
	@Override
	public void persist(Powergrid powergrid) {
		em = emf.createEntityManager();
    	/*for(Terminal t: car.getTerminals()){
    		em.persist(t);
    		
    	}*/
    	em.persist(powergrid);
    	if (em != null)
        {
            em.close();
            em = null;
        }    	
	}


   
    public Object get(Class<?> entityClass, String id)
    {
        EntityManager em = emf.createEntityManager();
        Object p = em.find(entityClass, id);
        return p;
    }
    
	@Override
	public List<String> getPowergridNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Powergrid getPowergridByMrid(String mrid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Powergrid getPowergridByName(String powergridName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Powergrid getPowergridById(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlertContext getAlertContext(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PowergridModel getPowergridModel(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PowergridModel getPowergridModelAtTime(int powergridId,
			Timestamp timestep) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Powergrid> getAvailablePowergrids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Timestamp> getTimeSteps(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Area> getAreas(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Branch> getBranches(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Bus> getBuses(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Line> getLines(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Load> getLoads(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Machine> getMachines(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SwitchedShunt> getSwitchedShunts(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Substation> getSubstations(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Transformer> getTransformers(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Zone> getZones(int powergridId) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
