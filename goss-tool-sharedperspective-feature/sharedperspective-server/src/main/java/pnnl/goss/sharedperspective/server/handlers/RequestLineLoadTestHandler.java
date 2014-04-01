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
package pnnl.goss.sharedperspective.server.handlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import pnnl.goss.core.Data;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.server.core.GossRequestHandler;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegmentTest;
import pnnl.goss.sharedperspective.common.datamodel.Topology;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoad;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoadTest;
import pnnl.goss.sharedperspective.dao.PowergridSharedPerspectiveDaoMySql;

public class RequestLineLoadTestHandler extends GossRequestHandler {

	Data data =null;
	Connection connection=null;
	RequestLineLoad request1=null;

	public Response handle(Request request){

		DataResponse dataResponse = new DataResponse();
		
		
		return dataResponse;
		
		/*ACLineSegmentTest data =null;
		RequestLineLoadTest requestLineLoad = (RequestLineLoadTest) request;
		long time = System.currentTimeMillis();
		Connection connection = null;
		ACLineSegmentTest acLineSegment = null;
		ResultSet rs =null;

		try{
			//PowergridSharedPerspectiveDaoMySql dao = new PowergridSharedPerspectiveDaoMySql(PowergridDataSources.instance().getConnectionPool(requestLineLoad.getPowergridName().toLowerCase()));
			//int powergridId = dao.getPowergridId(requestLineLoad.getPowergridName());
			//data = dao.getLineLoadTest(powergridId, requestLineLoad.getStartTime(),requestLineLoad.getLineId());




			connection = PowergridDataSourceBoneCP.getInstance().getConnection();
			Statement stmt = connection.createStatement();

			String dbQuery="";

			Timestamp timestamp_=null;
			if(timestamp_==null){
				//Get current time -> set date to 2013-08-01 -> make sure that second value is multiple of 3
				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date());
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				timestamp_ = new Timestamp(cal.getTime().getTime());
			}
			else{
				Calendar cal = Calendar.getInstance();
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
				java.util.Date parsedDate = sdf.parse(cal.getTime().toString());
				timestamp_ = new Timestamp(parsedDate.getTime());
			}

			dbQuery = "select mbr.mrid, lt.p, lt.q,  bu.BaseKV, br.Rating, br.Status "+
					"from mridbranches mbr, branches br, buses bu, linetimesteps lt, lines_ l "+
					"where br.branchid = mbr.branchid "+
					"and br.frombusnumber = bu.busnumber "+
					"and l.lineid = "+requestLineLoad.getLineId() + " "+
					"and l.branchid = br.branchid "+
					"and mbr.powergridid = br.powergridid "+
					"and bu.powergridid = br.powergridid "+
					"and lt.powergridid = br.powergridid "+
					"and l.powergridid = br.powergridid "+
					"and br.powergridid = 1 "+
					"and lt.timestep ='"+ timestamp_+"'";

			System.out.println(dbQuery);

			rs=stmt.executeQuery(dbQuery);

			if(rs.next()){
				acLineSegment = new ACLineSegmentTest();
				//acLineSegment.setMrid(rs.getString("mrid"));					//Branch's Mrid
				acLineSegment.setKvlevel(rs.getDouble("BaseKV")); 			//Base KV from buses 
				acLineSegment.setRating(rs.getDouble("Rating")); 				//branch
				acLineSegment.setStatus(rs.getInt("Status"));				//line timestep
				double mvaFlow  = Math.sqrt((rs.getDouble("p")*rs.getDouble("p"))+ (rs.getDouble("q")*rs.getDouble("q")));
				if(rs.getDouble("p")<0)
					mvaFlow = -mvaFlow;
				acLineSegment.setMvaFlow(mvaFlow); 			
			}

			data =  acLineSegment;
			data.setTime(System.currentTimeMillis()-time);
			dataResponse.setData(data);
		}
		catch(Exception e){
			e.printStackTrace();
			dataResponse.setData(new DataError(e.getMessage()));
			return dataResponse;
		}
		finally{
			if(rs!=null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(connection!=null)
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}


		return dataResponse;*/
	}

}