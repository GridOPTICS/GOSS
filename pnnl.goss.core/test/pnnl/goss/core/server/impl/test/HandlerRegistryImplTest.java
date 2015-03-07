package pnnl.goss.core.server.impl.test;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;

import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.server.HandlerNotFoundException;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.core.server.RequestUploadHandler;
import pnnl.goss.core.server.impl.HandlerRegistryImpl;


public class HandlerRegistryImplTest {
	
	private HandlerRegistryImpl registry;
	
	private class MyRequest extends Request{
		
		private static final long serialVersionUID = 402798455538154736L;
		
	}
	
	private class MyUploadRequest extends UploadRequest{
		
		private static final long serialVersionUID = 4027984612538154736L;
		
		public MyUploadRequest(Serializable data, String dataType) {
			super(data, dataType);
		}
		
	}
	
	private class MyAuthorizationHandler implements AuthorizationHandler{

		@Override
		public boolean isAuthorized(Request request, Set<String> userRoles) {
			return false;
		}
		
	}
	private class MyUploadHandler implements RequestUploadHandler{

		@Override
		public Map<String, Class<? extends AuthorizationHandler>> getHandlerDataTypes() {
			Map<String, Class<? extends AuthorizationHandler>> list = new HashMap<>();
			list.put(MyUploadRequest.class.getName(), MyAuthorizationHandler.class);
			return list;
		}

		@Override
		public Response upload(String dataType, Serializable data) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	private class MyRequestHandler implements RequestHandler{

		@Override
		public Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> getHandles() {
			Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> list = new HashMap<>();
			list.put(MyRequest.class, MyAuthorizationHandler.class);
			return list;

		}

		@Override
		public Response handle(Request request) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	@Before
	public void before(){
		registry = new HandlerRegistryImpl();
	}
	
	@Test
	public void canAddAndGetUploadHandler(){
		@SuppressWarnings("unchecked")
		ServiceReference<RequestUploadHandler> ref = (ServiceReference<RequestUploadHandler>)mock(ServiceReference.class); 
		RequestUploadHandler handler = new MyUploadHandler();
		registry.uploadHandlerAdded(ref, handler);
		try {
			RequestUploadHandler backHandler = registry.getUploadHandler(MyUploadRequest.class.getName());
			assertSame(handler, (RequestUploadHandler)backHandler);			
		} catch (HandlerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void canAddAndGetRequestHandler(){
		@SuppressWarnings("unchecked")
		ServiceReference<RequestHandler> ref = (ServiceReference<RequestHandler>)mock(ServiceReference.class); 
		RequestHandler handler = new MyRequestHandler();
		registry.requestHandlerAdded(ref, handler);
		try {
			RequestHandler backHandler = registry.getHandler(MyRequest.class);
			assertSame(handler, (RequestHandler)backHandler);			
		} catch (HandlerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}

}
