/*
	Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
*/
package pnnl.goss.powergrid.server.datasources;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.server.core.InvalidDatasourceException;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;


public class PowergridDataSources {

	/**
	 * <p>
	 * A collection of pooled datasources to be used from the server.
	 * </p>	
	 */
	private static HashMap<String, DataSource> datasources = new HashMap<String, DataSource>();
	
	/**
	 * <p>
	 * Add logging to the class so that we can debug things effectively after deployment.
	 * </p>
	 */
	private static Logger log = LoggerFactory.getLogger(PowergridDataSources.class);
	
	/**
	 * <p>
	 * A singleton instance for the powergrid datasources.
	 * </p>
	 */
	private static volatile PowergridDataSources instance = null;
	
	/**
	 * <p>
	 * The default key to be used for retrieving data.
	 * </p>
	 */
	private static String defaultDatasourceKey = null;
	
	/**
	 * <p>
	 * Private constructor for <code>PowergridDatasources</code>
	 * </p>
	 */
	private PowergridDataSources(){
		// private constructor.
	}
	
	/**
	 * <p>
	 * Retrieves the instance to the datasources.
	 * </p>
	 */
	public static PowergridDataSources instance(){
				
		if (instance == null){
			// Handle race condition between thread instancing.
			synchronized (PowergridDataSources.class){
				if (instance == null){
					instance = new PowergridDataSources();					
				}				
			}
		}
		return instance;
	}
	
