<%@ page session="true" %>
<%@ page import="java.io.*,java.util.*,pnnl.goss.dsa.web.GossDsaServlet,pnnl.goss.powergrid.datamodel.PowergridTimingOptions" %>
<%
	PowergridTimingOptions timingOptions = (PowergridTimingOptions)session.getAttribute(GossDsaServlet.SESSION_TIME_OPTIONS);
	if (timingOptions == null){
		response.sendRedirect("dsa-service/");
		return;
	}
	else{
		out.println("Current timing option: "+ timingOptions.getTimingOption()+"<br/>");
		if(!timingOptions.getTimingOption().equals(PowergridTimingOptions.TIME_OPTION_CURRENT)){
			out.println("Timing argument: "+timingOptions.getTimingOptionArgument()+"<br/>");			
		}		
	}
%>
<html>
<body>
	<h2>DSA Management</h2>
	
	<form action="dsa-service/update" method="post">
	<table>
		<caption>DSA Display Time</caption>
		<tr>
			<td>Option:</td>
			<td>
				
				<input type="radio" name="timeOption" id="currentTime" value="currentTime" 
					<% if(timingOptions.getTimingOption().equals(PowergridTimingOptions.TIME_OPTION_CURRENT)){ %>checked="checked" <%} %> /> Current Time<br/>
				<input type="radio" name="timeOption" id="currentTimeOffset" value="currentTimeOffset"
					<% if(timingOptions.getTimingOption().equals(PowergridTimingOptions.TIME_OPTION_OFFSET)){ %>checked="checked" <%} %> /> 
					Current Time Offset: 
					<input type="text" name="offset" id="offset" <% if(timingOptions.getTimingOption().equals(PowergridTimingOptions.TIME_OPTION_OFFSET)){ %>value="<%=timingOptions.getTimingOptionArgument() %>" <%} %> /> (+/- hh:mm ex 08:00 for +8 hours)<br />
				<input type="radio" name="timeOption" id="staticTime" value="staticTime"
					<% if(timingOptions.getTimingOption().equals(PowergridTimingOptions.TIME_OPTION_STATIC)){ %>checked="checked" <%} %> /> Static Time: 
					<input type="text" name="staticTimeValue" id="staticTimeValue" <% if(timingOptions.getTimingOption().equals(PowergridTimingOptions.TIME_OPTION_STATIC)){ %>value="<%=timingOptions.getTimingOptionArgument() %>" <%} %> /> (hh:mm:ss ex 08:00:03 must be 3-second divisible)<br />
			</td>
		</tr>
		<tr>
			<td colspan="2"><input type="submit" value="Update" /></td>
		</tr>
	</table>
	</form>
	
	
</body>
</html>
