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
package pnnl.goss.client.tests;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

import pnnl.goss.client.tests.util.ClientAuthHelper;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.client.GossClient;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegment;
import pnnl.goss.sharedperspective.common.datamodel.ContingencyResultList;
import pnnl.goss.sharedperspective.common.datamodel.Substation;
import pnnl.goss.sharedperspective.common.datamodel.Topology;
import pnnl.goss.sharedperspective.common.requests.RequestContingencyResult;
import pnnl.goss.sharedperspective.common.requests.RequestLineLoad;
import pnnl.goss.sharedperspective.common.requests.RequestTopology;

public class ClientMainDSA {

	public static void main(String args[]) {

		String regionName = "Greek-118-North"; // or Greek-118-South
		String timestamp = "2013-08-01 10:12:12";

		// get base (full) topology
		getBaseTopology(regionName);

		// get base (full) topology in xml
		getBaseTopologyXML(regionName);

		// get base (full) topology for given timestamp
		getBaseTopology(regionName, timestamp);

		// get topology update since given timestamp
		getTopologyUpdate(regionName, timestamp, true);

		// get Line load data for given timestamp
		getLineLoad(regionName, timestamp);

		// get latest CA results
		getContingencyResults(regionName);

		// get CA result for given timestamp
		getContingencyResults(regionName, timestamp);

	}

	private static void getBaseTopology(String regionName) {
		GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
		DataResponse response = null;
		try {
			Request request = new RequestTopology(regionName);
			response = (DataResponse) client.getResponse(request);
			Topology topology = (Topology) response.getData();
			for (ACLineSegment ac : topology.getAcLineSegments()) {
				System.out.println("Line seg: " + ac.getName());
				for (Substation s : ac.getSubstations()) {
					System.out.println("\tfrom ss: " + s.getName() + " to suss: " + s.getName() + " totalpload: " + s.getTotalPLoad() + " totalqload: " + s.getTotalQGen() + " totalpgen: " + s.getTotalPGen() + " totalqgen: " + s.getTotalQGen() + " totalmaxmva: " + s.getTotalMaxMva());
				}
			}
			client.close();

		} catch (ClassCastException e) {
			if (response != null) {
				DataError error = (DataError) response.getData();
				System.out.println(error.getMessage());
			} else
				throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getBaseTopology(String regionName, String timestamp) {
		GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
		DataResponse response = null;
		try {
			Request request = new RequestTopology(regionName, timestamp);
			response = (DataResponse) client.getResponse(request);
			Topology topology1 = (Topology) response.getData();
			System.out.println(topology1.getRegion());
			System.out.println(topology1.getAcLineSegments().get(0).getName());
			System.out.println(topology1.getAcLineSegments().size());
			client.close();
		} catch (ClassCastException e) {
			if (response != null) {
				DataError error = (DataError) response.getData();
				System.out.println(error.getMessage());
			} else
				throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getBaseTopologyXML(String regionName) {
		GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
		try {
			Request request = new RequestTopology(regionName);
			String topologyXML = client.getResponse(request, RESPONSE_FORMAT.XML).toString();
			FileUtils.writeStringToFile(new File(regionName + ".xml"), StringEscapeUtils.unescapeHtml(topologyXML));
			// XStream xStream = new XStream();
			// xStream.toXML(topologyXML, new FileOutputStream("north1.xml"));
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getTopologyUpdate(String regionName, String timestamp, Boolean update) {
		GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
		DataResponse response = null;
		try {
			Request request = new RequestTopology(regionName, timestamp, true);
			response = (DataResponse) client.getResponse(request);
			Topology topology = (Topology) response.getData();
			System.out.println(topology.getAcLineSegments().get(0).getName());
			System.out.println(topology.getAcLineSegments().size());
			client.close();
		} catch (ClassCastException e) {
			if (response != null) {
				DataError error = (DataError) response.getData();
				System.out.println(error.getMessage());
			} else
				throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getLineLoad(String regionName, String timestamp) {
		GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
		try {
			Request request = new RequestLineLoad(regionName, timestamp);
			String topologyXML = client.getResponse(request, RESPONSE_FORMAT.XML).toString();
			FileUtils.writeStringToFile(new File(regionName + "_lineLoad.xml"), StringEscapeUtils.unescapeHtml(topologyXML));
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getContingencyResults(String regionName) {
		GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
		DataResponse response = null;
		try {
			Request request = new RequestContingencyResult(regionName);
			response = (DataResponse) client.getResponse(request);
			ContingencyResultList resultList = (ContingencyResultList) response.getData();
			System.out.println(resultList.getResultList().size());
			client.close();
		} catch (ClassCastException e) {
			if (response != null) {
				DataError error = (DataError) response.getData();
				System.out.println(error.getMessage());
			} else
				throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getContingencyResults(String regionName, String timestamp) {
		GossClient client = new GossClient(ClientAuthHelper.getPMUCredentials());
		DataResponse response = null;
		try {
			Request request = new RequestContingencyResult(regionName, timestamp);
			response = (DataResponse) client.getResponse(request);
			ContingencyResultList resultList = (ContingencyResultList) response.getData();
			System.out.println(resultList.getResultList().size());
			client.close();
		} catch (ClassCastException e) {
			if (response != null) {
				DataError error = (DataError) response.getData();
				System.out.println(error.getMessage());
			} else
				throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
