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
package pnnl.goss.kairosdb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;

import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.DataPoint;
import org.kairosdb.client.builder.LongDataPoint;
import org.kairosdb.client.builder.MetricBuilder;
import org.kairosdb.client.builder.QueryBuilder;
import org.kairosdb.client.response.QueryResponse;
import org.kairosdb.client.response.Response;

import pnnl.goss.gridmw.datasources.GridMW;

public class App 
{
	public static void main(String[] args) throws IOException, URISyntaxException, ParseException{
		//testPush();
		//transfer();
		//query();
		pushSampleData();
	}


	public static void testPush() throws IOException, URISyntaxException{
		MetricBuilder builder = MetricBuilder.getInstance();
		builder.addMetric("test50_metric")
		.addTag("test_tag2", "0");
		builder.getMetrics().get(0).addDataPoint(1270105200001L, 30);
		HttpClient client = new HttpClient("we22743", 8080);
		Response response = client.pushMetrics(builder);
		System.out.println(response.getStatusCode());
		client.shutdown();

	}

	public static void transfer() throws URISyntaxException, IOException
	{
		try{
			
			//frequency -  
			//phase angle
			//magnitude
			
			
			//get gridmw instance
			GridMW gridMW = GridMW.getInstance();
			HttpClient client = new HttpClient("eioc-goss", 8020);

			//get mysql connection
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://we22743:3306/gridopticsdb", "root", "rootpass");
			System.out.println("Successfully connected to mysql database");
			Statement statement =connection.createStatement();	
			String query;
			ResultSet rs;
			String metric_name;
			boolean stop = false;
			for(int timeSeriesId = 510; timeSeriesId<=555; timeSeriesId++ ){
				
				if(stop == true)
					break;
				
				long startTime = 1270105200;
				long endTime = 1270105201;
				long finalEndTime = 1270108799;
				int count = (int) (endTime - startTime ) * 30;

				query = "select TimeSeriesId, concat(PMUName,'.phasor',PhasorNo,'.',Channel) FROM griddb_map where TimeSeriesId = "+timeSeriesId;
				rs = statement.executeQuery(query);
				rs.next();
				metric_name = rs.getString(2);
				
				System.out.println("Transferring data for timeseriesId = "+timeSeriesId+" and metric_name = "+metric_name);

				while(endTime<=finalEndTime){

					float[] data = gridMW.get(timeSeriesId, startTime, endTime, count);
					startTime = startTime*1000;
					endTime = endTime*1000;
 
					MetricBuilder builder = MetricBuilder.getInstance();
					builder.addMetric(metric_name) 
					.addTag("status", "raw");

					//System.out.println("Adding 1 = "+startTime+" : "+data[0]);
					//System.out.print(data[0]);
					builder.getMetrics().get(0).addDataPoint(startTime, data[0]);
					for(int i=1;i<=28;i++){
						startTime = startTime+35;
						int j=i+1;
						//System.out.println("Adding "+j+"= "+startTime+" : "+data[i]);
						//System.out.print(", "+data[i]);
						builder.getMetrics().get(0).addDataPoint(startTime,data[i]);
					}
					//System.out.println("Adding 30 = "+endTime+" : "+data[data.length-1]);
					//System.out.println(", "+data[data.length-1]);
					builder.getMetrics().get(0).addDataPoint(endTime, data[data.length-1]);
					

					Response response = client.pushMetrics(builder);
					if(response.getStatusCode()!=204){
						System.out.println("\n"+response.getStatusCode()+" for "+metric_name+" at startTime  = "+startTime+" endTime = "+endTime+" and id = "+timeSeriesId+" for value = "+ data[0]);
						stop= true;
					}
					else{
						DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				    	Date startDate = new Date((long)startTime);
				    	System.out.println("Done for "+timeSeriesId+" , "+metric_name+" , "+ df.format(startDate));
				    	startTime = endTime/1000;
				    	endTime = startTime+1;
					}

				}
				//System.out.println("\n");
			}
			client.shutdown();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}


	public static void query() throws IOException, URISyntaxException, ParseException{

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		Date startTime = new Date((long)1270105200*1000);
		Date endTime = new Date((long)1270105202*1000);

		QueryBuilder builder = QueryBuilder.getInstance();
		builder.setStart(df.parse(df.format(startTime)))
		.setEnd(df.parse(df.format(endTime)))
		.addMetric("GC50.phasor0.flag");
		HttpClient client1 = new HttpClient("we22743", 8080);
		QueryResponse response = client1.query(builder);
		if(response.getErrors().size()>0)
			System.out.println(response.getErrors().get(0));
		for(DataPoint dataPoint : response.getQueries().get(0).getResults().get(0).getDataPoints()){
			LongDataPoint point = (LongDataPoint)dataPoint;
			System.out.println(point.getTimestamp()+" : "+point.getValue());
		}


		client1.shutdown();

	}
	
	public static void pushSampleData() throws URISyntaxException, IOException
	{
		try{
			
			//frequency -  
			//phase angle
			//magnitude
			
			HttpClient client = new HttpClient("localhost", 8080);
			client.setRetryCount(3);
			String metric_name = "pmu2.phasor1.mod";
			long startTime = 1388570400;
			long endTime = 1388570401;
			long finalEndTime = 1388574000;
			int count = (int) (endTime - startTime ) * 30;

			float[] data  = new float[count];
			Random random = new Random();
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			float min = 0.9f;
			float max = 1.1f;
			//.9-1.1
			for(int i=0; i<count;i++){
				data[i] = Float.valueOf(decimalFormat.format(min + (max - min) * random.nextDouble()));
			}
			
			while(endTime<=finalEndTime){

					startTime = startTime*1000;
					endTime = endTime*1000;
 
					MetricBuilder builder = MetricBuilder.getInstance();
					builder.addMetric(metric_name) 
					.addTag("status", "raw");

					//System.out.println("Adding 1 = "+startTime+" : "+data[0]);
					builder.getMetrics().get(0).addDataPoint(startTime, data[0]);
					for(int i=1;i<=28;i++){
						startTime = startTime+35;
						int j=i+1;
						//System.out.println("Adding "+j+"= "+startTime+" : "+data[i]);
						builder.getMetrics().get(0).addDataPoint(startTime,data[i]);
					}
					//System.out.println("Adding 30 = "+endTime+" : "+data[data.length-1]);
					builder.getMetrics().get(0).addDataPoint(endTime, data[data.length-1]);
					

					Response response = client.pushMetrics(builder);
					if(response.getStatusCode()!=204){
						System.out.println("\n"+response.getStatusCode()+" for "+metric_name+" at startTime  = "+startTime+" endTime = "+endTime+" and metric = "+metric_name+" for value = "+ data[0]);
						break;
					}
					else{
						DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				    	Date startDate = new Date((long)startTime);
				    	System.out.println("Done for "+metric_name+" , "+ df.format(startDate));
				    	startTime = endTime/1000;
				    	endTime = startTime+1;
					}

				}
			
			client.shutdown();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}


}
