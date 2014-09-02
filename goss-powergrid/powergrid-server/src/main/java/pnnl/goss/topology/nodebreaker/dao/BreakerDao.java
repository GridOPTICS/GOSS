package pnnl.goss.topology.nodebreaker.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import pnnl.goss.powergrid.topology.ElementIdentifier;
import pnnl.goss.powergrid.topology.nodebreaker.Breaker;


public class BreakerDao {
	EntityManagerFactory emf;

    EntityManager em;

    public BreakerDao(String persistenceUnitName)
    {
        if (emf == null)
        {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        }
    }
    
    public Object get(Class<?> entityClass, ElementIdentifier id)
    {
        EntityManager em = emf.createEntityManager();
        Object p = em.find(entityClass, id);
        return p;
    }
    
    public void persist(Breaker car){
    	em = emf.createEntityManager();
    	/*for(Terminal t: car.getTerminals()){
    		em.persist(t);
    		
    	}*/
    	em.persist(car);
    	if (em != null)
        {
            em.close();
            em = null;
        }    	
    }

}
