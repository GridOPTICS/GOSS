package pnnl.goss.osgi.vaadin.securitydemo.impl;


import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.osgi.vaadin.DemoPMUService;

import com.vaadin.data.Container;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class DemoComponent extends CustomComponent {
	private static final Logger log = LoggerFactory.getLogger(DemoComponent.class);

	protected DemoPMUService gossService;
	protected OptionGroup pmus;
	public DemoComponent(GridLayout content){
		
		
		// load an service out of OSGi
				BundleContext context = ((DemoServlet)VaadinServlet.getCurrent()).getBundleContext();
				ServiceReference ref = context.getServiceReference(DemoPMUService.class.getName());
				gossService = (DemoPMUService) context.getService(ref);
				if(gossService==null){ 
					log.warn("gossService should not be null!");
				}
				

		        
		        
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
		        content.addComponent(verticalInputs, 0, 1);
		        
		        
		        List<String> phasorNames = gossService.requestPMUList();
		        //checkboxes
		        pmus = new OptionGroup("PMUs", phasorNames);
		        pmus.setMultiSelect(true);
		        content.addComponent(pmus, 1, 1);
		        
		        
		        //Poll for data button
		        Button startPollingBtn = new Button("Start Polling");
		        //Stop poll for data button (disabled unless polling)
		        Button stopPollingBtn = new Button("Stop Polling");
		        stopPollingBtn.setEnabled(false);
		        
		        
		        
		        //actions for buttons
		        startPollingBtn.addClickListener(new StartClickListener(stopPollingBtn));
		        stopPollingBtn.addClickListener(new StopClickListener(startPollingBtn));
		        HorizontalLayout horizontalButtons = new HorizontalLayout();
		        content.addComponent(horizontalButtons, 0, 2);
		        horizontalButtons.addComponent(startPollingBtn);
		        horizontalButtons.addComponent(stopPollingBtn);
		        
	}
	
	DataGetter dataGetter = new DataGetter();
	private class StartClickListener implements ClickListener{
		Button startButton;
		Button stopButton;
		public StartClickListener(Button stopButton){
			this.stopButton = stopButton;
		}
		
		@Override
		public void buttonClick(ClickEvent event) {
			this.startButton = event.getButton();
			stopButton.setEnabled(true);
			startButton.setEnabled(false);
			dataGetter.running = true;
			dataGetter.startButtonListener = this;
			dataGetter.start();
		}
		
		
		public void resetButtons(){
			System.out.println("RESET "+startButton);
			if(startButton!=null){
				startButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		}
	}
	
	private class StopClickListener implements ClickListener{
		Button startButton;
		public StopClickListener(Button startButton){
			this.startButton = startButton;
		}
		
		@Override
		public void buttonClick(ClickEvent event) {
			startButton.setEnabled(true);
			event.getButton().setEnabled(false);
			dataGetter.running = false;
			dataGetter.interrupt();
		}
		
	}
	
	
	
	
	private class DataGetter extends Thread{

		boolean running = false;
		StartClickListener startButtonListener;
		
		@Override
		public void run() {
			try {
				while(running){
					System.out.println("REQUESTING DATA ");
					Collection value = (Collection)pmus.getValue();
					
					Long lStart = new Long("1270105201455");
					Long lEnd = new Long("1270105700980");
					
					System.out.println("VALUE IS "+value+"   "+value.getClass());
					gossService.requestPMUData(new Date(lStart), new Date(lEnd), value);
				
				
				
					wait(1000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error("Interupted exception"+e.getMessage(), e);
				System.out.println("LISTENER "+startButtonListener);
				if(startButtonListener!=null){
					startButtonListener.resetButtons();
				}
			} catch(Throwable e){
				log.error("Other exception"+e.getMessage(), e);
				System.out.println("LISTENER "+startButtonListener);
				if(startButtonListener!=null){
					startButtonListener.resetButtons();
				}
			}
			
		}
		
	}
}
