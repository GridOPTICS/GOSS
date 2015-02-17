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
package pnnl.goss.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Response;
import static pnnl.goss.core.GossCoreContants.PROP_CORE_CONFIG;

import com.thoughtworks.xstream.XStream;

public class Utilities {
	
	private static Utilities instance;
	private static Properties properties = new Properties();
	private static URI brokerURI;
	private static final Logger log = LoggerFactory.getLogger(Utilities.class);
	
	private Utilities(){
		
	}
	
	public static Utilities getInstance() {
		if(instance == null)  {
			Utilities util = new Utilities();
			
			loadProperties();
			
			instance = util;
		}
		return instance;
	}

	public static String getEntryString(String requestId, long timestamp,
			String type) {
		return new String(requestId + "\t" + timestamp + "\t" + type + "\n");
	}

	public static boolean writeToFile(String fileFormat, String path,
			String fileName, Response response) {
		try {
			XStream xStream = Utilities.getAliasedXStream();
			String serializedResponse = xStream.toXML(response);
			File responseFile = new File(path, fileName);
			if (!responseFile.exists())
				responseFile.createNewFile();
			FileUtils.writeStringToFile(responseFile,
					StringEscapeUtils.unescapeHtml(serializedResponse));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static XStream getAliasedXStream() {
		XStream xStream = new XStream();
		xStream.alias("Response", Response.class);
		return xStream;
	}
	
	/**
	 * Create a dictionary of properties from the passed properties file.  The evaluation of
	 * relative path is as follows.
	 * 
	 *  if ${user.home}/.goss/path.${hostname} exists use it
	 *  else if ${user.home}/.goss/path if exist us it
	 *  else if path exists use it
	 *  else throw configuration exception.
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public static Dictionary loadProperties(String path){
		return loadProperties(path, true);
	}
	
	@SuppressWarnings("rawtypes")
	public static Dictionary loadProperties(String path, boolean iskarafconfig){
		Properties props = new Properties();
		try{
			// All configuration files now end with .cfg except the one that
			// ends in the hostname.
			if (!path.endsWith(".cfg") && iskarafconfig){
				path += ".cfg";
			}
			
			// Are we running in a karaf context?  If so then
			// use a file from etc below.
			String karafBase = System.getProperty("karaf.base");
			
			// The machine where this script is running.
			String hostname = getHostname();
			
			if (karafBase != null){
				path = Paths.get(karafBase, "etc", path).toString();
			}
			if(path!=null){
 
				InputStream input = Utilities.class.getClassLoader().getResourceAsStream(path+"."+hostname);
				if (input!=null){
					log.debug("loading properties from:\n\t"+ path+"."+hostname);					
					props.load(input);
				}
				else{
					input = Utilities.class.getClassLoader().getResourceAsStream(path);
					if(input!=null){
						log.debug("loading properties from:\n\t"+ path);
						props.load(input);
					}
					else{
						File file = new File(path);
						if (file.exists()){
							input = new FileInputStream(file);
							if(input != null){
								log.debug("loading properties from:\n\t"+ path);
								props.load(input);
							}
						}
						else{
							log.debug("Couldn't find properties for path: \n\t"+path);
						}
					}
				}
			}
			else
				throw new Exception("Config path not found!");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return props;
	}
	
	/**
	 * Converts a Dictionary object to a new Properties object.
	 * @param dict
	 * @return
	 */
	public static Properties toProperties(Dictionary dict){
		Properties properties = new Properties();
		Enumeration nummer = dict.keys();
		
		while(nummer.hasMoreElements()){
			String key = (String)nummer.nextElement();
			properties.setProperty(key, (String) dict.get(key));
		}
		
		return properties;
	}
	
	private static String getHostname(){
		String hostname = null;

		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
		}
		catch (UnknownHostException ex)
		{
		    System.out.println("Hostname can not be resolved");
		}
		
		return hostname;
	}
	
	/**Loads properties from config.properties into memory */
	public static void loadProperties(){
		try{
			InputStream input = Utilities.class.getResourceAsStream(PROP_CORE_CONFIG);
			if(input!=null){
				properties.load(input);
			}
			else{
				String filePath = Paths.get("config", PROP_CORE_CONFIG + ".cfg").toString();
				properties.load(new FileInputStream(filePath));
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String str){
			return properties.getProperty(str);
	}
	
	public static void setbrokerURI(URI uri){
		brokerURI = uri;
		
	}
	
	public static URI getbrokerURI(){
		return brokerURI;
		
	}

}
