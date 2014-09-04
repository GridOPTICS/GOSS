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
package pnnl.goss.fusiondb.handlers;

import static pnnl.goss.fusiondb.FusionDBServerActivator.PROP_FUSIONDB_DATASERVICE;

import java.sql.Connection;
import java.sql.Statement;

import pnnl.goss.core.Request;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.fusiondb.datamodel.CapacityRequirement;
import pnnl.goss.fusiondb.datamodel.GeneratorData;
import pnnl.goss.fusiondb.datamodel.InterfacesViolation;
import pnnl.goss.fusiondb.datamodel.VoltageStabilityViolation;
import pnnl.goss.server.core.GossRequestHandler;


public class FusionUploadHandler extends GossRequestHandler {
	
	Connection connection;
	Statement statement;
		
	public UploadResponse handle(Request request) {
		
		UploadResponse response = null;
		
		try{
			UploadRequest uploadrequest = (UploadRequest)request;
			
			connection = this.dataservices.getPooledConnection(PROP_FUSIONDB_DATASERVICE);
			System.out.println(connection);
			statement = connection.createStatement();
			
			if(uploadrequest.getData() instanceof CapacityRequirement)
				uploadCapacityRequirement((CapacityRequirement)uploadrequest.getData());
			else if(uploadrequest.getData() instanceof GeneratorData)
				uploadGeneratorData((GeneratorData)uploadrequest.getData());
			else if(uploadrequest.getData() instanceof InterfacesViolation)
				uploadInterfacesViolation((InterfacesViolation)uploadrequest.getData());
			else if(uploadrequest.getData() instanceof VoltageStabilityViolation)
				uploadVoltageStabilityViolation((VoltageStabilityViolation)uploadrequest.getData());
			
				
		}
		catch(Exception e){
			response = new UploadResponse(false);
			response.setMessage(e.getMessage());
			e.printStackTrace();
			return response;
		}
		response = new UploadResponse(true);
		return response;
	}
	
	private void uploadCapacityRequirement(CapacityRequirement data) throws Exception{
		
		String queryString = "replace into capacity_requirements(`timestamp`,confidence,interval_id,up,down) values "+
								"('"+data.getTimestamp()+"',"+data.getConfidence()+","+data.getIntervalId()+","+data.getUp()+","+data.getDown()+")";
		System.out.println(queryString);
		int rows =  statement.executeUpdate(queryString);
		System.out.println(rows);
		if(connection.getAutoCommit()==false)
			connection.commit();
		connection.close();
		
	}
	
	private void uploadGeneratorData(GeneratorData data) throws Exception{
		
		String queryString = "replace into generator("
							+ "busnum,"
							+ "genmw,"
							+ "gen_mvr,"
							+ "gen_mvr_max,"
							+ "gen_mvr_min,"
							+ "gen_volt_set,"
							+ "gen_id,"
							+ "gen_status,"
							+ "gen_mw_max,"
							+ "gen_mw_min) values ("+
							+data.getBusNum()+","
							+data.getGenMW()+","
							+data.getGenMVR()+","
							+data.getGenMVRMax()+","
							+data.getGenMVRMin()+","
							+data.getGenVoltSet()+","
							+data.getGenId()+",'"
							+data.getGenStatus()+"',"
							+data.getGenMWMax()+","
							+data.getGenMWMin()+")";
		System.out.println(queryString);
		int rows =  statement.executeUpdate(queryString);
		System.out.println(rows);
		if(connection.getAutoCommit()==false)
		connection.commit();
		connection.close();
		
		
	}
	
	private void uploadInterfacesViolation(InterfacesViolation data) throws Exception{
		
		String queryString = "replace into interfaces_violation("
						+ "`timestamp`,"
						+ "interval_id,"
						+ "interface_id,"
						+ "probability) values ('"
						+ data.getTimestamp()+"',"
						+ data.getIntervalId()+","
						+ data.getInterfaceId()+","
						+ data.getProbability()+")";
		System.out.println(queryString);
		int rows =  statement.executeUpdate(queryString);
		System.out.println(rows);
		if(connection.getAutoCommit()==false)
		connection.commit();
		connection.close();
		
	}
	
	private void uploadVoltageStabilityViolation(VoltageStabilityViolation data) throws Exception{
		String queryString = "replace into voltage_stability_violation("
				+ "`timestamp`,"
				+ "interval_id,"
				+ "bus_id,"
				+ "probability) values ('"
				+ data.getTimestamp()+"',"
				+ data.getIntervalId()+","
				+ data.getBusId()+","
				+ data.getProbability()+")";
		System.out.println(queryString);
		int rows =  statement.executeUpdate(queryString);
		System.out.println(rows);
		if(connection.getAutoCommit()==false)
		connection.commit();
		connection.close();
	}

}
