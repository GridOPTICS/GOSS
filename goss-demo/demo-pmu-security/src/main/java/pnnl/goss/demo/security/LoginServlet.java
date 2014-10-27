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
package pnnl.goss.demo.security;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import pnnl.goss.demo.security.util.DemoConstants;

@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet{
	protected Logger log = Logger.getLogger(LoginServlet.class);

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String username = request.getParameter(DemoConstants.USERNAME_CONSTANT);
        String password = request.getParameter(DemoConstants.PASSWORD_CONSTANT);
        
        //TODO need to get properties from config rather than hardcoded
        
        log.debug("Checking login");
        if (username == null || password == null) {

            log.warn("PMU Demo LoginServlet: Invalid paramters ");
        }

        // Here you put the check on the username and password
		
		String server="eioc-goss";
		String port="10389";
		// Connect to the LDAP server.
		Hashtable<String, Object> env = new Hashtable<String, Object>(5);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://" + server + ":" + port + "/");
		// Authenticate
		String binddn = "uid="+username+",ou=users,ou=goss,ou=system";
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, binddn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        try{
        InitialLdapContext ldap = new InitialLdapContext(env, null);
			log.debug("PMU Demo LoginServlet: Authentication successful");
//            System.out.println("Welcome " + username + " <a href=\"index.jsp\">Back to main</a>");
//            response.getWriter().print("Welcome " + username + " <a href=\"index.jsp\">Back to main</a>");
			
			session.setAttribute(DemoConstants.USERNAME_CONSTANT, username);
			session.setAttribute(DemoConstants.PASSWORD_CONSTANT, password);
			response.sendRedirect("frequency-plot.jsp");
        }catch (AuthenticationException e){
        	log.info("PMU Demo LoginServlet: Authentication failed");
        	response.getWriter().print("Invalid username and password  <a href=\""+DemoConstants.LOGIN_URL+"\">Try again</a>");
        }catch (Exception e) {
        	e.printStackTrace();
        	response.getWriter().print("Unknown authentication Exception  <a href=\""+DemoConstants.LOGIN_URL+"\">Try again</a>");
       }
	}
}
