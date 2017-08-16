package gov.pnnl.goss.server.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.shiro.mgt.SecurityManager;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.northconcepts.exception.SystemException;

import gov.pnnl.goss.client.GossUploadRequest;
import gov.pnnl.goss.client.api.Request;
import gov.pnnl.goss.client.api.RequestAsync;
import gov.pnnl.goss.client.api.Response;
import gov.pnnl.goss.security.api.AuthorizationHandler;
import gov.pnnl.goss.security.api.PermissionAdapter;
import gov.pnnl.goss.server.api.HandlerNotFoundException;
import gov.pnnl.goss.server.api.RequestHandler;
import gov.pnnl.goss.server.api.RequestHandlerInterface;
import gov.pnnl.goss.server.api.RequestHandlerRegistry;
import gov.pnnl.goss.server.api.RequestUploadHandler;


@SuppressWarnings("restriction")
@Component
public class HandlerRegistryImpl implements RequestHandlerRegistry {
	private static final Logger log = LoggerFactory.getLogger(HandlerRegistryImpl.class);
	
	// Keep track of the service references so that when they go away w0e can clean up the list.
	private final Map<ServiceReference<RequestHandler>, RequestHandler> registeredHandlers = new ConcurrentHashMap<>();
	private final Map<ServiceReference<AuthorizationHandler>, AuthorizationHandler> authorizationHandlers = new ConcurrentHashMap<>();
	private final Map<ServiceReference<RequestUploadHandler>, RequestUploadHandler> registeredUploadHandlers = new ConcurrentHashMap<>();
	
	@ServiceDependency
	private volatile SecurityManager securityManager;
	
	@ServiceDependency
	private volatile PermissionAdapter permissionAdapter;
		
	// Map
	private final Map<String, UploadHandlerMapping> uploadHandlers = new ConcurrentHashMap<>();
	
	// HandlerMapping now takes care of the mapping of requests through to authorization class name.
	// The actual instances are then looked up in the authorizationInstanceMap.
	private final Map<String, HandlerMapping> handlerMapping = new ConcurrentHashMap<>();
	private final Map<String, AuthorizationHandler> authorizationInstanceMap = new ConcurrentHashMap<>();
	
	private class UploadHandlerMapping{
		private volatile String uploadDataType;
		private volatile String authorizationHandlerClassName;
		private volatile RequestUploadHandler uploadRequestHandlerInstance;
				
		@SuppressWarnings("unused")
		public String getUploadDataType() {
			return uploadDataType;
		}
		public UploadHandlerMapping setDataType(String uploadDataType) {
			this.uploadDataType = uploadDataType;
			return this;
		}
		public String getAuthorizationHandlerClassName() {
			return authorizationHandlerClassName;
		}
		public UploadHandlerMapping setAuthorizationHandlerClassName(
				String authorizationHandlerClassName) {
			this.authorizationHandlerClassName = authorizationHandlerClassName;
			return this;
		}
		public RequestUploadHandler getRequestHandlerInstance() {
			return uploadRequestHandlerInstance;
		}
		public UploadHandlerMapping setRequestHandlerInstance(RequestUploadHandler uploadRequestHandlerInstance) {
			this.uploadRequestHandlerInstance = uploadRequestHandlerInstance;
			return this;
		}
	}
	
	private class HandlerMapping{
		private volatile String requestClassName;
		private volatile String authorizationHandlerClassName;
		private volatile RequestHandler requestHandlerInstance;
				
		@SuppressWarnings("unused")
		public String getRequestClassName() {
			return requestClassName;
		}
		public HandlerMapping setRequestClassName(String requestClassName) {
			this.requestClassName = requestClassName;
			return this;
		}
		@SuppressWarnings("unused")
		public String getAuthorizationHandlerClassName() {
			return authorizationHandlerClassName;
		}
		public HandlerMapping setAuthorizationHandlerClassName(
				String authorizationHandlerClassName) {
			this.authorizationHandlerClassName = authorizationHandlerClassName;
			return this;
		}
		public RequestHandler getRequestHandlerInstance() {
			return requestHandlerInstance;
		}
		public HandlerMapping setRequestHandlerInstance(RequestHandler requestHandlerInstance) {
			this.requestHandlerInstance = requestHandlerInstance;
			return this;
		}
	}
	
	
	@ServiceDependency(removed="authorizationHandlerRemoved", required=false)
	public void authorizationHandlerAdded(ServiceReference<AuthorizationHandler> ref, AuthorizationHandler handler){
		System.out.println("Registering Authorization Handler: "+handler.getClass().getName());
		authorizationHandlers.put(ref, handler);
		authorizationInstanceMap.put(handler.getClass().getName(), handler);
	}
	
	public void authorizationHandlerRemoved(ServiceReference<AuthorizationHandler> ref){
		
		AuthorizationHandler handler = authorizationHandlers.remove(ref);
		System.out.println("Un-Registering Authorization Handler: "+handler.getClass().getName());
		authorizationInstanceMap.remove(handler.getClass().getName());
	}
			
