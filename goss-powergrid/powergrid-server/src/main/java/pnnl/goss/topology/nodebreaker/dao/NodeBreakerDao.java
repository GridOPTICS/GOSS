package pnnl.goss.topology.nodebreaker.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import pnnl.goss.powergrid.topology.NodeBreakerDataType;

public class NodeBreakerDao {

	EntityManagerFactory emf;

	EntityManager em;

	public NodeBreakerDao(String persistenceUnitName) {
		if (emf == null) {
			emf = Persistence.createEntityManagerFactory(persistenceUnitName);
		}
	}

	public void persist(NodeBreakerDataType entity) {
		try {
			// System.out.println("Saving: "+entity.getClass().toString()+" "+entity.toString());
			em = emf.createEntityManager();

			em.persist(entity);
			if (em != null) {
				em.close();
				em = null;
			}
		} finally {
			if (em != null) {
				em.close();
				em = null;
			}
		}
	}

	public Object get(Class<?> entityClass, String id) {
		EntityManager em = emf.createEntityManager();
		Object p = em.find(entityClass, id);
		return p;
	}
}
