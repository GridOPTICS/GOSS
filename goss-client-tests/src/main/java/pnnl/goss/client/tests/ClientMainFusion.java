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

import java.util.ArrayList;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.fusiondb.datamodel.ActualTotal;
import pnnl.goss.fusiondb.datamodel.CapacityRequirement;
import pnnl.goss.fusiondb.datamodel.CapacityRequirementValues;
import pnnl.goss.fusiondb.datamodel.ForecastTotal;
import pnnl.goss.fusiondb.datamodel.GeneratorData;
import pnnl.goss.fusiondb.datamodel.HAInterchangeSchedule;
import pnnl.goss.fusiondb.datamodel.InterfacesViolation;
import pnnl.goss.fusiondb.datamodel.RTEDSchedule;
import pnnl.goss.fusiondb.datamodel.VoltageStabilityViolation;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement.Parameter;
import pnnl.goss.fusiondb.requests.RequestForecastTotal;
import pnnl.goss.fusiondb.requests.RequestGeneratorData;
import pnnl.goss.fusiondb.requests.RequestHAInterchangeSchedule;
import pnnl.goss.fusiondb.requests.RequestInterfacesViolation;
import pnnl.goss.fusiondb.requests.RequestRTEDSchedule;
import pnnl.goss.fusiondb.requests.RequestVoltageStabilityViolation;

public class ClientMainFusion {
	
	static String startTimestamp = "2013-1-21 00:00:00";
	static String endTimestamp = "2013-1-23 00:00:00";
	static int interval = 12;
	static GossClient client = new GossClient(new UsernamePasswordCredentials("pmu_user","password"));
	

	public static void main(String[] args) {
		try{
			
			/*getActualTotal();
			getForecastTotal();
			getHAInterchageSchedule();
			getRTEDSchedule();
			uploadCapacityRequirements();
			requestCapacityRequirement();
			uploadGeneratorData();
			requestGeneratorData();
			*/
			
			uploadInterfaceViolation();
			uploadVoltageViolation();
			requestInterfaceViolation();
			requestVoltageViolation();
			
			
		/*	GossResponseEvent event = new GossResponseEvent() {
				
				@Override
				public void onMessage(Response response) {
					System.out.println(response);
					
				}
			};
			
			client.subscribeTo("FUSION/RESULTS", event);
			
			client.publish("FUSION/RESULTS", "This is fusion test result");
			*/
			
			
			
			client.close();
		
		}
		catch(JMSException e){
			e.printStackTrace();
		}

	}
	
	static void getActualTotal() throws JMSException{
		
		for(int i=0; i<10;i++){
		Request request = new RequestActualTotal(RequestActualTotal.Type.SOLAR, startTimestamp, endTimestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		
		ActualTotal data = (ActualTotal)response.getData();
		System.out.println("Solar = "+ data.getValues()[0]);
		
		request = new RequestActualTotal(RequestActualTotal.Type.WIND, startTimestamp, endTimestamp);
		response = (DataResponse)client.getResponse(request);
		data = (ActualTotal)response.getData();
		
		if(data!=null){
			System.out.println("Wind = "+ data.getValues()[0]);
			data=null;
		}
		else
			System.out.println("it's null"+i);
		}
		}
		
	}
	
	
	static void getForecastTotal() throws JMSException{
		Request request = new RequestForecastTotal(RequestForecastTotal.Type.LOAD, startTimestamp, interval, endTimestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		ForecastTotal data = (ForecastTotal)response.getData();
		System.out.println(data.getTimestamps().length);
		System.out.println(data.getValues().length);
		System.out.println(data.getIntervals().length);
		}
	}
	
	static void getHAInterchageSchedule() throws JMSException{
		Request request = new RequestHAInterchangeSchedule(startTimestamp, endTimestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		HAInterchangeSchedule data = (HAInterchangeSchedule)response.getData();
		System.out.println(data.getTimestamps().length);
		System.out.println(data.getValues().length);
		}
	}
	
	static void getRTEDSchedule() throws JMSException{
		Request request = new RequestRTEDSchedule(startTimestamp, interval,endTimestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		RTEDSchedule data = (RTEDSchedule)response.getData();
		System.out.println(data.getTimestamps()[0]);
		System.out.println(data.getIntervals()[0]);
		System.out.println(data.getGenValues()[0]);
		System.out.println(data.getMaxValues()[0]);
		System.out.println(data.getMinValues()[0]);
		}
	}
	
