package pnnl.goss.osgi.vaadin.views.impl;

import java.util.List;

import pnnl.goss.osgi.vaadin.handlers.OptionsHandler;
import pnnl.goss.osgi.vaadin.util.DemoConstants;
import pnnl.goss.osgi.vaadin.views.OptionsView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class OptionsViewImpl extends GridLayout implements OptionsView{
	
	
	private OptionsHandler handler;
	protected OptionGroup pmus;

	public OptionsViewImpl(){
		setColumns(2);
		setRows(4);
		setSizeFull();
        addStyleName("view-content");
        setMargin(true);
        setSpacing(true);
        
        Label label = new Label("Welcome to Security Demo 0.0002");
    	label.addStyleName("heading");
    	addComponent(label, 0, 0, 1, 0);
	}
	
	
	@Override
	public void setHandler(OptionsHandler handler) {
		this.handler = handler;
	}
	
	public void init(){
//		Label success = new Label("Congratulations, you are special!");
//		addComponent(success);
		
		//TODO add more here
		//Add after button click to to graph view
		

        //text box poll frequency
        TextField pollFreq = new TextField("Poll Frequency (sec)", "5");
        //text box time shown
        TextField timeShown = new TextField("Time Shown (minutes)", "10");
        //text box start time 
        PopupDateField startTime = new PopupDateField("Start");
        startTime.setResolution(Resolution.SECOND);
        startTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
        VerticalLayout verticalInputs = new VerticalLayout();
        verticalInputs.addComponent(pollFreq);
        verticalInputs.addComponent(timeShown);
        verticalInputs.addComponent(startTime);
        addComponent(verticalInputs, 0, 1);
        
        
        List<String> phasorNames = handler.requestPMUList();
        //checkboxes
        pmus = new OptionGroup("PMUs", phasorNames);
        pmus.setMultiSelect(true);
        addComponent(pmus, 1, 1);
        
        
        //Poll for data button
        Button startPollingBtn = new Button("Start Polling");
        
        //when start is clicked, go to the graph view
        startPollingBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				System.out.println("GO TO GRAPH VIEW");
				//TODO verify options, then go to next view
				UI.getCurrent().getNavigator().navigateTo(DemoConstants.GRAPH_VIEW);
			}
		});
        HorizontalLayout horizontalButtons = new HorizontalLayout();
        addComponent(horizontalButtons, 0, 2);
        horizontalButtons.addComponent(startPollingBtn);
		
		
	}
	
	
	
	
	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	
}
