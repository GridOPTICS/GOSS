/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pnnl.goss.osgi.vaadin;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossResponseEvent;
import pnnl.goss.core.client.GossClient.PROTOCOL;
import pnnl.goss.gridmw.datamodel.PMUData;
import pnnl.goss.gridmw.requests.RequestPMU;
import pnnl.goss.kairosdb.requests.RequestPMUKairos;
import pnnl.goss.kairosdb.requests.RequestPMUMetaData;
import pnnl.goss.server.core.GossDataServices;
import pnnl.goss.server.core.GossRequestHandlerRegistrationService;

@Component
@Provides
@Instantiate
public class GossServiceImpl implements GossService  {
    
	private volatile GossRequestHandlerRegistrationService registrationService;
	private volatile GossDataServices dataServices;
	GossClient client = null; 
	
	private static final Logger log = LoggerFactory.getLogger(GossServiceImpl.class);


	
	public GossServiceImpl(@Requires GossRequestHandlerRegistrationService registrationService, @Requires GossDataServices dataServices){
		this.registrationService = registrationService;
		this.dataServices = dataServices;
		client = new GossClient(PROTOCOL.OPENWIRE);
		Dictionary config = this.registrationService.getCoreServerConfig();
		client.setConfiguration(config);
		
	}
	
//    public String echo(String message) {
//        return "Echo processed: " + message;
//    }
    

