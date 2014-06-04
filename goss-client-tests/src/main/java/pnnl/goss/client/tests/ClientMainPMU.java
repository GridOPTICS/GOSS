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
package pnnl.goss.client.tests;

import java.io.FileWriter;
import java.util.ArrayList;

import pnnl.goss.client.tests.util.ClientAuthHelper;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.gridmw.datamodel.GridMWTestData;
import pnnl.goss.gridmw.datamodel.PMUData;
import pnnl.goss.gridmw.requests.RequestGridMWTest;
import pnnl.goss.gridmw.requests.RequestPMU;
import pnnl.goss.kairosdb.requests.RequestPMUKairos;
import pnnl.goss.kairosdb.requests.RequestPMUMetaData;


public class ClientMainPMU {

	public static void main(String args[]) {

		//dataCleaningNew();
		//gridmwTest();
		kairos();

	}


	private static void dataCleaningNew(){
		
		try{
		
		System.out.println("Gettig Raw Data");
		FileWriter logWriter = new FileWriter("gridMW.log",true);
		FileWriter responseWriter = new FileWriter("gridMW_response.log",true);
		//get raw data
		String startTime = "01-04-2010 07:00:00";
		String endTime = "01-04-2010 07:05:00"; //Data upto "01-04-2010 07:59:59"
		String[] responsefor = {"frequency"};
		int[] pmuNo = {1};
		RequestPMU request = new RequestPMU("PMU_RAW",startTime, endTime);
		request.setPmuNo(pmuNo);
		request.setResponsefor(responsefor);
				
		GossClient main = new GossClient(ClientAuthHelper.getPMUCredentials());
		
		for(int channel=1;channel<=100;channel++){
			long perfStartTime = System.currentTimeMillis();
			DataResponse response = (DataResponse)main.getResponse(request);
			logWriter.write(String.valueOf(System.currentTimeMillis()-perfStartTime)+"\n");
			
			if(response.getData() instanceof PMUData){
				PMUData pmuData = (PMUData)response.getData();
				for(int i=0;i<pmuData.getValues()[0].length;i++){
					for(int col=0;col<pmuData.getValues().length;col++)
					responseWriter.write(String.valueOf(pmuData.getValues()[col][i])+",");
				}
				responseWriter.write("\n");
			}
			else if(response.getData() instanceof DataError){
				DataError error = (DataError)response.getData();
				System.out.println(error.getMessage());
			}
			
		}
		logWriter.close();
		responseWriter.close();

		main.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	private static void gridmwTest(){
		
		try{
			GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
			RequestGridMWTest request = new RequestGridMWTest(1, 1270105200, 1270105500);
			request.setResponseFormat(RESPONSE_FORMAT.XML);
			
			//for(int i=1;i<10;i++){
			FileWriter logWriter = new FileWriter("gridMW_run.log",true);
			//FileWriter responseWriter = new FileWriter("gridMW_response.log",true);
			logWriter.write("GridMW,GridMW+GOSS\n");
			for(int channel=1;channel<=1000;channel++){
				long perfStartTime = System.currentTimeMillis();
				GridMWTestData data  = (GridMWTestData)((DataResponse)client.getResponse(request)).getData();
				logWriter.write(data.getTime()+","+String.valueOf(System.currentTimeMillis()-perfStartTime)+"\n");
				//GridMWTestData pmuData = (GridMWTestData)response.getData();
				//for(int i=0;i<pmuData.getValues().length;i++)
				//	responseWriter.write(String.valueOf(pmuData.getValues()[i])+",");
				//responseWriter.write("\n");
			}
			logWriter.close();
			//responseWriter.close();
			
			//}
			client.close();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private static void kairos(){
		
		
		DataResponse response=null;
		try{
		
			//pmu data available for time from 1270105200 to 1270108799
			long startTime = 1270105200;
			long endTime = 1270105201;
			
			//1. get all available pmu channels
			RequestPMUMetaData request = new RequestPMUMetaData();
			GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
			response = (DataResponse)client.getResponse(request);
			ArrayList<String> channels = (ArrayList)response.getData();
			
			//2. iterate over pmu channels
			for(String channel: channels){
				System.out.println(channel);
				
				//3. Get pmu values for given time range
				RequestPMUKairos requestPmu = new RequestPMUKairos(channel,startTime,endTime);
				response = (DataResponse)client.getResponse(requestPmu);
				ArrayList<Float> values = (ArrayList)response.getData();
				for(int i=0; i<values.size(); i++)
					System.out.print(values.get(i)+" , ");
				System.out.print("\n");
			}
			
			client.close();
		
		}
		catch(Exception e){
			DataError error = (DataError)response.getData();
			System.out.println(error.getMessage());
			e.printStackTrace();
		}
			
			
	}

}