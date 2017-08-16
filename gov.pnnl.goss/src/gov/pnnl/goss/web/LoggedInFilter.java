//package gov.pnnl.goss.web;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
////import java.util.Enumeration;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//
//import gov.pnnl.goss.server.api.TokenIdentifierMap;
//
///**
// * This filter tests that a user has logged in before allowing
// * access to the requested resource.  It does this by using a
// * {@link TokenIdentifierMap} based service that will check the
// * ip address and the pressence of a valid token.
// *
// * If a valid token is present then the request will modified to
// * include an "identifier" parameter that can be used in a web request
// * to authenticate a user's permissions.
// *
// * @author Craig Allwardt
// *
// */
//public class LoggedInFilter implements Filter
//{
//
//	// Injected by Activator
//	private volatile TokenIdentifierMap idMap;
//
//
//    @Override
//    public void init(FilterConfig config)
//        throws ServletException
//    {
//        System.out.println("Initializing filter with config: "+config);
//    }
//
//    /**
//     * Retrieves a token from the passed request.  The token could be
//     * in a header if a GET request or in either the header or body
//     * of the request if a POST request.
//     *
//     * @param request
//     * @return The token or a null string.
//     */
//    private String getTokenIfPresent(HttpServletRequest request){
//
//    	String token = request.getHeader("AuthToken");
//
//    	// Not available through the header
//    	if (token == null || token.isEmpty()){
//
//    		// If POST request then check the content of the body for an
//    		// AuthToken element
//    		if (request.getMethod().equalsIgnoreCase("POST")){
//    			StringBuilder body = new StringBuilder();
//        		char[] charBuffer = new char[128];
//        		InputStream inputStream;
//				try {
//					inputStream = request.getInputStream();
//					int bytesRead = -1;
//	        		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//	        		while ((bytesRead = reader.read(charBuffer)) > 0) {
//	        			body.append(charBuffer, 0, bytesRead);
//	        		}
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//				
//				if (!body.toString().isEmpty()){
//
//	        		try {
//
//	        			Gson gson = new Gson();
//
//	        			JsonObject json = gson.fromJson(body.toString(), JsonObject.class);
//	        			token = json.get("AuthToken").getAsString();
//
//	        			// Return a null for an empty token string.
//	        			if (token.isEmpty()){
//	        				token = null;
//	        			}
//
//
//	        		}catch (Exception e){
//	        			e.printStackTrace();
//	        		}
//				}
//    		}
//    	}
//
//    	return token;
//    }
//
//    /*
//     * This function is designed to validate that a user has been logged into
//     * the system and made a request within a period of time.  The time is
//     * not determined in this class but in the {@link TokenIdentifiedMap} service.
//     * In addition the token and ip address will be checked to make sure the
//     * origin of the request is from the same ip.
//     *
//     * If the request is a GET request then the header AuthToken must be present
//     * with a validated token.  If a POST request then the AuthToken can either
//     * be present in the header or in a json body element.
//     *
//     * If the AuthToken is valid then an 'identifier' parameter will be set on the
//     * request before it is sent to the next filter.
//     *
//     * If the AuthToken is not valid or is invalid then 401 header is set and an
//     * error message is produced.
//     *
//     * (non-Javadoc)
//     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
//     */
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
//        throws IOException, ServletException
//    {
//    	HttpServletRequest httpReq = (HttpServletRequest) req;
//    	MultiReadHttpServletRequestWrapper wrapper = new MultiReadHttpServletRequestWrapper(httpReq);   	    	
//    	String authToken = getTokenIfPresent(wrapper);
//    	String ip = httpReq.getRemoteAddr();
//    	String identifier = null;
//    	boolean identifierSet = false;
//
//    	if (authToken != null){
//    		identifier = idMap.getIdentifier(ip, authToken);
//    		if (identifier != null && !identifier.isEmpty()){
//    			wrapper.setAttribute("identifier", identifier);
//    			identifierSet = true;
//    		}
//    	}
//
//    	if (!identifierSet){
//    		((HttpServletResponse)res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//    		PrintWriter out = res.getWriter();
//			out.write("{\"error\":\"Invalid Authentication Token\"}");
//			out.close();
//			return;
//    	}
//
//        System.out.println("Identifier set: "+identifier);
//        chain.doFilter(wrapper, res);
//    }
//
//	@Override
//	public void destroy() {
//		System.out.println("Destroying filter.");
//	}
//}
