package pnnl.goss.osgi.vaadin.presenters;

import pnnl.goss.osgi.vaadin.handlers.GraphHandler;
import pnnl.goss.osgi.vaadin.service.GraphService;
import pnnl.goss.osgi.vaadin.views.GraphView;


public class GraphPresenter implements GraphHandler {

	
	private GraphView view;
    private GraphService service;

    public GraphPresenter(GraphView view, GraphService service) {
        this.view = view;
        this.service = service;
    }

	@Override
	public void pollForData() {
		// TODO poll for data
		//service.getdata
		//view.updateGraph(data)
		
	}
} 