	@ServiceDependency(removed="requestHandlerRemoved", required=false)
	public void requestHandlerAdded(ServiceReference<RequestHandler> ref, RequestHandler handler){
		System.out.println("Registering Request Handler: "+handler.getClass().getName());
		registeredHandlers.put(ref, handler);
		handler.getHandles().forEach((k, v)->{
			handlerMapping.put(k.getName(), new HandlerMapping()
										.setRequestClassName(k.getName())
										.setRequestHandlerInstance(handler)
										.setAuthorizationHandlerClassName(v.getName()));
		});
	}
	
	public void requestHandlerRemoved(ServiceReference<RequestHandler> ref){
		
		RequestHandler handler = registeredHandlers.remove(ref);
		System.out.println("Un-Registering Request Handler: "+ handler.getClass().getName());
		handler.getHandles().forEach((k,v)->{
			handlerMapping.remove(k);
		});
		registeredHandlers.remove(ref);
	}
	
	
	@ServiceDependency(removed="uploadHandlerRemoved", required=false)
	public void uploadHandlerAdded(ServiceReference<RequestUploadHandler> ref, RequestUploadHandler uploadHandler){
		System.out.println("Registering Upload Handler: "+uploadHandler.getClass().getName());
		registeredUploadHandlers.put(ref, uploadHandler);
		uploadHandler.getHandlerDataTypes().forEach((k, v)-> {
			uploadHandlers.put(k, new UploadHandlerMapping()
									.setDataType(k)
									.setAuthorizationHandlerClassName(v.getName())
									.setRequestHandlerInstance(uploadHandler));
		});
	}
	
	public void uploadHandlerRemoved(ServiceReference<RequestUploadHandler> ref){
		RequestUploadHandler handler = registeredUploadHandlers.remove(ref);
		System.out.println("Un-Registering Upload Handler: "+handler.getClass().getName());
		handler.getHandlerDataTypes().forEach((k,v)->{
			uploadHandlers.remove(k);
		});
		uploadHandlers.remove(handler.getClass().getName());
	}


	@Override
	public RequestHandler getHandler(Class<? extends Request> request) throws HandlerNotFoundException {
		log.debug("getHandler for class: "+request.getName());
		Optional<RequestHandler> maybeHandler = Optional.ofNullable(
				handlerMapping.get(request.getName()).getRequestHandlerInstance());	
		return maybeHandler.orElseThrow(()-> new HandlerNotFoundException(request));
		
	}

	@Override
	public List<RequestHandlerInterface> list() {
		ArrayList<RequestHandlerInterface> items = new ArrayList<>();
		registeredHandlers.values().forEach(p->items.add(p));
		registeredUploadHandlers.values().forEach(p->items.add(p));
		authorizationHandlers.values().forEach(p->items.add(p));
		
		return items;
	}

	@Override
	public Response handle(Request request) throws HandlerNotFoundException {
		
		RequestHandler handler = getHandler(request.getClass());
		return handler.handle(request);
		
	}

	@Override
	public Response handle(String dataType, Serializable data) throws HandlerNotFoundException {
		log.debug("handling datatype: "+ dataType);
		RequestUploadHandler handler = Optional
				.ofNullable(uploadHandlers.get(dataType).getRequestHandlerInstance())
				.orElseThrow(()-> new HandlerNotFoundException(dataType));
		return handler.upload(dataType, data);
	}

	@Override
	public Response handle(RequestAsync request) throws HandlerNotFoundException {
		log.debug("handling async request:");
		RequestHandler handler = (RequestHandler)getHandler(request.getClass());
		return handler.handle(request);
	}

	@Override
	public RequestUploadHandler getUploadHandler(String dataType)
			throws HandlerNotFoundException {
		return uploadHandlers.get(dataType).getRequestHandlerInstance();
	}

	@Override
	public boolean checkAccess(Request request, String identifier)
			throws SystemException {
		
		AuthorizationHandler authHandler = null;
		log.debug("Checking access for request " + request.getClass() + " identifier " + identifier);
		if (request instanceof GossUploadRequest){
			// Upload request handling.
			log.debug("Handle auth request for upload!");
			GossUploadRequest upRquest = (GossUploadRequest)request;
			UploadHandlerMapping mapTo = uploadHandlers.get(upRquest.getDataType());
			authHandler = authorizationInstanceMap.get(mapTo.getAuthorizationHandlerClassName());
		}
		else {
			HandlerMapping requestToHandlerMapping = handlerMapping.get(request.getClass().getName());
			authHandler = authorizationInstanceMap.get(requestToHandlerMapping.authorizationHandlerClassName);
		}
		
		if (authHandler == null){
			return false;
		}
		return authHandler.isAuthorized(request, permissionAdapter.getPermissions(identifier));
	}
	
	
}