	/**
	 * <p>
	 * Returns the datasourcekey where the powergrid name is located.  If the
	 * powergridname is not found in the set then return null.
	 * </p>
	 * @param mysqlDao
	 * @param powergridName
	 * @return
	 */
	public String getDatasourceKeyWherePowergridName(PowergridDaoMySql mysqlDao, String powergridName){
		for(String key:PowergridDataSources.datasources.keySet()){
			mysqlDao.setDatasource(PowergridDataSources.datasources.get(key));
			if(mysqlDao.getPowergridNames().contains(powergridName)){
				return key;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the name of all powergrids throughout all of the datasources..
	 * @return
	 */
	public List<Powergrid> getAllPowergrids(){
		PowergridDaoMySql dao = new PowergridDaoMySql();
		List<Powergrid> powergrids = new ArrayList<Powergrid>();
		for(String key:PowergridDataSources.datasources.keySet()){
			dao.setDatasource(PowergridDataSources.datasources.get(key));
			powergrids.addAll(dao.getAvailablePowergrids());
		}
		
		return powergrids;
	}
	
	public String getDefaultDatasourceKey(){
		return defaultDatasourceKey;
	}
	
	public Connection getDefaultConnection() throws InvalidDatasourceException, SQLException{
		if (defaultDatasourceKey == null)
			throw new InvalidDatasourceException("Default connection hasn't been specified.");
		return getConnection(defaultDatasourceKey);
	}
	
	/**
	 * <p>
	 * Retireves the keys of the datasources so clients can reference them.
	 * </p>
	 */
	public List<String> getDatasourceKeys(){
		return new ArrayList<String>(datasources.keySet());
	}
		
	/**
	 * <p>
	 * Retrieve a connection object from the pooled datasource
	 * </p>
	 */
	public Connection getConnection(String dsKey) throws SQLException, InvalidDatasourceException {
		Connection connection = null;

		if (!datasources.containsKey(dsKey)) {
			throw new InvalidDatasourceException("Unknown datasource: " + dsKey);
		}

		if (datasources.containsKey(dsKey)) {
			connection = datasources.get(dsKey).getConnection();
		} else {
			throw new InvalidDatasourceException("Unknown datasource "+dsKey+" refered to.");	}

		log.debug("returning connection for: "+dsKey+" datasource.");
		return connection;
	}

	/**
	 * <p>
	 * A short cut for getting a connection when there is only a single datasource available.
	 * </p>
	 */
	public Connection getConnection() throws SQLException, InvalidDatasourceException {

		if (datasources.size() > 1) {
			throw new InvalidDatasourceException("More that one datasource call getConnection(dsKey) instead.");
		}
	
		// Lookup the datasource key.
		Set<String> keys = datasources.keySet();

		String dsKey = keys.iterator().next();
		return getConnection(dsKey);
	}

	/*<p>
	 * Closes all connections (if open) and clears the collection of datasources.
	 * </p>
	 */
	public void shutdown() {
		/*
		for (DataSource ds: datasources.values()){
			
			if(!ds.isClosed()){
				try {
					ds.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}*/
		datasources.clear();
	}
	
	/* <p>
	 * Returns whether or not a datasource exists in the collection of datasources.
	 * </p> 
	 */
	public boolean contains(String datasource){
		return datasources.containsKey(datasource);
	}
	
	/**
	 * Add connections from a config dictionary.  The dictionary must have a base name of
	 * datasourceBase with the suffix starting at 0.  In addition a possible additional property
	 * datasourcedefault may be available for use.  It should reference one of the other
	 * datasource keys.  If it is not present then the first (Zeroth) datasource will be chosen
	 * as the default.  
	 * 
	 * Example:
	 * name,uri,username,password,(optional)driver class
	 * datasource0=northandsouth,jdbc:mysql://localhost:3306/northandsouth,root,password,com.mysql.jdbc.Driver
	 * datasource1=north,jdbc:mysql://localhost:3306/north,root,password
	 * 
	 * datasourcedefault=datasource1
	 * 
	 * @param config
	 * @param datasourceBase
	 * @throws InvalidDatasourceException 
	 * @throws SQLException 
	 */
	@SuppressWarnings("rawtypes") 
	public void addConnections(Dictionary config, String datasourceBase) throws SQLException, InvalidDatasourceException{
		int i=0;
		String datasource = datasourceBase+i;
		String defaultKey = datasourceBase+"default";
		
		while(config.get(datasource) != null){
			String properties[] = ((String)config.get(datasource)).split(",");
			
			// name,uri,username,password,(optional)driver class| mysql is the default.
			try {
				if(properties.length > 4){
					addConnection(properties[0], properties[1],properties[2],properties[3], properties[4]);
				}
				else{
					addConnection(properties[0], properties[1],properties[2],properties[3], null);
				}
				
				if (config.get(defaultKey) != null){
					// if we have a default then set it as we load the datasources.
					if (config.get(defaultKey).equals(datasource)){
						defaultDatasourceKey = properties[0];
					}
					
				}
				
			} catch (Exception e) {
				log.error("Datasource connection could not be established! datasource"+i, e);
				e.printStackTrace();
			}
			
			i++;
			datasource = datasourceBase+i;
		}
		
		if (datasources.size() == 0){
			throw new InvalidDatasourceException("No datasources found in the configuration file.");
		}
	}
	
	public DataSource getConnectionPool(String datasourceKey){
		return datasources.get(datasourceKey);
	}

	/**
	 * <p>
	 * Adds a poolable connection using the passed parameters to connect to the datasource.
	 * </p>
	 */
	public void addConnection(String key, String url, String username, String password, String driver) throws Exception {
		Properties properties = new Properties();
		
		// Available properties http://commons.apache.org/proper/commons-dbcp/xref-test/org/apache/commons/dbcp/TestBasicDataSourceFactory.html#50
		if (driver == null || driver.trim().equals("")){
			properties.setProperty("driverClassName", "com.mysql.jdbc.Driver"); 
		}
		else{
			properties.setProperty("driverClassName", driver);
		}
		Class.forName(properties.getProperty("driverClassName"));
		properties.setProperty("url", url);
		properties.setProperty("username", username);
		properties.setProperty("password", password);
		properties.setProperty("max-connections", "10");
		properties.setProperty("maxOpenPreparedStatements", "10");
		
		MysqlConnectionPoolDataSource pooledDs = new MysqlConnectionPoolDataSource();
		
		pooledDs.setUser(username);
		pooledDs.setPassword(password);
		pooledDs.setUrl(url);
		pooledDs.setConnectTimeout(32000);
		pooledDs.setAllowMultiQueries(true);
		
		datasources.put(key, pooledDs); 
		
	}
}
