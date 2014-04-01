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
import java.util.Dictionary;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

import pnnl.goss.core.Response;

import com.thoughtworks.xstream.XStream;

public class Utilities {
	
	private static Utilities instance;
	private static Properties properties = new Properties();
	private static URI brokerURI;
	
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
	
	@SuppressWarnings("rawtypes")
	public static Dictionary loadProperties(String path){
		Properties props = new Properties();
		try{
			String hostname = getHostname();
			if(path!=null){
				InputStream input = Utilities.class.getClassLoader().getResourceAsStream(path+"."+hostname);
				if (input!=null){
					System.out.println("Uploading configuration file = "+ path+"."+hostname);
					props.load(input);
				}
				else{
					input = Utilities.class.getClassLoader().getResourceAsStream(path);
					if(input!=null){
						System.out.println("Uploading configuration file = "+ path);
						props.load(input);
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
			InputStream input = Utilities.class.getResourceAsStream("/config.properties");
			if(input!=null)
				properties.load(input);
			else
				properties.load(new FileInputStream("config"+File.separatorChar+"config.properties"));
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
