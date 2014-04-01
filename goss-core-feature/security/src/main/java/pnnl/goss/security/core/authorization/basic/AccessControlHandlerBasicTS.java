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
package pnnl.goss.security.core.authorization.basic;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import pnnl.goss.security.core.GossSecurityHandlerImpl;
import pnnl.goss.security.core.authorization.AbstractAccessControlHandler;
import pnnl.goss.security.core.internal.SecurityConfiguration;



/**
 * @author tara
 *
 */
public abstract class AccessControlHandlerBasicTS extends AbstractAccessControlHandler {

//	private static String databaseUri="jdbc:mysql://localhost:3306/gridopticsdb";
//	private static String databaseDriverClass="com.mysql.jdbc.Driver";
	private static Logger log = LoggerFactory.getLogger(AccessControlHandlerBasicTS.class);
	
	private static DataSource dataSourceConnection;
	private static HashMap<String, CachedRoles> cachedRoleMappings = new HashMap<String, CachedRoles>();
	private static final long EXPIRE_TIME = 3600000;
	
	
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	protected List<String> getAllowedRoles(String datatype, String[] sources, long dataAge, String accessLevel){
		List<String> roles = new ArrayList<String>();
		//TODO  check the db
		try{
			String sourcesStr = StringUtils.join(sources, ",");
			
			String keyStr = datatype+sourcesStr+dataAge+accessLevel;
			if(cachedRoleMappings.containsKey(keyStr)){
				CachedRoles c = (CachedRoles)cachedRoleMappings.get(keyStr);
				if(c!=null && !c.isExpired()){
					return c.getRoles();
				} else if(c!=null){
					cachedRoleMappings.remove(keyStr);
				}
			}
			
			
			Stack<String> remainingRoles = new Stack<String>();
			//select role from accesstable where datatype='datatype' and source in (sources...) and (age=-1 or age<dataAge) and accessLevel='accesLevel'
			String queryStr = "select roles from securitypolicy where dataType='"+datatype+"' and source in ("+sourcesStr+") and (age=-1 or age>="+dataAge+") and accessLevel='"+accessLevel+"'";
			log.info("DB QUERY "+queryStr);
			Connection conn = getDBConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(queryStr);
			while(rs.next()){
				String rolesStr = rs.getString(1);
				if(rolesStr!=null && rolesStr.trim().length()>0){
					if(!remainingRoles.contains(rolesStr.trim())){
							remainingRoles.push(rolesStr.trim());
						}
				}
			}
			
			
			if(!remainingRoles.isEmpty()){
				//Go through returned roles and get every combination of allowed roles valid for the request, for example if the request
				// contains some sources from utility 1 and some from utility 2, then it should require the user to have both of those roles
				List<List<String>> aggRoles = new ArrayList<List<String>>();
				String firstRoles = remainingRoles.pop();
				String[] rArr = firstRoles.split(",");
				for(String r: rArr){
					List<String> tmp = new ArrayList<String>();
					tmp.add(r);
					aggRoles.add(tmp);
				}
				
				List<List<String>> newAggRoles = addDistinctRoles(remainingRoles, aggRoles);
				for(List<String> aggRole: newAggRoles){
					StringBuilder sb = new StringBuilder();
					for (String s : aggRole)
					{
					    sb.append(s);
					    sb.append(":");
					}
					roles.add(sb.toString());
				}
			}
			
			cachedRoleMappings.put(keyStr, new CachedRoles(roles));
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return roles;
	}
	
	
	protected void grantAccess(String datatype, String[] sources, long dataAge, String accessLevel, List<String> roles){
		//TODO
	}
	protected void revokeAccess(String datatype, String[] sources, long dataAge, String accessLevel, List<String> roles){
		//TODO
	}

	
	/**
	 * 
	 * @param remainingRoles
	 * @param aggRoles
	 * @return
	 */
	private List<List<String>> addDistinctRoles(Stack<String> remainingRoles, List<List<String>> aggRoles){
		if(remainingRoles.isEmpty())
			return aggRoles;
		
		List<List<String>> newAggRoles = new ArrayList<List<String>>();
		String rolesStr = remainingRoles.pop();
		String[] rArr = rolesStr.split(",");
		for(String r: rArr){
			
			for(List<String> aggStr: aggRoles){
				List<String> newAggStr = new ArrayList<String>();
				newAggStr.addAll(aggStr);
				if(!newAggStr.contains(r.trim())){
					newAggStr.add(r.trim());
				}
				newAggRoles.add(newAggStr);
			}
		}
		return addDistinctRoles(remainingRoles, newAggRoles);
	}
	
	protected Properties getConfiguration() throws IOException {
//		Properties configProperties = new Properties();
//		// Grabs the config file from the resources path which is on the class path.
//		InputStream input = AccessControlHandlerBasicTS.class.getResourceAsStream("/goss.core.security.cfg");
//		configProperties.load(input);
		
		return SecurityConfiguration.getConfig();
		
	}
	
	protected Connection getDBConnection() throws SQLException{
		if(dataSourceConnection==null ){
			try {
				dataSourceConnection = getDataSourceConnection();
			} catch (Exception e) {
				e.printStackTrace();
				throw new SQLException(e.getMessage());
			}
		}
		
		if(dataSourceConnection!=null){
			return dataSourceConnection.getConnection();
		}
		return null;
	}
		
		
		/**
		 * <p>
		 * Adds a poolable connection using the passed parameters to connect to the datasource.
		 * </p>
		 */
		public DataSource getDataSourceConnection() throws Exception {
			Properties properties =getConfiguration();
			
			String url = properties.getProperty("ac_databaseURI");
			String username = properties.getProperty("ac_databaseUser");
			String password = properties.getProperty("ac_databasePW");
			String driver = properties.getProperty("ac_databaseDriver");
			
			// Available properties http://commons.apache.org/proper/commons-dbcp/xref-test/org/apache/commons/dbcp/TestBasicDataSourceFactory.html#50
			if (driver == null || driver.trim().equals("")){
				properties.setProperty("driverClassName", "com.mysql.jdbc.Driver"); 
			}
			else{
				properties.setProperty("driverClassName", driver);
			}
			
			Class.forName(properties.getProperty("driverClassName"));
			
//			properties.setProperty("url", url);
//			properties.setProperty("username", username);
//			properties.setProperty("password", password);
			MysqlConnectionPoolDataSource pooledDs = new MysqlConnectionPoolDataSource();
			
			pooledDs.setUser(username);
			pooledDs.setPassword(password);
			pooledDs.setUrl(url);
			pooledDs.setConnectTimeout(300);
			pooledDs.setAllowMultiQueries(true);
			
			
			log.info("Connecting datasource to url: "+url+" with user: "+username);
			System.out.println("Connecting datasource to url: "+url+" with user: "+username+" instance "+this);
			
			return pooledDs;
			
		}
		
		@Override
			protected void finalize() throws Throwable {
//				if(dataSourceConnection!=null){
//					dataSourceConnection.close();
//				}
				super.finalize();
			}
		
		
		private class CachedRoles {
			List<String> roles;
			long expires;
			public CachedRoles(List<String> roles) {
				this.roles = roles;
				this.expires = new Date().getTime()+EXPIRE_TIME;
			}
			
			public List<String> getRoles(){
				return roles;
			}
			
			
			public boolean isExpired(){
				long d = new Date().getTime();
				return d>expires;
			}
			
		}
}
