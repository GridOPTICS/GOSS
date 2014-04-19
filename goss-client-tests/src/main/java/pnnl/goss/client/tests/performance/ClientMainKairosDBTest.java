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

import pnnl.goss.client.tests.util.ClientAuthHelper;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossResponseEvent;
import pnnl.goss.kairosdb.datamodel.KairosTestData;
import pnnl.goss.kairosdb.requests.RequestKairosAsyncTest;
import pnnl.goss.kairosdb.requests.RequestKairosTest;

public class ClientMainKairosDBTest {

	public static void main(String args[]){

		String typeOfCommunication = "s";
		int noOfClients = 1;
		int noOfChannels = 1; //max = 555
		int dataPerResponse = 1;

		if(typeOfCommunication.equals("s"))
			synchronousTest(noOfClients, noOfChannels);
		if(typeOfCommunication.equals("a"))
			asynchronousTest(noOfClients, noOfChannels, dataPerResponse);
	}

	static void synchronousTest(int noOfClients, int noOfChannels){
		for(int clientNo=1;clientNo<=noOfClients;clientNo++){
			final int clientNum = clientNo;
			final int numOfChannels = noOfChannels;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					DataResponse response=null;		
					try{
						GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
						RequestKairosTest request = null;
						FileWriter logWriter = new FileWriter("kairos_synchronous_client"+clientNum+".log",true);
						logWriter.write("KairosDB;KairosDB+GOSS\n");
						for(int channel=1;channel<=numOfChannels;channel++){
							for(long time = 1270105200; time<1270105300; time++){
								request = new RequestKairosTest("test","flag", time, time+5);
								long perfStartTime = System.nanoTime();
								response = (DataResponse)client.getResponse(request,null);
								long res = System.nanoTime();
								KairosTestData data  = (KairosTestData)(response).getData();
								long dbTime = data.getDS2()-data.getDS1();
								long total = res-perfStartTime;
								logWriter.write(dbTime+";"+total+"\n");
								//System.out.println(data.getValues().length);
								//System.out.println(response.sizeof());
							}
						}
						logWriter.close();
						client.close();
					}
					catch(ClassCastException cce){
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
				}
			});
			thread.start();
		}	
	}

static void asynchronousTest(int noOfClients, int noOfChannels, int dataPerResponse){
	for(int clientNo=1;clientNo<=noOfClients;clientNo++){
		final int clientNum = clientNo;
		final int numOfChannels = noOfChannels;
		final int dataPerResp = dataPerResponse;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try{
					final FileWriter logWriter = new FileWriter("kairos_asynchronous_client"+clientNum+".log",true);
					final GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
					RequestKairosAsyncTest request = new RequestKairosAsyncTest("test", "flag", 1270105200, 1270105300,dataPerResp);
					
					GossResponseEvent event = new GossResponseEvent() {
						public void onMessage(Response response) {
							long res = System.nanoTime();
							DataResponse dataresponse=null;
							try{
								dataresponse = (DataResponse)response;
								KairosTestData data  = (KairosTestData)(dataresponse).getData();
								
								if(dataresponse.getBeforeSecurity()!=0)
									logWriter.write(+data.getDS1()+";"+data.getDS2()+";"+dataresponse.getBeforeSecurity()+";"+dataresponse.getAfterSecurity()+";"+res+"\n");
								else
									logWriter.write(+data.getDS1()+";"+data.getDS2()+";;;"+res+"\n");
								//System.out.println(data.getValues().length);
								//System.out.println(response.sizeof());
								if(dataresponse.isResponseComplete()){
									logWriter.close();
									client.close();
								}
							}
							catch(ClassCastException cce){
								if(dataresponse!=null){
									DataError error = (DataError)dataresponse.getData();
									System.out.println(error.getMessage());
								}
								else 
									throw cce;
							}
							catch(Exception e){
								e.printStackTrace();
							}
						}
					};

					for(int channel=1;channel<=numOfChannels;channel++){
						client.sendRequest(request, event, null);
						logWriter.write("Req;"+System.currentTimeMillis()+"\n");
						logWriter.write("DS1;DS2;Sec1;Sec2;Res\n");
					}
					
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}	
}
	
}