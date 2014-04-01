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
package pnnl.goss.security.util.usermgmt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

public class GossLDAPUtils {

	protected static DirContext ctx;
	protected static Logger log = Logger.getLogger(GossLDAPUtils.class);
	
	private static Dictionary configProperties;
	protected static final String GOSS_DEFAULT_LDAP_URL = "ldap://localhost:10389";
	protected static final String GOSS_PROP_SYSTEM_USER = "systemUser";
	protected static final String GOSS_PROP_SYSTEM_PW = "systemPW";
	protected static final String GOSS_PROP_ADMIN_USER = "adminUser";
	protected static final String GOSS_PROP_ADMIN_PW = "adminPW";
	protected static final String GOSS_PROP_LDAP_URL = "ldapURL";
	
	public static final String GOSS_LDAP_GROUP_OU = "ou=groups,ou=goss,ou=system";
	public static final String GOSS_LDAP_USER_OU = "ou=users,ou=goss,ou=system";
	public static final String GOSS_LDAP_DESTINATION_OU = "ou=Destination,ou=goss,ou=system";
//	public static final String GOSS_LDAP_ROLE_OU = "ou=roles,ou=goss,ou=system";
	
	public static DirContext getLDAPContext(){
		if(ctx==null){
			
			String ldapURL = getProperty(GOSS_PROP_LDAP_URL);
			if(ldapURL==null || ldapURL.trim().length()==0){
				log.warn("Warning: could not find ldapURL in properties, defaulting to "+GOSS_DEFAULT_LDAP_URL);
				ldapURL = GOSS_DEFAULT_LDAP_URL;
			}
			// Set up environment for creating initial context
	        Hashtable<String, Object> env = new Hashtable<String, Object>(11);
		    env.put(Context.INITIAL_CONTEXT_FACTORY, 
		        "com.sun.jndi.ldap.LdapCtxFactory");
		    env.put(Context.PROVIDER_URL, ldapURL);
		    env.put(Context.SECURITY_PRINCIPAL, getProperty(GOSS_PROP_ADMIN_USER));
		    env.put(Context.SECURITY_CREDENTIALS, getProperty(GOSS_PROP_ADMIN_PW));
			
			// Create initial context
			try {
		        ctx = new InitialDirContext(env);
			} catch (NamingException e) {
				log.error("Error creating ldap context with "+ldapURL,e);
				e.printStackTrace();
			}
		}
		
		return ctx;
	}
	
	public static void resetContext(){
		if(ctx!=null){
			try {
				ctx.close();
				ctx = null;
			} catch (NamingException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static String getProperty(String propertyName){
		if(configProperties!=null){
			return (String)configProperties.get(propertyName);
		} else {
			log.warn("Security util Configuration not found, loading from file");
			
			Properties props = loadFromFile();
			return props.getProperty(propertyName);
		}
		
	}

	public static void setConfigProperties(Dictionary props){
		configProperties = props;
	}
	
	protected static Properties loadFromFile(){
		Properties configProperties = new Properties();
		
		// Grabs the config file from the resources path which is on the class path.
		InputStream input = GossLDAPUtils.class.getResourceAsStream("/config.properties");
		try {
			configProperties.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return configProperties;
	}
}
