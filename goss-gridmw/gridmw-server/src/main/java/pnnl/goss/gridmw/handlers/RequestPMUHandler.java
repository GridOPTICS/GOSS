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
package pnnl.goss.gridmw.handlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.gridmw.datamodel.PMUData;
import pnnl.goss.gridmw.datasources.GridMW;
import pnnl.goss.gridmw.requests.RequestPMU;
import pnnl.goss.server.core.GossRequestHandler;
import static pnnl.goss.gridmw.GridMWServerActivator.PROP_GRIDMW_DATASERVICE;

public class RequestPMUHandler extends GossRequestHandler{
	
	private static final Logger log = LoggerFactory.getLogger(RequestPMUHandler.class);
	
	public DataResponse handle(Request request){
		
		DataResponse response  = new DataResponse();
		PMUData pmuData = new PMUData();
//		Connection connection= GridmwMappingDataSource.getInstance().getConnection();
		Connection connection = this.dataservices.getPooledConnection(PROP_GRIDMW_DATASERVICE);

		GridMW gridmw = GridMW.getInstance();
		
		try{
			RequestPMU requestPMU=null;
			if(request instanceof RequestPMU)
				requestPMU = (RequestPMU)request;
			
			Map<Integer,String> timeSeriesId = getTimeSeriesIds(requestPMU.getResponsefor(),requestPMU.getPmuNo(),requestPMU.getDataType(),connection);
			long startTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(requestPMU.getStartTime()).getTime() / 1000;
			long endTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(requestPMU.getEndTime()).getTime() / 1000;

			
			log.debug("PMU Handler Start time = "+requestPMU.getStartTime()+"	= "+startTime);
			log.debug("PMU Handler End time = "+requestPMU.getEndTime()+"	= "+endTime);
			
			int count = (int) (endTime - startTime  ) * 30;
			if(count==0){
				count=30;
				endTime=endTime+1;
			}

			log.debug("PMU Handler count = "+count);

			float[] colData = new float[count];
			for (int i = 0; i < colData.length; i++) {
				colData[i] = -1;
			}

			String values[][] = new String[count+1][timeSeriesId.keySet().size()+1];
			//Calendar cal = Calendar.getInstance();
			//cal.setTime(new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(request.getStartTime()));

			int col=1;
			values[0][0] = "time";
			for(Integer id : timeSeriesId.keySet()) {
				values[0][col]=timeSeriesId.get(id).split(";")[0];
				pmuData.addCode_Id_Map(values[0][col].split("[.]")[0],Integer.valueOf(timeSeriesId.get(id).split(";")[1]));
				colData = gridmw.get(id, startTime,endTime, count);
				for(int i=1;i<=colData.length;i++){
					String time = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date ((startTime+((i-1)/30))*1000));
					values[i][0] = time;
					values[i][col]=new Double(colData[i-1]).toString();
				}
				col++;
			}

			//return data;
			pmuData.setValues(values);
			
			connection.close();
			response.setData(pmuData);
			response.setResponseComplete(true);
			return response;
		}
		catch(Exception e){
			e.printStackTrace();
			response.setData(new DataError(e.getMessage()));
			return response;
		}
		
	}

	private Map<Integer,String> getTimeSeriesIds(String[] requestFor, int[] pmuNo, String dataType, Connection connection) throws SQLException{
		
		Map<Integer,String> timeSeriesIds = new HashMap<Integer, String>();
		String dbQuery=null;
		
		if(dataType.equals("PMU_RAW"))
			dbQuery="select timeseriesid, concat(pmuname,'.',phasorno,'.',channel,';',pmuid) from gridopticsdb.griddb_map";// where PMUId="+pmuId";
		else if(dataType.equals("PMU_CLEANED"))
			dbQuery="select max(timeseriesid), concat(pmuname,'.',phasorno,'.',channel,';',pmuid) from gridopticsdb.griddb_map";// where PMUId="+pmuId";
		
		if(pmuNo.length>0 || requestFor.length>=1)
			dbQuery += " where ";

		//Add pmu
		if(pmuNo.length>0){
			if(pmuNo.length==1)
				dbQuery +="pmuid = "+pmuNo[0];
			else if(pmuNo.length>1){
				dbQuery += "pmuid in (";
				for(int i=0;i<pmuNo.length;i++){
					dbQuery += pmuNo[i];
					if(i<pmuNo.length-1)
						dbQuery+=",";
				}
				dbQuery += ")";
			}
		}
			
			
		
		//Add condition
		/*
		while(itr.hasNext()){
			int key = pmuNo[j];
			String[] value = conditions.get(key);
			if(value.length==1)
				dbQuery += key+" = "+conditions.get(key)[0];
			else{
				dbQuery += key+" in (";
				for(int i=0;i<value.length;i++){
					dbQuery += value[i];
					if(i<value.length-1)
						dbQuery+=",";
				}
				dbQuery += ")";
			}
		}
		*/
		
		

		if(pmuNo.length>0 && requestFor!=null && requestFor.length>=1){
			dbQuery+=" and ";
		}

		//Add requestFor
		if(requestFor.length==1){
			if(!requestFor[0].equalsIgnoreCase("ALL")){
				dbQuery+="channel = '"+requestFor[0]+"'";
			}
		}
		else if(requestFor.length>1){
			dbQuery+="channel in (";
			for(int i=0;i<requestFor.length;i++){
				dbQuery+="'"+requestFor[i]+"'";
				if(i<requestFor.length-1)
					dbQuery+=",";
			}
			dbQuery+=")";
		}


		if(pmuNo.length>0 || requestFor.length>1){
			dbQuery+=" and ";
		}

		if(dataType.equals("PMU_RAW"))
			dbQuery+=" rawtimeseriesid is null ";
		else if(dataType.equals("PMU_CLEANED"))
			dbQuery+=" rawtimeseriesid is not null ";

		if(dataType.equals("PMU_CLEANED"))
		dbQuery +="group by pmuname";
		
		log.debug("PMU Handler query "+dbQuery);
		
		
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(dbQuery);
		rs.last();
		int numOfRow = rs.getRow();

		rs.beforeFirst();

		for(int i=1;i<=numOfRow;i++){
			rs.next();
			timeSeriesIds.put(rs.getInt(1),rs.getString(2));
		}
		
		
		return timeSeriesIds;

	}

}
