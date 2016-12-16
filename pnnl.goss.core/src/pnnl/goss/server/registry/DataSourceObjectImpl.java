package pnnl.goss.server.registry;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceType;

/**
 * An internal (non-service) implementation of DataSourcePooledJdbc interface.  This
 * allows the use of the PooledBasicDataSourceBuilderImpl to make use of this class
 * when registering it with the DataSourceRegistry.
 * 
 * @author Craig Allwardt
 *
 */
public class DataSourceObjectImpl implements DataSourcePooledJdbc {
	
	private static final Logger log = LoggerFactory.getLogger(DataSourceObjectImpl.class);
	private String name;
	private DataSourceType datsourceType;
	private DataSource datasource;
	
	/**
	 * Construct a new DataSourceObject with the specified name(key), datasourceType and datasource
	 * 
	 * @param name
	 * @param dataSourceType
	 * @param ds
	 */
	public DataSourceObjectImpl(String name, DataSourceType dataSourceType, DataSource ds) {
		this.name = name;
		this.datsourceType = dataSourceType;
		this.datasource = ds;
		log.debug("Created "+DataSourceObjectImpl.class.getName()+ " for ds: "+name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DataSourceType getDataSourceType() {
		// TODO Auto-generated method stub
		return datsourceType;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return datasource.getConnection();
	}
	


}
