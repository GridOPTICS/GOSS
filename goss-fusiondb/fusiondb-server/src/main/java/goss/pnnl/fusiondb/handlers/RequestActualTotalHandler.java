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
package goss.pnnl.fusiondb.handlers;

import goss.pnnl.fusiondb.impl.FusionDataSourceMysql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.fusiondb.datamodel.ActualTotal;
import pnnl.goss.fusiondb.datamodel.ActualTotalData;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.server.core.GossRequestHandler;

public class RequestActualTotalHandler extends GossRequestHandler {

	public boolean viz = false;
	
	public DataResponse handle(Request request) {

		Serializable data = null;
		
		try {
			String dbQuery = "";
			Connection connection = FusionDataSourceMysql.getInstance().getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = null;
			
			RequestActualTotal request1 = (RequestActualTotal) request;
			
			String tableName = "actual_"+request1.getType().toString().toLowerCase()+"_total";
			if(request1.getType().toString().toLowerCase().equals("interhchange"))
				tableName = "actual_interchange_total";
			
			if (request1.getEndTimeStamp() != null) {
				dbQuery = "select * from fusion."+tableName+" where `TimeStamp` between '"+request1.getStartTimestamp()+"'"+
						" and  '"+request1.getEndTimeStamp()+"' order by `TimeStamp`";
			} else {

				dbQuery = "select * from fusion."+tableName+" where `TimeStamp` ='"+request1.getStartTimestamp()+"' order by `TimeStamp`";
			}

			System.out.println(dbQuery);
			rs = stmt.executeQuery(dbQuery);
			
			if(viz==false){
				List<Double> valuesList = new ArrayList<Double>();
				List<String> timestampsList = new ArrayList<String>();
				
				while (rs.next()) {
					timestampsList.add(rs.getString(1));
					valuesList.add(rs.getDouble(2));
				}
	
				ActualTotal actualTotal = new ActualTotal();
				actualTotal.setType(request1.getType().toString());
				actualTotal.setTimestamps(timestampsList.toArray(new String[timestampsList.size()]));
				actualTotal.setValues(valuesList.toArray(new Double[valuesList.size()]));
				
				data = actualTotal;
			}
			else{
				ArrayList<ActualTotalData> list = new ArrayList<ActualTotalData>();
				ActualTotalData actualTotal=null;
				while (rs.next()) {
					actualTotal = new ActualTotalData();
					actualTotal.setTimestamps(rs.getString(1));
					actualTotal.setType(request1.getType().toString());
					actualTotal.setValue(rs.getDouble(2));
					list.add(actualTotal);
				}
				data = list;
			}
			
			stmt.close();
			connection.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		DataResponse dataResponse = new DataResponse();
		dataResponse.setData(data);
		return dataResponse;

	}

}
