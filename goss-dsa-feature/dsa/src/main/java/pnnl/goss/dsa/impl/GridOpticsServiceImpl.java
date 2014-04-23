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
package pnnl.goss.dsa.impl;

import javax.jms.JMSException;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;


import pnnl.goss.core.client.GossClient;
import pnnl.goss.core.Data;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.dsa.GridOpticsService;


import pnnl.goss.sharedperspective.common.datamodel.ContingencyResultList;
import pnnl.goss.sharedperspective.common.datamodel.Topology;
import pnnl.goss.sharedperspective.common.requests.RequestContingencyResult;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoad;
import pnnl.goss.sharedperspective.common.requests.RequestTopology;

public class GridOpticsServiceImpl extends GossServiceHelper implements GridOpticsService  {

//	private GossClient gridOptics;

	//@Value("${gridoptics.powergridname}")
	private String powerGridName;
	
	
	public GridOpticsServiceImpl() {
//		this.gridOptics= new GossClient(new UsernamePasswordCredentials("goss", "manager"));
		//this.gridOptics = new GossClient();
	}
	
	public String getPowerGridName() {
		return powerGridName;
	}
	
	public void setPowerGridName(String powerGridName) {
		this.powerGridName = powerGridName;
	}
		
	public Topology getLineLoad(String timestamp) {
		return getLineLoad(this.powerGridName, timestamp);
	}
	
	public Topology getLineLoad(String powerGridName, String timestamp) {
		RequestLineLoad request = new RequestLineLoad(powerGridName, timestamp);
		return (Topology) sendGridOpticsRequest(request);
	}

	public Topology getCurrentTopology() {

		return getCurrentTopology(this.powerGridName);
	}
	
	
	public Topology getCurrentTopology(String powerGridName) {
		return getTopology(powerGridName, null);
	}
	
	public Topology getTopologyChanges(String timestamp) {
		return getTopologyChanges(this.powerGridName, timestamp);
	}

	public Topology getTopologyChanges(String powerGridName, String timestamp) {
		return getTopology(powerGridName, timestamp, true);
	}

	public Topology getTopology(String timestamp) {
		return getTopology(this.powerGridName, timestamp);
	}
	
	public Topology getTopology(String powerGridName, String timestamp) {
		return getTopology(powerGridName, timestamp, false);
	}

	private Topology getTopology(String powerGridName, String timestamp,
			boolean changesOnly) {
		Request gridOpticsRequest = null;
		if (timestamp == null) {
			System.err.println("Creating a plain-jane RequestTopology with no timestamp");
			gridOpticsRequest = new RequestTopology(powerGridName);
		} else {
			System.err.println("Creating a fancy-pants RequestTopology with a timestamp and all the trappings");
			gridOpticsRequest = new RequestTopology(powerGridName, timestamp, changesOnly);
		}
		return (Topology) sendGridOpticsRequest(gridOpticsRequest);
	}
	
	public ContingencyResultList getLatestContingencyResults() {
		return getLatestContingencyResults(this.powerGridName);
	}
	
	public ContingencyResultList getLatestContingencyResults(String powerGridName) {
		Request request = new RequestContingencyResult(powerGridName);
		return (ContingencyResultList)sendGridOpticsRequest(request);
	}
	
	public ContingencyResultList getContingencyResults(String timestamp) {
		return getContingencyResults(this.powerGridName, timestamp);
	}
	
	public ContingencyResultList getContingencyResults(String powerGridName, String timestamp) {
		Request request = new RequestContingencyResult(powerGridName, timestamp);
		return (ContingencyResultList)sendGridOpticsRequest(request);
	}

	private Object sendGridOpticsRequest(Request request) {
		Object data = null;
		
		
		try {
			
			GossClient gridOptics = new GossClient(getMessageCredentials());
			DataResponse response = (DataResponse) gridOptics.getResponse(request);
			data = response.getData();
			if (data == null) {
				System.err.println("Response data is NULL");
			} else {
				System.err.println("Response data class: " + data.getClass().toString());
			}
		} catch (JMSException e) {
			System.err.println(e.getMessage());
			data = null;
		}
		return data;
	}
	
/*	
	private String currentTimestamp() {
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String timestamp = format.format(now);
		return timestamp;
	}
*/
}