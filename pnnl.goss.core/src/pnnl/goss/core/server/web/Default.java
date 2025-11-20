package pnnl.goss.core.server.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class Default extends HttpServlet {

    private static final long serialVersionUID = -543706852564073624L;

    @Activate
    public void starting() {
        System.out.println("Starting");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doGet(req, resp);
    }

    @Deactivate
    public void stopping() {
        System.out.println("Stopping");
    }
}
