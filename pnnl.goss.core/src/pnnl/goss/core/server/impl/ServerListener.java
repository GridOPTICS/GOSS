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
package pnnl.goss.core.server.impl;

import java.io.Serializable;

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Event;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.RequestAsync;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.security.SecurityConstants;
import pnnl.goss.core.server.RequestHandlerRegistry;
//import pnnl.goss.security.util.GossSecurityConstants;
//import pnnl.goss.security.core.GossSecurityConstants;
//import pnnl.goss.security.core.SecurityRequestHandler;
//import pnnl.goss.security.util.GossSecurityConstants;

//import org.apache.http.impl.cookie.RFC2109DomainHandler;

public class ServerListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(ServerListener.class);
   
    
    private volatile RequestHandlerRegistry handlerRegistry;
    
    private Session session;
    boolean useAuth = true;

    
    public ServerListener setSession(Session session){
    	this.session = session;
    	return this;
    }
    
    public ServerListener setRegistryHandler(RequestHandlerRegistry registry){
    	this.handlerRegistry = registry;
    	return this;
    }

    public void onMessage(Message message1) {

        final Message message = message1;
        log.debug("Message of type: "+ message1.getClass() + " received");

        Thread thread = new Thread(new Runnable() {
            public void run() {
                ServerPublisher serverPublisher = new ServerPublisher(session);
            	String username = "";
                try {
                    ObjectMessage objectMessage = (ObjectMessage) message;

                    // Assume that the passed object on the wire is of type Request.  An error will be thrown
                    // if that is not the case.
                    Request request = (Request) objectMessage.getObject();
                    log.debug("Handling request type: " + request.getClass());
                    
                    if (useAuth) {
                    	if (!message.getBooleanProperty(SecurityConstants.HAS_SUBJECT_HEADER)){
                    		log.error("Identifier not set in message header");
                    		serverPublisher.sendErrror("Invalid subject in message!", message.getJMSReplyTo());
                    		return;
                    		
                    	}
                    	
                    	String identifier = message.getStringProperty(SecurityConstants.SUBJECT_HEADER);
                    	username = identifier;
                    	boolean allowed = handlerRegistry.checkAccess(request, identifier); 
                    	
                    	if (!allowed){
                    		log.info("Access denied to "+identifier+" for request type "+request.getClass().getName());
                    		serverPublisher.sendErrror("Access Denied for the requested data", message.getJMSReplyTo());
                    		return;
                    	}
                    	log.debug("Access allowed to the request");
                    }
                    

                    if (request instanceof UploadRequest) {

                        try {
                            UploadRequest uploadRequest = (UploadRequest) request;

                            String dataType = uploadRequest.getDataType();
                            Serializable data = uploadRequest.getData();
                            
                            UploadResponse response = (UploadResponse) handlerRegistry.handle(dataType, data);
                            response.setId(request.getId());
                            serverPublisher.sendResponse(response, message.getJMSReplyTo());

                            //TODO: Added capability for event processing without upload. Example - FNCS
                            /*UploadResponse response = new UploadResponse(true);
                            response.setId(request.getId());
                            serverPublisher.sendResponse(response, message.getJMSReplyTo());*/

                            if (data instanceof Event) {
                                DataResponse dataResponse = new DataResponse();
                                dataResponse.setUsername(username);
                                dataResponse.setData(data);
                                serverPublisher.sendEvent(dataResponse, data.getClass().getName().substring(data.getClass().getName().lastIndexOf(".") + 1));
                                serverPublisher.close();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("Upload request failed!" + e);
                            UploadResponse uploadResponse = new UploadResponse(false);
                            uploadResponse.setMessage(e.getMessage());
                            serverPublisher.sendResponse(uploadResponse, message.getJMSReplyTo());
                            serverPublisher.close();
                        }
                    } else if (request instanceof RequestAsync) {

                        RequestAsync requestAsync = (RequestAsync)request;

                        //AbstractRequestHandler handler = handlerService.getHandler(request);

                        DataResponse response = (DataResponse) handlerRegistry.handle(request);
                        response.setUsername(username);
                        response.setId(request.getId());

                        if (message.getStringProperty("RESPONSE_FORMAT") != null) {
                            serverPublisher.sendResponse(response, message.getJMSReplyTo(), RESPONSE_FORMAT.valueOf(message.getStringProperty("RESPONSE_FORMAT")));
                        }
                        else {
                            serverPublisher.sendResponse(response, message.getJMSReplyTo(), null);
                        }

                        while(response.isResponseComplete()==false){
                            Thread.sleep(requestAsync.getFrequency());
                            response = (DataResponse) handlerRegistry.handle(request);
                            response.setId(request.getId());

                            if (message.getStringProperty("RESPONSE_FORMAT") != null) {
                                serverPublisher.sendResponse(response, message.getJMSReplyTo(), RESPONSE_FORMAT.valueOf(message.getStringProperty("RESPONSE_FORMAT")));
                            }
                            else {
                                serverPublisher.sendResponse(response, message.getJMSReplyTo(), null);
                            }
                        }
                    }
                    else {

                        DataResponse response = (DataResponse) handlerRegistry.handle(request);

                        //DataResponse response = (DataResponse) ServerRequestHandler.handle(request);
                        response.setResponseComplete(true);
                        response.setUsername(username);
                        response.setId(request.getId());

                        if (message.getStringProperty("RESPONSE_FORMAT") != null)
                            serverPublisher.sendResponse(response, message.getJMSReplyTo(), RESPONSE_FORMAT.valueOf(message.getStringProperty("RESPONSE_FORMAT")));
                        else
                            serverPublisher.sendResponse(response, message.getJMSReplyTo(), null);
                        //System.out.println(System.currentTimeMillis());
                        }

                } catch (InvalidDestinationException e) {

                    e.printStackTrace();
                    try {
                    	DataResponse response = new DataResponse(new DataError("Exception occured: "+e.getMessage()));
                    	response.setUsername(username);
                        serverPublisher.sendResponse(response , message.getJMSReplyTo());
                    } catch (JMSException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    serverPublisher.close();
                } catch (Exception e) {

                    e.printStackTrace();
                    try {
                    	DataResponse response = new DataResponse(new DataError("Exception occured"));
                    	response.setUsername(username);
                        serverPublisher.sendResponse(response , message.getJMSReplyTo());
                    } catch (JMSException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    serverPublisher.close();
                }
                catch(Throwable t){
                    t.printStackTrace();
                }
                finally {

                }

            }

        });

        thread.start();

    }

}