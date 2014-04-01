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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Random;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.client.GossResponseEvent;
import pnnl.goss.gridmw.datamodel.GridMWTestData;
import pnnl.goss.gridmw.requests.RequestGridMWTest;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegmentTest;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoadTest;

public class ClientMainMySqlTest {

	public static void main(String args[]){

		String typeOfCommunication = "s";
		int noOfClients = 1;
		int noOfLines = 1;
		int dataPerResponse = 1;

		if(typeOfCommunication.equals("s"))
			synchronousTest(noOfClients, noOfLines);
		
	}

	static void synchronousTest(int noOfClients, int noOfChannels){
		for(int clientNo=1;clientNo<=noOfClients;clientNo++){
			final int clientNum = clientNo;
			//final int numOfChannels = noOfChannels;
			//final String powergridName = "south";
			//final String startTime = "8/1/2013 12:00:00 AM"; //min = 8/1/2013 12:00:00 AM
			//final String endTime = "8/1/2013 11:59:00 PM"; //max = 8/1/2013 11:59:00 PM
			
			//java.util.Date date= new java.util.Date();
			//Timestamp timestamp  = new Timestamp(date.getTime());
			
			Thread thread = new Thread(new Runnable() {
				public void run() {
					DataResponse response=null;		
					try{
						GossClient client = new GossClient();
						RequestLineLoadTest request = null;
						FileWriter logWriter = new FileWriter("mysql_synchronous_client"+clientNum+".log",true);
						logWriter.write("MySQL,MySQL+GOSS\n");
						for(int i=0;i<3600;i++){
								System.out.println(i);
								request = new RequestLineLoadTest("south",null,38);
								long perfStartTime = System.currentTimeMillis();
								response = (DataResponse)client.getResponse(request,null);
								//ACLineSegmentTest data  = (ACLineSegmentTest)(response).getData();
								//logWriter.write(data.getTime()+";"+String.valueOf(System.currentTimeMillis()-perfStartTime)+"\n");
								//System.out.println(data.getKvlevel());
						}
						logWriter.close();
						client.close();
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
	

	
	private static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	}