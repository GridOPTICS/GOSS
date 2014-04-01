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
package goss.pnnl.fusiondb.datasources;

import goss.pnnl.fusiondb.util.FusionDBConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;

public class FusionDataSource {

	private BasicDataSource connectionPool = null; 
	private static FusionDataSource instance;

	private FusionDataSource(){
		try{
			System.out.println("Connecting to GOSS Metadata store");
			System.out.println("Using GOSS Metadata store at "+FusionDBConfiguration.getProperty(FusionDBConfiguration.CONFIG_DB_URI));
			connectionPool = getDataSourceConnection(FusionDBConfiguration.getProperty(FusionDBConfiguration.CONFIG_DB_URI), FusionDBConfiguration.getProperty(FusionDBConfiguration.CONFIG_DB_USER),
					FusionDBConfiguration.getProperty(FusionDBConfiguration.CONFIG_DB_PW), null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void resetInstance(){
		System.out.println("Resetting GridmwMappingDatasource Instance");
		if(instance!=null){
			try {
				instance.connectionPool.close();
			} catch (SQLException e) {
				System.err.println("Error closing gridmw datasource connection");
			}
			instance = null;
		}
	}
	
	public static FusionDataSource getInstance(){
		try{
			if(instance == null){
				System.out.println("Creating new data store connection");
				instance  = new FusionDataSource();
			}
			return instance;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public Connection getConnection(){
		try{

			return connectionPool.getConnection();
		}
		catch(SQLException e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>
	 * Adds a poolable connection using the passed parameters to connect to the datasource.
	 * </p>
	 */
	public BasicDataSource getDataSourceConnection(String url, String username, String password, String driver) throws Exception {
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

		properties.setProperty("maxOpenPreparedStatements", "10");

		System.out.println("Connecting datasource to url: "+url+" with user: "+username);

		return (BasicDataSource)BasicDataSourceFactory.createDataSource(properties);

	}

}
