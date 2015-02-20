package pnnl.goss.core.server.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Request;
import pnnl.goss.core.RequestAsync;
import pnnl.goss.core.Response;
import pnnl.goss.core.ResponseError;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.server.HandlerNotFoundException;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.core.server.RequestHandlerInterface;
import pnnl.goss.core.server.RequestUploadHandler;
import pnnl.goss.core.server.RequestHandlerRegistry;

@Component
public class HandlerRegistryImpl implements RequestHandlerRegistry {
	private static final Logger log = LoggerFactory.getLogger(HandlerRegistryImpl.class);
	
	private final Map<ServiceReference<RequestHandler>, RequestHandler> registeredHandlers = new ConcurrentHashMap<>();
	private final Map<ServiceReference<RequestUploadHandler>, RequestUploadHandler> registeredUploadHandlers = new ConcurrentHashMap<>();
	
	private final Map<String, RequestHandler> handlers = new ConcurrentHashMap<>();
	private final Map<String, RequestUploadHandler> uploadHandlers = new ConcurrentHashMap<>();
	
			
	@ServiceDependency(removed="handlerRemoved", required=false)
	public void handlerAdded(ServiceReference<RequestHandler> ref, RequestHandler handler){
		System.out.println("Registering Service");
		registeredHandlers.put(ref, handler);
		handler.getHandles().forEach(p->handlers.put(p.getName(), handler));
	}
	
	public void handlerRemoved(ServiceReference<RequestHandler> ref){
		System.out.println("Un-Registering Service");
		registeredHandlers.get(ref).getHandles().forEach(p->handlers.remove(p));
		registeredHandlers.remove(ref);
	}
	
	
	@ServiceDependency(removed="uploadHandlerRemoved", required=false)
	public void uploadHandlerAdded(ServiceReference<RequestUploadHandler> ref, RequestUploadHandler uploadHandler){
		System.out.println("Registering Upload Service");
		registeredUploadHandlers.put(ref, uploadHandler);
		uploadHandler.getHandlerDataTypes().forEach(p->uploadHandlers.put(p, uploadHandler));
	}
	
	public void uploadHandlerRemoved(ServiceReference<RequestUploadHandler> ref){
		System.out.println("Un-Registering Upload Service");
		registeredUploadHandlers.get(ref).getHandlerDataTypes().forEach(p->uploadHandlers.remove(p));
		registeredUploadHandlers.remove(ref);
	}


	@Override
	public RequestHandler getHandler(Class<? extends Request> request) throws HandlerNotFoundException {
		log.debug("getHandler for class: "+request.getName());
		Optional<RequestHandler> maybeHandler = Optional.ofNullable(handlers.get(request.getName()));	
		return maybeHandler.orElseThrow(()-> new HandlerNotFoundException(request));
		
	}

	@Override
	public List<RequestHandlerInterface> list() {
		ArrayList<RequestHandlerInterface> items = new ArrayList<>();
		registeredHandlers.values().forEach(p->items.add(p));
		registeredUploadHandlers.values().forEach(p->items.add(p));
		return items;
	}

	@Override
	public Response handle(Request request) throws HandlerNotFoundException {
		
		RequestHandler handler = getHandler(request.getClass());
		return handler.handle(request);
	}

	@Override
	public Response handle(String dataType, Serializable data) throws HandlerNotFoundException {
		RequestUploadHandler handler = Optional.ofNullable(uploadHandlers.get(dataType))
											.orElseThrow(()-> new HandlerNotFoundException(dataType));
		return handler.upload(dataType, data);
	}

	@Override
	public Response handle(RequestAsync request) throws HandlerNotFoundException {
		RequestHandler handler = getHandler(request.getClass());
		return handler.handle(request);
	}

}
