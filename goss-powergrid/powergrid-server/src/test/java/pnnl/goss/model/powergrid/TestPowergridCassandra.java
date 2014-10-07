package pnnl.goss.model.powergrid;

import java.sql.SQLException;
import java.util.List;

import javax.activation.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import pnnl.goss.powergrid.dao.PowergridDao;
import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.powergrid.server.impl.PowergridPersist;
import pnnl.goss.server.core.BasicDataSourceCreator;

public class TestPowergridCassandra {

	public static final String PERSISTENCE_UNIT="powergrid_cass_pu";
	
	public static void main(String[] args) throws Exception {
		
		BasicDataSourceCreator dsc = new BasicDataSourceCreator();
		BasicDataSource datasource = null;
		try {
			datasource = dsc.create("jdbc:mysql://we22743:3306/north", "root", "rootpass");
			PowergridDao persist = new PowergridPersist(PERSISTENCE_UNIT);
			PowergridDao pgDao = new PowergridDaoMySql(datasource);
			
			List<Powergrid> grids = pgDao.getAvailablePowergrids();
			for(Powergrid g: grids){
				System.out.println(g.getName());
				persist.persist(g);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(datasource != null && !datasource.isClosed())
				datasource.close();
		}
		//powergrid.datasource.1=north,jdbc:mysql://localhost:3306/north,powergrid,rootpass
		//dsc.create(url, username, password)
		
		
		
		
		

	}

}
