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
package pnnl.goss.powergrid.handlers;

import java.sql.Connection;
import java.sql.Timestamp;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;

import org.junit.Test;

//Let's import Mockito statically so that the code looks clearer
import static org.mockito.Mockito.*;
import pnnl.goss.core.DataResponse;
import pnnl.goss.powergrid.ContingencyModel;
import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.requests.RequestContingencyModel;
import pnnl.goss.powergrid.requests.RequestPowergrid;
import pnnl.goss.powergrid.requests.RequestPowergridTimeStep;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.powergrid.server.handlers.RequestContingencyModelHandler;
import pnnl.goss.powergrid.server.handlers.RequestPowergridHandler;
import pnnl.goss.server.core.GossDataServices;

public class PowergridHandlerTest {

	
	static final String dsKey = "northandsouth";
	static final String databaseUri = "jdbc:mysql://localhost:3306/northandsouth";
	static final String databaseUser = "root";
	static final String databasePassword = "Luckydog2004";
	
	private GossDataServices _dataservice;
	private Connection _connection;
	
	@Before
	public void setup(){
		_connection = mock(Connection.class);
		_dataservice = mock(GossDataServices.class);
	}
	
	@Test
	public void canGetPowergridByName(){
		
	}
	
	public static void setupDatasource(){
		try {
			PowergridDataSources.instance().addConnection(dsKey,databaseUri, databaseUser, databasePassword, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testRequestPowergridHandler(){
		RequestPowergrid request = new RequestPowergrid("Greek-118");
		RequestPowergridHandler handler = new RequestPowergridHandler();
		PowergridModel model = (PowergridModel)((DataResponse)handler.handle(request)).getData();
		
		System.out.println("Powergrid: "+model.getPowergrid().getName()+" Number: "+model.getPowergrid().getPowergridId());
		System.out.println("# Buses: "+ model.getBuses().size());
		System.out.println("# Branches: "+ model.getBranches().size());
		System.out.println("# Transformers: "+ model.getTransformers().size());
		System.out.println("# Lines: "+ model.getLines().size());
		System.out.println("# Substations: "+ model.getSubstations().size());
		System.out.println("# Machines: "+ model.getMachines().size());
		System.out.println("# Loads: "+ model.getLoads().size());
		System.out.println("# SwitchedShunts: "+ model.getSwitchedShunts().size());
	}
	
	public static void testRequestPowergridTimestepsHandler(){
		RequestPowergridTimeStep request = new RequestPowergridTimeStep("Greek-118", Timestamp.valueOf("2013-08-01 00:00:00"));
		RequestPowergridHandler handler = new RequestPowergridHandler();
		
		PowergridModel model = (PowergridModel)((DataResponse)handler.handle(request)).getData();
		
		//System.out.println("# Available Timesteps: "+model.getTimesteps().size());
		//System.out.println("Current Timestamp: "+model.getCurrentTimestamp());
	}
	
	public static void testRequestContingencyModelHandler(){
		RequestContingencyModel request = new RequestContingencyModel("Greek-118");
		RequestContingencyModelHandler handler = new RequestContingencyModelHandler();
		ContingencyModel model = (ContingencyModel)((DataResponse)handler.handle(request)).getData();
		
		System.out.println("# Contingencies: "+ model.getContingencies().size());
		/*
		for (Contingency c: model.getContingencies()){
			System.out.println("branches out: "+model.getBranchesOut(c).size());
		}*/
		
	}
	
	public static void testRequestContingenciesHandler(){
		
	}
	
	public static void main(String[] args) {
		setupDatasource();
		
		testRequestPowergridHandler();
		System.out.println("\n\n");
		testRequestPowergridTimestepsHandler();
		System.out.println("\n\n");
		testRequestContingencyModelHandler();
		System.out.println("\n\n");
	}

}
