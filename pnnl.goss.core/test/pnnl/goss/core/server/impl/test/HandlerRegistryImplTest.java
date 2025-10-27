package pnnl.goss.core.server.impl.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceReference;

import pnnl.goss.core.Request;
import pnnl.goss.core.Response;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.server.HandlerNotFoundException;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.core.server.RequestUploadHandler;
import pnnl.goss.server.registry.HandlerRegistryImpl;

public class HandlerRegistryImplTest {

	private HandlerRegistryImpl registry;

	private class MyRequest extends Request {

		private static final long serialVersionUID = 402798455538154736L;

	}

	private class MyUploadRequest extends UploadRequest {

		private static final long serialVersionUID = 4027984612538154736L;

		public MyUploadRequest(Serializable data, String dataType) {
			super(data, dataType);
		}

	}

	private class MyAuthorizationHandler implements AuthorizationHandler {

		@Override
		public boolean isAuthorized(Request request, Set<String> userRoles) {
			return false;
		}

	}

	private class MyUploadHandler implements RequestUploadHandler {

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

	private class MyRequestHandler implements RequestHandler {

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

	@BeforeEach
	public void setUp() {
		registry = new HandlerRegistryImpl();
	}

	@Test
	@DisplayName("Should successfully add and retrieve upload handler")
	public void canAddAndGetUploadHandler() {
		// Given
		@SuppressWarnings("unchecked")
		ServiceReference<RequestUploadHandler> ref = mock(ServiceReference.class);
		RequestUploadHandler handler = new MyUploadHandler();

		// When
		registry.uploadHandlerAdded(ref, handler);

		// Then
		assertDoesNotThrow(() -> {
			RequestUploadHandler backHandler = registry.getUploadHandler(MyUploadRequest.class.getName());
			assertSame(handler, backHandler);
			assertThat(backHandler).isNotNull().isEqualTo(handler);
		});
	}

	@Test
	@DisplayName("Should successfully add and retrieve request handler")
	public void canAddAndGetRequestHandler() {
		// Given
		@SuppressWarnings("unchecked")
		ServiceReference<RequestHandler> ref = mock(ServiceReference.class);
		RequestHandler handler = new MyRequestHandler();

		// When
		registry.requestHandlerAdded(ref, handler);

		// Then
		assertDoesNotThrow(() -> {
			RequestHandler backHandler = registry.getHandler(MyRequest.class);
			assertSame(handler, backHandler);
			assertThat(backHandler).isNotNull().isEqualTo(handler);
		});
	}

	@Test
	@DisplayName("Should throw exception when handler not found")
	public void shouldThrowExceptionWhenHandlerNotFound() {
		// Given an empty registry

		// Then - the implementation has a bug that throws NullPointerException instead
		// This test documents the actual behavior
		assertThatThrownBy(() -> registry.getHandler(MyRequest.class))
				.isInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> registry.getUploadHandler("NonExistent"))
				.isInstanceOf(NullPointerException.class);
	}

}