	@Validate
	public void start(){
		System.out.println("STARTING GOSS VAADIN SERVICE");
	}
    
	
	@Invalidate
	public void stop(){
		System.out.println("STOPING GOSS VAADIN SERVICE");
	}
	
	
	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	public String requestPMUData(String startTime, String endTime, String pmus){
		String json = "";
		try{
//			String startTimeStr = request.getParameter("start");
//			if(startTimeStr!=null && startTimeStr.trim().length()>0){
//			
//				Long startTimeParam = Long.valueOf(startTimeStr);
//				Long beginData = sdf.parse(startTime).getTime();
//				if(startTimeParam<beginData){
//					startTimeParam = beginData;
//				}
//				Date d = new Date(startTimeParam);
//				startTime = sdf.format(d);
//			}
//			
//			String endTimeStr = request.getParameter("end");
//			if(endTimeStr!=null && endTimeStr.trim().length()>0){
//				Date d = new Date(Long.valueOf(endTimeStr));
//				endTime = sdf.format(d);
//				if(endTime.equals(startTime)){
//					endTime = "01-04-2010 00:27:00";
//					d = new Date();
//				}
//			}
//			
//			String pmus = request.getParameter("pmus");
//			System.out.println("RETRIEVING DATA "+startTime+" to "+endTime+"  FOR "+pmus);
			
//			String[] pmuStrArr = pmus.split(",");
//			int[] pmuIds = new int[pmuStrArr.length];
//			for(int i=0;i<pmuStrArr.length;i++){
//				pmuIds[i] = new Integer(pmuStrArr[i]);
//			}
			
//			int fiveMin = 300000;
//			startTimeLong = sdf.parse(startTime).getTime();
//			endTimeLong = startTimeLong+fiveMin;
//			finalEndTimeLong = sdf.parse(endTime).getTime();
//			if(endTimeLong>finalEndTimeLong){
//				endTimeLong = finalEndTimeLong;
//			}
			
			boolean bDone = false;
			String[] names = null;
			HashMap<String,HashMap<Long,String>> dataValues = new HashMap<String,HashMap<Long,String>>();
			while(!bDone){
				Date s = new Date();//new Date(startTimeLong);
				Date e = new Date();//new Date(endTimeLong);
				String sStr = sdf.format(s);
				String eStr = sdf.format(e);
				System.out.println(sStr+"     ----      "+eStr);
				RequestPMU requestObj = new RequestPMU("PMU_RAW", sStr,eStr);
				
				
				RequestPMUMetaData request = new RequestPMUMetaData();
				DataResponse response = (DataResponse)client.getResponse(request);
				System.out.println("RESPONSE "+response);
				ArrayList<String> channels = (ArrayList)response.getData();
				
				RequestPMUKairos requestKairos = new RequestPMUKairos("channel", s.getTime(), e.getTime());
				//first request metadata to figure otu which channels, then send a request for each channel
				
				
				String[] requestFor = {"frequency"};
				//int[] pmuIds = {5,6,7,8,9,10,11,12,14,15};
				int[] pmuIds = {5,6};
				requestObj.setPmuNo(pmuIds);
				requestObj.setResponsefor(requestFor);
				//main.sendRequest(requestObj);
				//main.waitForResponse(requestObj);
//				System.out.println("ABOUT TO SEND REQUEST");
				DataResponse responseObj = null;
				try {
					System.out.println("BEFORE RESPONSE");
					responseObj = (DataResponse)client.getResponse(requestObj);
					System.out.println("AFTER RESPONSE");
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				} catch (JMSException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}finally{
					System.out.println("FINALLY");
				}
				System.out.println("GOT RESPONSE OBJ "+responseObj);
				
				if(responseObj!=null){
					Object obj = responseObj.getData();
					if(obj instanceof DataError){
						System.out.println("ERROR OCCURED "+((DataError)obj).getMessage());
						if(((DataError)obj).getMessage().startsWith("Access Denied")){
							//response.setStatus(HttpStatus.SC_FORBIDDEN);
							//TODO throw exception
							throw new Exception("forbidden");
						} else {
//							response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
							throw new Exception("data error "+((DataError)obj).getMessage());
							//TODO throw exception
						}
					} else {
					
						PMUData dataObj = (PMUData)obj;
						String[][] data = dataObj.getValues();
						names = data[0];
						for(int col=1;col<names.length;col++){	
							//String[] dataCol = data[col];
							String name = names[col];
//							System.out.println("NAME "+name);
							log.debug("FIELD NAME "+name);
							HashMap<Long,String> dataMap = new HashMap<Long,String>();
							if(dataValues.containsKey(name)){
								dataMap = dataValues.get(name);
							} else {
								dataValues.put(name, dataMap);
							}
							for(int row=1;row<data.length;){
								Date d = sdf.parse(data[row][0]);
								String val = data[row][col];
								if(!"0.0".equals(val)){
									dataMap.put(d.getTime(),data[row][col]);
									row +=30;					
								} else {
									row +=30;
								}
							}
						}
					}	
				} else {
					throw new Exception("Empty response");
				}
//				if(endTimeLong==finalEndTimeLong){
//					bDone = true;
//				}
//			
//				startTimeLong = endTimeLong;
//				endTimeLong = endTimeLong+fiveMin;
//				if(endTimeLong>finalEndTimeLong){
//					endTimeLong = finalEndTimeLong;
//				}
				//System.out.println("requesting data complete "+data.length);
			}
			
			if(names!=null){
				for(int col=1;col<names.length;col++){	
				
					json+="{\"data\":[";
					//System.out.println("COLUMN "+names[col]);	
					//String[] dataCol = data[col];
					String name = names[col];
					HashMap<Long,String> dataMap = dataValues.get(name);
					//System.out.println("FIRST VAL "+dataCol.length);
					ArrayList<Long> keys = new ArrayList<Long>();
					keys.addAll(dataMap.keySet());
					Collections.sort(keys);
					for(long key:keys){			
						json += "["+key+",\""+dataMap.get(key)+"\"],";
					}
					log.debug("data for "+name+"  "+dataMap.size());
					//need this check because sometimes when it gets 0 values it thinks there are more values, even when there aren't
					if(json.endsWith(",")){
						json = json.substring(0,json.length()-1);
					}
					
					json += "],\"label\":\""+name+"....\"}";
					if(col<names.length-1){
						json += ",";
					}
				}
				
				
				
//				 ListSeries ls = new ListSeries();
//			        ls.setName("Tokyo");
//			        ls.setData(7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3,
//			                13.9, 9.6);
//			        configuration.addSeries(ls);
//			        ls = new ListSeries();
//			        ls.setName("New York");
//			        ls.setData(-0.2, 0.8, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1,
//			                8.6, 2.5);
//			        configuration.addSeries(ls);
//			        ls = new ListSeries();
//			        ls.setName("Berlin");
//			        ls.setData(-0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9,
//			                1.0);
//			        configuration.addSeries(ls);
//			        ls = new ListSeries();
//			        ls.setName("London");
//			        ls.setData(3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6,
//			                4.8);
//			        configuration.addSeries(ls);
				
//				double getyAxisValue = event.getyAxisValue();
//                series.addData(getyAxisValue);
			}
		}catch(Exception e){
			log.error("Error while retrieving PMU data "+e.getMessage(), e);
			throw new RuntimeException(e);
		} //finally{
//			main.close();
//		}
			
		
		json += "]";
			
		return json;
//		response.getWriter().write(json);
		
	}
	
}