package pnnl.goss.topology.nodebreaker.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import pnnl.goss.powergrid.topology.Substation;
import pnnl.goss.powergrid.topology.nodebreaker.Analog;
import pnnl.goss.powergrid.topology.nodebreaker.AnalogLimit;
import pnnl.goss.powergrid.topology.nodebreaker.AnalogLimitSet;
import pnnl.goss.powergrid.topology.nodebreaker.Breaker;
import pnnl.goss.powergrid.topology.nodebreaker.BusbarSection;
import pnnl.goss.powergrid.topology.nodebreaker.ConformLoad;
import pnnl.goss.powergrid.topology.nodebreaker.ConnectivityNode;
import pnnl.goss.powergrid.topology.nodebreaker.Disconnector;
import pnnl.goss.powergrid.topology.nodebreaker.Discrete;
import pnnl.goss.powergrid.topology.nodebreaker.Line;
import pnnl.goss.powergrid.topology.nodebreaker.Terminal;
import pnnl.goss.powergrid.topology.nodebreaker.TransformerWinding;
import pnnl.goss.powergrid.topology.nodebreaker.VoltageLevel;

public class NodeBreakerDao {

	EntityManagerFactory emf;

    EntityManager em;

    public NodeBreakerDao(String persistenceUnitName)
    {
        if (emf == null)
        {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        }
    }
    
    public void persist(Analog entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(AnalogLimit entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(AnalogLimitSet entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(ConformLoad entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(ConnectivityNode entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(Disconnector entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(Terminal entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(Discrete entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(BusbarSection entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    
    public void persist(VoltageLevel entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(Substation substation){
    	em = emf.createEntityManager();
    	
    	em.persist(substation);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(TransformerWinding entity){
    	em = emf.createEntityManager();
    	
    	em.persist(entity);
    	if (em != null)
        {
            em.close();
            em = null;
        }
    }
    
    public void persist(Breaker breaker){
    	em = emf.createEntityManager();
    	
    	em.persist(breaker);
    	if (em != null)
        {
            em.close();
            em = null;
        }    	
    }
    
    public void persist(Line obj){
    	em = emf.createEntityManager();
    	
    	em.persist(obj);
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
}
