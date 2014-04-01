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
/**

 * Data store for PMU data.
 * Original data available from 01-04-2010 07:00:01 (1270105200) upto 01-04-2010 07:59:59 (1270108799). 
 * TimeSeriesId range from  0-37
 */

package pnnl.goss.gridmw.datasources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class GridMW {

	//GridMW Server 1
	private static String ip = "130.20.104.168";
	private static Integer port = 6007;

	//GridMW Server 2
	//private static String ip = "gridmw";
	//private static Integer port = 6001;

	public static Socket cliSock = null;
	private static GridMW instance;

	private GridMW(){
		try {
			cliSock = new Socket(ip, port);
			System.out.println(cliSock.toString());
		} catch (UnknownHostException e){
			System.err.println("Unknown host "+ip+". Cannot recognize data store server.");
			e.printStackTrace();
		}
		catch(ConnectException e){
			System.err.println("Can not connect to data store server.Make sure it is started.");
			e.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	public static GridMW getInstance() {
		if(instance == null){
			instance  = new GridMW();
		}
		return instance;
	}

	public float[] get(Integer timeSeriesId, Long startTime, Long endTime, int count) {
		float data[] = new float[count];
			try {
				DataOutputStream requestStream = new DataOutputStream(new BufferedOutputStream(cliSock.getOutputStream()));
				requestStream.writeInt(1);	
				requestStream.writeInt(timeSeriesId);
				requestStream.writeLong(startTime);
				requestStream.writeLong(endTime);
				requestStream.flush();
				DataInputStream input = new DataInputStream(new BufferedInputStream(cliSock.getInputStream()));
				for(int i=0;i<count;i++){
					data[i] = input.readFloat();
				}
			}  
			catch (Exception e) {
				e.printStackTrace();
			}
		return data;
	}

	public boolean upload(Integer timeSeriesId, Long startTime, Long endTime, Double[] values) {
		try{
			DataOutputStream requestStream = new DataOutputStream( new BufferedOutputStream(cliSock.getOutputStream()));
			requestStream.writeInt(2);
			requestStream.writeInt(timeSeriesId);
			requestStream.writeLong(startTime);
			requestStream.writeLong(endTime);
			for (int i=0; i<(endTime-startTime)*30; i++){
				requestStream.writeFloat(values[i].floatValue());
			}
			requestStream.flush();
			//To test writing un-comment this code.
			/*DataInputStream input = new DataInputStream(new BufferedInputStream(cliSock.getInputStream()));
			requestStream.writeInt(1);
			requestStream.writeInt(timeSeriesId);
			requestStream.writeLong(startTime);
			requestStream.writeLong(endTime);
			requestStream.flush();
			for (int i=0; i<(endTime-startTime)*30; i++){
				float number = input.readFloat();
				System.out.println(number);
			}*/
			return true;
		}catch(Exception e ){
			e.printStackTrace();
			return false;
		}

	}

	private static void test(){
		try{
			
			int timeSeriesId = 5;
			long startTime = 1270106100;
			long endTime = 1270106101;
			int count = (int) (endTime - startTime ) * 30;
			
			GridMW gridMW = GridMW.getInstance();
			float[] data = gridMW.get(timeSeriesId, startTime, endTime, count);
			for(int j=0;j<count;j++){
				System.out.println(String.valueOf(data[j]));
			}
		//	gridMW.cliSock.close();
		
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	public static void main(String[] args){
		
		for(int i=0;i<5;i++){
			GridMW.test();
		}
	}

}
