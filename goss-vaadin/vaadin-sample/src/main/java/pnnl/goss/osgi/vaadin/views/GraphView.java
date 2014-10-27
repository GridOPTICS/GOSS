package pnnl.goss.osgi.vaadin.views;

import pnnl.goss.osgi.vaadin.handlers.GraphHandler;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

public interface GraphView extends View{
	void init();
	void enter(ViewChangeEvent event);
	void setHandler(GraphHandler handler);
	void updateGraph(Object data);
}
