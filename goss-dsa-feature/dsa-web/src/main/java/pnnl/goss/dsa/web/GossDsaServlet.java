package pnnl.goss.dsa.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GossDsaServlet extends HttpServlet {
	static String PARAM_TIME_OPTION = "timeOption";
	static String PARAM_TIME_OFFSET = "offset";
	static String TIME_OPTION_CURRENT = "currentTime";
	static String TIME_OPTION_OFFSET = "currentTimeOffset";
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		PrintWriter out = resp.getWriter();
		out.write("Got it!");
		// TODO Auto-generated method stub
		//super.doGet(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Map params = req.getParameterMap();
		PrintWriter out = resp.getWriter();
		
		out.write("Time offset option: " + req.getParameter(PARAM_TIME_OPTION) + " " + req.getParameter(PARAM_TIME_OPTION).equals(TIME_OPTION_CURRENT) + "<br />\n");
		out.write("Time offset value: " + req.getParameter(PARAM_TIME_OFFSET) + "<br />\n");
		
	}
}
