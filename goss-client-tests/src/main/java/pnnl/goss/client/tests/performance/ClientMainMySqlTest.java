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
package pnnl.goss.client.tests.performance;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.client.tests.util.ClientAuthHelper;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossResponseEvent;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegmentTest;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoadAsyncTest;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoadTest;

public class ClientMainMySqlTest {

	private static Logger log = LoggerFactory.getLogger(ClientMainMySqlTest.class);
	
	public static void main(String args[]){
		try{
		String typeOfCommunication = "s";
		int noOfClients =375;
		int noOfLines = 10;
		String startTime = "2013-08-01 10:00:00";
		String endTime = "2013-08-01 10:05:00";
		int segment = 3;

		if(typeOfCommunication.equals("s"))
			synchronousTest(noOfClients, noOfLines);
		else
			asynchronousTest(noOfClients, noOfLines, startTime, endTime, segment);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
//50 - 4793
//100 - 13154
//200 - 19309
//300 - 35504

	static void synchronousTest(int noOfClients, int noOfChannels) throws IOException{
		System.out.println("Client_No,Request_No,StartTime,EndTime,TotalTime,StartTimeDB,EndTimeDB,TimeTakenByDB,TimeTakenByGOSS");
		for(int clientNo=1;clientNo<=noOfClients;clientNo++){
			final int noOfLines = noOfChannels;
			final int clientNum = clientNo;
			//final int totalClients = noOfClients;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					DataResponse response=null;		
					try{
						//FileWriter timeWriter = new FileWriter("mysql_synchronous_client"+clientNum+".log",true);
						//System.out.println("Start,"+System.currentTimeMillis());
						long startTime = System.currentTimeMillis();
						GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
						RequestLineLoadTest request = null;
						//FileWriter logWriter = new FileWriter("mysql_synchronous_client"+clientNum+".log",true);
						//logWriter.write("MySQL;MySQL+GOSS\n");
						for(int i=1;i<=noOfLines;i++){
								//System.out.println("Client="+clientNum+" Request=+"+i);
								request = new RequestLineLoadTest("Greek-118-North",null,noOfLines);
								long perfStartTime = System.currentTimeMillis();
								response = (DataResponse)client.getResponse(request,null);
								if (response.getData() instanceof DataError){
									DataError error = (DataError)response.getData();
									System.out.println(error.getMessage());
									break;
								}
								ACLineSegmentTest data  = (ACLineSegmentTest)(response).getData();
								//logWriter.write(data.getTime()+";"+String.valueOf(System.currentTimeMillis()-perfStartTime)+"\n");
								//long gossTime = System.currentTimeMillis()-perfStartTime-data.getTime();
								long endTime = System.currentTimeMillis();
								long total = endTime-perfStartTime;
								long startTimeDB = data.getBeforeTime();
								long endTimeDB = data.getTime();
								long totalTimeDB = endTimeDB-startTimeDB;
								long timeTakneByGoss = total-totalTimeDB;
								System.out.println("Client_"+clientNum+",Request_"+i+","+perfStartTime+","+endTime+","+total+","+startTimeDB+","+endTimeDB+","+totalTimeDB+","+timeTakneByGoss);
								//System.out.println(data.getKvlevel());
								//System.out.println(response.sizeof());
						}
						//logWriter.close();
						client.close();
						//System.out.println("Client"+clientNum+","+startTime+","+System.currentTimeMillis());
					}
					catch(ClassCastException cce){
						cce.printStackTrace();
						if (response.getData() instanceof DataError){
							DataError error = (DataError)response.getData();
							System.out.println(error.getMessage());
						}
						else
							throw cce;
					}
					catch(Exception e){
						e.printStackTrace();
					}
					catch(Throwable e){
						e.printStackTrace();
					}
				}
			});
			thread.start();
		}	
	}
	
	static void asynchronousTest(int noOfClients, int noOfChannels, String startTime, String endTime, int segment) throws ParseException {
		for(int clientNo=1;clientNo<=noOfClients;clientNo++){
			final int clientNum = clientNo;
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			final Date startDate = formatter.parse(startTime);
			final Date endDate = formatter.parse(endTime);
			final int segment_ = segment;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try{
						final GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
						final FileWriter logWriter = new FileWriter("mysql_asynchronous_client"+clientNum+".log",true);
						RequestLineLoadAsyncTest request = new RequestLineLoadAsyncTest("Greek-118-South",38, startDate, endDate, segment_);
						GossResponseEvent event = new GossResponseEvent() {
							public void onMessage(Response response) {
								try{
									long time = System.currentTimeMillis();
									DataResponse dataResponse = (DataResponse)response;
									if(dataResponse.getData() instanceof ACLineSegmentTest){
										ACLineSegmentTest data  = (ACLineSegmentTest)(dataResponse).getData();
										//logWriter.write(data.getBeforetime()+";"+data.getTime()+";"+time+"\n");
										//System.out.println(response.sizeof());
										if(dataResponse.isResponseComplete()==true){
											logWriter.close();
											client.close();
										}
									}
									else if(dataResponse.getData() instanceof DataError){
											DataError error = (DataError)dataResponse.getData();
											System.out.println(error.getMessage());
									}
								}
								catch(Exception e){
									e.printStackTrace();
								}
							}
							
						};
						client.sendRequest(request, event, null);
						logWriter.write("Req; "+System.currentTimeMillis()+"\n");
						logWriter.write("DS1;DS2;Res;\n");
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			});
			thread.start();
		}	
	}

	
	private static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	}