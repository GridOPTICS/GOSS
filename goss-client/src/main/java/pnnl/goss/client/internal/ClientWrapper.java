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
package pnnl.goss.client.internal;

import pnnl.goss.client.GossClient;
import pnnl.goss.client.GossResponseEvent;
import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadRequest;

import java.util.HashMap;
import java.util.Map;

public class ClientWrapper implements GossResponseEvent {

	private Response response;
	//private HashMap<String, Request> requests = new HashMap<String, Request>();
	private Map<String,String> requestIds = new HashMap<String,String>();
	private GossClient gridOpticsClient;
	
	public ClientWrapper(){
			gridOpticsClient = new GossClient(ClientWrapper.this);
	}
	
	public void sendRequest(Request request) {
		response=null;
		gridOpticsClient.sendRequest(request);
		waitForResponse(request);
	}
	
	public void upload(UploadRequest request){
		response=null;
		gridOpticsClient.sendRequest(request);
		waitForResponse(request);
	}

	public void waitForResponse(Request towaitFor) {
		//requests.put(towaitFor.getId(), towaitFor);
		requestIds.put(towaitFor.getId(),towaitFor.getId());
		//System.out.println("Added: "+towaitFor.getRequestId());
		synchronized (towaitFor.getId()) {
			if (this.response == null) {
				try {
					towaitFor.getId().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//requests.remove(towaitFor.getId());
			requestIds.remove(towaitFor.getId());
		}
	}
	
	public void onMessage(Response response) {

		String id= requestIds.get(response.getId());
		
		//String id = response.getId();
		
		if (id != null) {
			synchronized (id) {
				this.response = response;
				id.notify();
			}
		} else {
			this.response = response;
		}
		
	}
	
	/*public void onMessage(Response response) {
		
		Request req= requests.get(data.getRequest().getRequestId());
		String id = response.getId();
		
		if (req != null) {
			synchronized (req) {
				this.response = response;
				req.notify();
			}
		} else {
			this.response = response;
		}
		
	}*/

	public Response getResponse() {
		return response;
	}
	
	
	
	public void close(){
		gridOpticsClient.close();
	}
	
	
	
}
