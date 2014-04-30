package pnnl.goss.dsa.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import pnnl.goss.dsa.web.bundle.GossDsaWebActivator;
import pnnl.goss.powergrid.datamodel.PowergridTimingOptions;
import pnnl.goss.powergrid.server.PowergridContextService;

public class GossDsaServlet extends HttpServlet {
	public static final String PARAM_TIME_OPTION = "timeOption";
	public static final String PARAM_TIME_OFFSET = "offset";
	public static final String PARAM_STATIC_TIME = "staticTimeValue";
	
	public static final String TIME_OPTION_CURRENT = "currentTime";
	public static final String TIME_OPTION_OFFSET = "currentTimeOffset";
	public static final String TIME_OPTION_STATIC = "staticTime";
	
	
	public static final String SESSION_TIME_OPTIONS = "SESSION_TIME_OPTIONS";
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		PowergridContextService service = GossDsaWebActivator.getPowergridContextService();
		PowergridTimingOptions timingOptions = service.getPowergridTimingOptions();
		
		updateSessionAndRedirect(req, resp, timingOptions);
	}

	private void updateSessionAndRedirect(HttpServletRequest req, HttpServletResponse resp, PowergridTimingOptions timingOptions) throws IOException {
		HttpSession session = req.getSession();
		
		synchronized(session){
			session.setAttribute(SESSION_TIME_OPTIONS, timingOptions);
		}
		
		resp.sendRedirect("/dsa");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// TODO Validate input a bit!
		String timingOption = req.getParameter(PARAM_TIME_OPTION);
		String timingArgument= req.getParameter(PARAM_TIME_OFFSET);
		
		if(timingOption.equals(TIME_OPTION_STATIC)){
			timingArgument = req.getParameter(PARAM_STATIC_TIME);
		}
		
		PowergridContextService service = GossDsaWebActivator.getPowergridContextService();
		PowergridTimingOptions timingOptions = new PowergridTimingOptions(timingOption, timingArgument);
		service.saveTimingOptions(timingOptions);
		
		updateSessionAndRedirect(req, resp, timingOptions);
	}
}
