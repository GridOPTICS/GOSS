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
package pnnl.goss.gridpack.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.gridpack.common.datamodel.GridpackBranch;
import pnnl.goss.gridpack.common.datamodel.GridpackBus;
import pnnl.goss.gridpack.common.datamodel.GridpackPowergrid;
import pnnl.goss.gridpack.service.impl.GridpackUtils;
import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.requests.RequestPowergrid;
import pnnl.goss.powergrid.server.handlers.RequestPowergridHandler;

@Path("/")
public class GridpackServiceImpl {
	
	@GET
	@Path("/{powergridName}/buses/{numberOfBuses}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Collection<GridpackBus> getBuses0ToN(
			@PathParam(value = "powergridName") String powergridName, 
			@PathParam(value = "numberOfBuses") int numberOfBuses){
		
		return getBusesNToM(powergridName, 0, numberOfBuses);
	}
	
	@GET
	@Path("/{powergridName}/buses/{startAtIndex}/{numberOfBuses}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Collection<GridpackBus> getBusesNToM(
			@PathParam(value = "powergridName") String powergridName, 
			@PathParam(value = "startAtIndex") int startAtIndex,
			@PathParam(value = "numberOfBuses") int numberOfBuses){
		
		GridpackPowergrid grid = getGridpackGrid(powergridName);
		List<GridpackBus> buses = new ArrayList<GridpackBus>(grid.getBuses());
		
		if (buses.size() > startAtIndex + numberOfBuses){
			return buses.subList(startAtIndex, startAtIndex+numberOfBuses);
		}
		else if(buses.size() > startAtIndex){
			return buses.subList(startAtIndex, buses.size());
		}
		
		return null;
	}
	
	@GET
    @Path("/{powergridName}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public GridpackPowergrid getGridpackGrid(
			@PathParam(value = "powergridName") String powergridName){
		
		GridpackPowergrid pg = null;
		
		RequestPowergrid request = new RequestPowergrid(powergridName);
		RequestPowergridHandler handler = new RequestPowergridHandler();
		
		DataResponse response = handler.getResponse(request);

		// Make sure the response didn't throw an error.
		GridpackUtils.throwDataError(response);
		
		PowergridModel powergrid = (PowergridModel)response.getData(); //handler.getResponse(request).getData();
					
		pg = new GridpackPowergrid(powergrid);
		
		return pg;
	}
	
	@GET
	@Path("/{powergridName}/bus/count")
	@Produces(MediaType.TEXT_PLAIN)
	public Integer getNumberOfBuses(
			@PathParam(value = "powergridName") String powergridName)
	{
		RequestPowergrid request = new RequestPowergrid(powergridName);
		RequestPowergridHandler handler = new RequestPowergridHandler();
		DataResponse response = handler.getResponse(request);
		
		// Make sure the response didn't throw an error.
		GridpackUtils.throwDataError(response);
		
		PowergridModel model = (PowergridModel)response.getData();
		
		return model.getBuses().size();
	}
	
	@GET
	@Path("/{powergridName}/branch/count")
	@Produces(MediaType.TEXT_PLAIN)
	public Integer getNumberOfBranches(
			@PathParam(value = "powergridName") String powergridName)
	{
		RequestPowergrid request = new RequestPowergrid(powergridName);
		RequestPowergridHandler handler = new RequestPowergridHandler();
		DataResponse response = handler.getResponse(request);
		
		// Make sure the response didn't throw an error.
		GridpackUtils.throwDataError(response);
		
		PowergridModel model = (PowergridModel)response.getData();
		
		return model.getBranches().size();
	}
	
	@GET
	@Path("/{powergridName}/branches/{numberOfBranches}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Collection<GridpackBranch> getBranches0ToN(
			@PathParam(value = "powergridName") String powergridName, 
			@PathParam(value = "numberOfBranches") int numberOfBranches){
		
		Collection<GridpackBranch> branches = getBranchesNToM(powergridName, 0, numberOfBranches);
		return branches;
	}
	
	@GET
	@Path("/{powergridName}/branches/{startAtIndex}/{numberOfBuses}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Collection<GridpackBranch> getBranchesNToM(
			@PathParam(value = "powergridName") String powergridName, 
			@PathParam(value = "startAtIndex") int startAtIndex,
			@PathParam(value = "numberOfBranches") int numberOfBranches){
		
		GridpackPowergrid grid = getGridpackGrid(powergridName);
		List<GridpackBranch> branches = new ArrayList<GridpackBranch>(grid.getBranches());
		
		if (branches.size() > startAtIndex + numberOfBranches){
			return branches.subList(startAtIndex, startAtIndex+numberOfBranches);
		}
		else if(branches.size() > startAtIndex){
			return branches.subList(startAtIndex, branches.size());
		}
		
		return null;
	}
	
	

	public Collection<GridpackBus> getBusesTimesteps(String powergridId,
			String timestep) {
		// TODO Auto-generated method stub
		return null;
	}

}
