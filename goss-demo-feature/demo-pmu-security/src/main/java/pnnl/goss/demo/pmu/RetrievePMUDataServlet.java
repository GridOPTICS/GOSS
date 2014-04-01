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
package pnnl.goss.demo.pmu;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.demo.security.util.DemoConstants;
import pnnl.goss.gridmw.datamodel.PMUData;
import pnnl.goss.gridmw.requests.RequestPMU;

@SuppressWarnings("serial")
public class RetrievePMUDataServlet extends HttpServlet { 
	private static final Logger log = LoggerFactory.getLogger(RetrievePMUDataServlet.class);

	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		String userName = (String)session.getAttribute(DemoConstants.USERNAME_CONSTANT);
		String pw = (String)session.getAttribute(DemoConstants.PASSWORD_CONSTANT);
		
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//		System.out.println("requesting frequencies");
		String startTime = "01-04-2010 00:00:00";
		String endTime = "01-04-2010 00:27:00";
		long startTimeLong=0;
		long endTimeLong = 0;
		long finalEndTimeLong = 0;
		
		String json="[";
		GossClient main = new GossClient(new UsernamePasswordCredentials(userName, pw));
		
		try{
			String startTimeStr = request.getParameter("start");
			if(startTimeStr!=null && startTimeStr.trim().length()>0){
			
				Long startTimeParam = Long.valueOf(startTimeStr);
				Long beginData = sdf.parse(startTime).getTime();
				if(startTimeParam<beginData){
					startTimeParam = beginData;
				}
				Date d = new Date(startTimeParam);
				startTime = sdf.format(d);
			}
			
			String endTimeStr = request.getParameter("end");
			if(endTimeStr!=null && endTimeStr.trim().length()>0){
				Date d = new Date(Long.valueOf(endTimeStr));
				endTime = sdf.format(d);
				if(endTime.equals(startTime)){
					endTime = "01-04-2010 00:27:00";
					d = new Date();
				}
			}
			
			String pmus = request.getParameter("pmus");
			System.out.println("RETRIEVING DATA "+startTime+" to "+endTime+"  FOR "+pmus);
			
			String[] pmuStrArr = pmus.split(",");
			int[] pmuIds = new int[pmuStrArr.length];
			for(int i=0;i<pmuStrArr.length;i++){
				pmuIds[i] = new Integer(pmuStrArr[i]);
			}
			
			int fiveMin = 300000;
			startTimeLong = sdf.parse(startTime).getTime();
			endTimeLong = startTimeLong+fiveMin;
			finalEndTimeLong = sdf.parse(endTime).getTime();
			if(endTimeLong>finalEndTimeLong){
				endTimeLong = finalEndTimeLong;
			}
			
			boolean bDone = false;
			String[] names = null;
			HashMap<String,HashMap<Long,String>> dataValues = new HashMap<String,HashMap<Long,String>>();
			while(!bDone){
	
				Date s = new Date(startTimeLong);
				Date e = new Date(endTimeLong);
				String sStr = sdf.format(s);
				String eStr = sdf.format(e);
				System.out.println(sStr+"     ----      "+eStr);
				RequestPMU requestObj = new RequestPMU("PMU_RAW", sStr,eStr);
				String[] requestFor = {"frequency"};
				//int[] pmuIds = {5,6,7,8,9,10,11,12,14,15};
//				int[] pmuIds = {5,6};
				requestObj.setPmuNo(pmuIds);
				requestObj.setResponsefor(requestFor);
				//main.sendRequest(requestObj);
				//main.waitForResponse(requestObj);
//				System.out.println("ABOUT TO SEND REQUEST");
				DataResponse responseObj = null;
				try {
					responseObj = (DataResponse)main.getResponse(requestObj);
					
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
				} catch (JMSException e1) {
					e1.printStackTrace();
				}
//				System.out.println("GOT RESPONSE OBJ "+responseObj);
				
				if(responseObj!=null){
					Object obj = responseObj.getData();
					if(obj instanceof DataError){
						System.out.println("ERROR OCCURED "+((DataError)obj).getMessage());
						if(((DataError)obj).getMessage().startsWith("Access Denied")){
							response.setStatus(HttpStatus.SC_FORBIDDEN);
						} else {
							response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
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
				}
				if(endTimeLong==finalEndTimeLong){
					bDone = true;
				}
			
				startTimeLong = endTimeLong;
				endTimeLong = endTimeLong+fiveMin;
				if(endTimeLong>finalEndTimeLong){
					endTimeLong = finalEndTimeLong;
				}
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
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			main.close();
		}
			
		
		json += "]";
			
		
		response.getWriter().write(json);
		
	}
	
}
