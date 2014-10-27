package pnnl.goss.osgi.vaadin.views;

import pnnl.goss.osgi.vaadin.handlers.OptionsHandler;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

public interface OptionsView extends View{
	public void init();
	public void enter(ViewChangeEvent event);
	void setHandler(OptionsHandler handler);

}
