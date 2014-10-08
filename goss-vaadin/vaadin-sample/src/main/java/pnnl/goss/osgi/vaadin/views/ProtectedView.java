package pnnl.goss.osgi.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ProtectedView extends VerticalLayout implements View{
	public void init(){
		Label success = new Label("Congratulations, you are special!");
		addComponent(success);
	}
	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

}