	static void uploadCapacityRequirements() throws JMSException,IllegalStateException{
		
		String timestamp = "2013-1-21 01:01:01";
		int confidence =200;
		int intervalId=1;
		int up=1;
		int down=1;
		CapacityRequirement data = new CapacityRequirement(timestamp,confidence,intervalId,up,down);
		UploadRequest request = new UploadRequest(data, "CapacityRequirement");
		UploadResponse response  = (UploadResponse)client.getResponse(request);
		
		/*if(response.isSuccess())
				client.publish("/topic/goss/fusion/capacity", data,RESPONSE_FORMAT.JSON);*/
		if(response.getMessage()!=null)
			System.out.println(response.getMessage());
		
	}
	
	static void requestCapacityRequirement() throws JMSException{
		String timestamp = "2013-1-21 01:01:01";
		RequestCapacityRequirement request = new RequestCapacityRequirement(timestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		CapacityRequirementValues data =null;
		
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		data  = (CapacityRequirementValues)response.getData();
		if(data.getTimestamp().length>0){
			System.out.println(data.getTimestamp()[0]);
		}
		}
		request = new RequestCapacityRequirement(timestamp,Parameter.CONFIDENCE,95);
		response = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		data  = (CapacityRequirementValues)response.getData();
		if(data.getTimestamp().length>0){
			System.out.println(data.getTimestamp()[0]);
		}
		}
		
		request = new RequestCapacityRequirement(timestamp,Parameter.INTERVAL,1);
		response = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		data  = (CapacityRequirementValues)response.getData();
		if(data.getTimestamp().length>0){
			System.out.println(data.getTimestamp()[0]);
		}
		}
	}
	
	static void uploadGeneratorData() throws JMSException,IllegalStateException{
		
		// GeneratorData(busNum, genMW, genMVR, genMVRMax, genMVRMin, genVoltSet, genId, genStatus, genMWMax, genMWMin)
		GeneratorData data = new GeneratorData(-1, 0.0, 0.0, 0.0, 0.0, 0.0, "-1", "Closed", 0.0, 0.0);
		UploadRequest request = new UploadRequest(data, "fusion_GeneratorData");
		UploadResponse response  = (UploadResponse)client.getResponse(request);
		if(response.getMessage()!=null)
			System.out.println(response.getMessage());
		
	}
	
	static void requestGeneratorData() throws JMSException,IllegalStateException{
		
		//RequestGeneratorData(busNum, genId)
		RequestGeneratorData request = new RequestGeneratorData(-1, -1);
		DataResponse response  = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			System.out.println(((DataError)response.getData()).getMessage());
		}
		else{
		GeneratorData data = (GeneratorData)response.getData();
		System.out.println(data.getBusNum());
		data.getGenId();
		data.getGenMVR();
		data.getGenMVRMax();
		data.getGenMVRMin();
		data.getGenMW();
		data.getGenMWMax();
		data.getGenMWMin();
		data.getGenStatus();
		}
	}
	
	static void uploadInterfaceViolation() throws JMSException,IllegalStateException{
		//InterfacesViolation(timestamp, intervalId, interfaceId, probability)
		InterfacesViolation data = new InterfacesViolation(startTimestamp, -1, -1, -1.1);
		UploadRequest request = new UploadRequest(data, "InterfacesViolation");
		UploadResponse response  = (UploadResponse)client.getResponse(request);
		if(response.getMessage()!=null)
			System.out.println(response.getMessage());
	}
	
	static void uploadVoltageViolation() throws JMSException,IllegalStateException{
		//VoltageStabilityViolation(timestamp, intervalId, budId, probability)
		VoltageStabilityViolation data = new VoltageStabilityViolation(startTimestamp, -1, -1, -1.1);
		UploadRequest request = new UploadRequest(data, "VoltageStabilityViolation");
		UploadResponse response  = (UploadResponse)client.getResponse(request);
		if(response.getMessage()!=null)
			System.out.println(response.getMessage());
	}
	
	static void requestInterfaceViolation() throws JMSException,IllegalStateException{
		
		RequestInterfacesViolation request = new RequestInterfacesViolation(startTimestamp);
		DataResponse response  = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			System.out.println(((DataError)response.getData()).getMessage());
		}
		else{
			ArrayList<InterfacesViolation> list = (ArrayList<InterfacesViolation>)response.getData();
			for(InterfacesViolation data : list){
				System.out.println(data.getTimestamp()+
				data.getIntervalId()+
				data.getInterfaceId()+
				data.getProbability());
			}
		}
	}
	
	static void requestVoltageViolation()throws JMSException,IllegalStateException{
		RequestVoltageStabilityViolation request = new RequestVoltageStabilityViolation(startTimestamp);
		DataResponse response  = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			System.out.println(((DataError)response.getData()).getMessage());
		}
		else{
			ArrayList<VoltageStabilityViolation> list = (ArrayList<VoltageStabilityViolation>)response.getData();
			for(VoltageStabilityViolation data : list){
				System.out.println(data.getTimestamp()+
				data.getIntervalId()+
				data.getBusId()+
				data.getProbability());
			}
		}
	}

}
	

