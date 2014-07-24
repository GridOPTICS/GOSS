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

import goss.pnnl.fusiondb.datasources.FusionDataSource;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.fusiondb.datamodel.ForecastTotal;
import pnnl.goss.fusiondb.datamodel.ForecastTotalData;
import pnnl.goss.fusiondb.requests.RequestForecastTotal;
import pnnl.goss.server.core.GossRequestHandler;

public class RequestForecastTotalHandler extends GossRequestHandler{

	public boolean viz= false;
	
	public DataResponse handle(Request request) {

		Serializable data = null;

		try {
			String dbQuery = "";
			Connection connection = FusionDataSource.getInstance().getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = null;
			
			RequestForecastTotal request1 = (RequestForecastTotal) request;
			
			String tableName = "rte_"+request1.getType().toString().toLowerCase()+"_forecast";

			if (request1.getEndTimeStamp() == null) {
				dbQuery = "select * from fusion."+tableName+" where `TimeStamp` = '"+request1.getStartTimestamp()+"' AND IntervalID <= "+request1.getInterval()+" order by IntervalID";
			} else {

				dbQuery = "select * from fusion."+tableName+" where `TimeStamp` >= '"+request1.getStartTimestamp()+"' and `TimeStamp`<'"+request1.getEndTimeStamp()+"' and "+
						"IntervalID <="+request1.getInterval()+"  order by `TimeStamp`";
			}

			System.out.println(dbQuery);
			rs = stmt.executeQuery(dbQuery);
			
			if(viz==true){
				List<Double> valuesList = new ArrayList<Double>();
				List<String> timestampsList = new ArrayList<String>();
				List<Integer> intervalsList = new ArrayList<Integer>();
				
				while (rs.next()) {
					timestampsList.add(rs.getString(1));
					intervalsList.add(rs.getInt(2));
					valuesList.add(rs.getDouble(3));
					
				}
	
				ForecastTotal forecastTotal = new ForecastTotal();
				forecastTotal.setType(request1.getType().toString());
				forecastTotal.setTimestamps(timestampsList.toArray(new String[timestampsList.size()]));
				forecastTotal.setValues(valuesList.toArray(new Double[valuesList.size()]));
				forecastTotal.setIntervals(intervalsList.toArray(new Integer[intervalsList.size()]));
				
				data = forecastTotal;
			}
			else{
				ArrayList<ForecastTotalData> list = new ArrayList<ForecastTotalData>();
				ForecastTotalData forecastTotal=null;
				while (rs.next()) {
					forecastTotal = new ForecastTotalData();
					forecastTotal.setTimestamp(rs.getString(1));
					forecastTotal.setType(request1.getType().toString());
					forecastTotal.setValue(rs.getDouble(3));
					forecastTotal.setInterval(rs.getInt(2));
					list.add(forecastTotal);
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
