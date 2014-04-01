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

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.server.core.GossRequestHandler;
import pnnl.goss.sharedperspective.common.requests.RequestContingencyResult;
import pnnl.goss.sharedperspective.dao.PowergridSharedPerspectiveDaoMySql;

public class RequestContingencyResultHandler extends GossRequestHandler {

	public static final String DATETIME_FORMAT = "y-M-d H:m:s";
	
	public DataResponse handle(Request request){
		
		DataResponse dataResponse = new DataResponse();
		
		try{
			
			RequestContingencyResult requestContext = (RequestContingencyResult) request;
			String dsName = PowergridDataSources.instance().getDatasourceKeyWherePowergridName(new PowergridDaoMySql(), requestContext.getPowergridName());
			PowergridSharedPerspectiveDaoMySql dao = new PowergridSharedPerspectiveDaoMySql(PowergridDataSources.instance().getConnectionPool(dsName));
			
			/*
			 * DatabaseName is a required parameter if it's not valid send back DataError
			 */		
			String dbName = requestContext.getPowergridName();
			if (dbName == null || dbName.isEmpty()){
				dataResponse.setData(new DataError("Invalid PowerGridName"));
				return dataResponse;
			}
			
			//Timestamp is not a required field, however it must be formatted properly if it is specified.  
			//After this call the timestamp will be set for any properly formatted timestamp string.
			Timestamp timestamp = getTimestamp(requestContext.getTimestamp());
			
			//System.out.println(timestamp);
			
			// Use the calendar to modify the timestamp accordingly.
			Calendar cal = Calendar.getInstance();
			
			// Now we handle the current timestep and cast it back to our epoch date 08/01/2013
			if (timestamp == null)
			{				
				cal.setTime(new java.util.Date());
			}
			else{
				cal.setTime(new Date(timestamp.getTime()));
			}
			
			// Change the year month and day to be our epoch eventually this will
			// have to change.  Month is 0-based
			cal.set(2013, 7, 1);
			
			// Modulo 5 because we have 5 minute intervals min - min % 5 is the last minute to be passed divisible by 5.
			cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - cal.get(Calendar.MINUTE) % 5);
			// Ignore seconds altogether.
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			// Grab the timestamp out of the calendar object.
			timestamp = new Timestamp(cal.getTime().getTime());
			
			//System.out.println("timestamp = "+timestamp);
			
			// Set our result data here.
			dataResponse.setData(dao.getContingencyResults(timestamp));
		
		} catch(Exception e){
			dataResponse.setData(new DataError(e.getCause().toString()));
			e.printStackTrace();
		}
		
		return dataResponse;
	}
		
	private Timestamp getTimestamp(String timestamp) throws Exception{
		// Null is a valid timestamp because we haven't built
		// our "current" date yet.
		if (timestamp == null) 
			return null;
		
		// The return value
		Timestamp retValue = null;
		
		// This is the format that we are accepting as a date time string.
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
		try {
			java.util.Date parsedDate = sdf.parse(timestamp);
			retValue = new Timestamp(parsedDate.getTime());
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		if (retValue == null)
			throw new Exception("Invalid timestamp " + timestamp + " format should be ("+DATETIME_FORMAT+") see http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html");
		
		return retValue;
		
	}


}