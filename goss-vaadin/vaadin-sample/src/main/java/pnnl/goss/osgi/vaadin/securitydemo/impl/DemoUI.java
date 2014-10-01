package pnnl.goss.osgi.vaadin.securitydemo.impl;





import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.osgi.vaadin.GossService;
import pnnl.goss.osgi.vaadin.GossServiceImpl;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;



@Theme("vaadinsample")
@Component(name="DemoUI", immediate=true)
public class DemoUI extends UI {

	@Requires
	protected GossService gossService;
	private static final Logger log = LoggerFactory.getLogger(GossServiceImpl.class);

	private static final long serialVersionUID = 1L;
//	private VerticalLayout panelContnent;

	@Override
	protected void init(VaadinRequest request) {
		
		// load an service out of OSGi
		BundleContext context = ((DemoServlet)VaadinServlet.getCurrent()).getBundleContext();
		ServiceReference ref = context.getServiceReference(GossService.class.getName());
		gossService = (GossService) context.getService(ref);
		if(gossService==null){
			log.warn("gossService should not be null!");
		}
		
		GridLayout content = new GridLayout(2,4);
		setContent(content);
		content.setSizeFull();
        content.addStyleName("view-content");
        content.setMargin(true);
        content.setSpacing(true);
        
        
        
        
        getPage().setTitle("Welcome to Demo 0.0001");
        {
        	Label label = new Label("Welcome to Security Demo 0.0001");
        	label.addStyleName("heading");
        	content.addComponent(label, 0, 0, 1, 0);
        }

        
      
        
        //text box poll frequency
        TextField pollFreq = new TextField("Poll Frequency (sec)", "5");
        //text box time shown
        TextField timeShown = new TextField("Time Shown (minutes)", "10");
        //text box start time 
        DateField startTime = new DateField("Start");
        VerticalLayout verticalInputs = new VerticalLayout();
        verticalInputs.addComponent(pollFreq);
        verticalInputs.addComponent(timeShown);
        verticalInputs.addComponent(startTime);
        content.addComponent(verticalInputs, 0, 1);
        
        //checkboxes
        OptionGroup pmus = new OptionGroup("PMUs");
        pmus.setMultiSelect(true);
        //utility1
        pmus.addItem("BE50");
        pmus.addItem("SYLM");
        pmus.addItem("MPLV");
        //utility2
        pmus.addItem("KEEL");
        pmus.addItem("CPJK");
        pmus.addItem("SUML");
        //utility3
        pmus.addItem("SLAT");
        pmus.addItem("SCE1");
        pmus.addItem("MCN2");
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
        
        
//        List<String> options = new ArrayList<String>();
//        options.add("Model 1");
//        options.add("Model 2");
//        options.add("Model 3");
//        ComboBox combo = new ComboBox("Pre-vetted Models", options);
//        content.addComponent(combo);
//        
//        Receiver modelReceiver = new Receiver() {
//			
//			public OutputStream receiveUpload(String filename, String mimeType) {
//				System.out.println("Recieved file "+filename+"  doing something with it");
//				//if mime = xml do something
//				//if text plain do something
//				//else complain
//				return null;
//			}
//		};
//        Upload modelUpload = new Upload("Upload new Model", modelReceiver);
//        content.addComponent(modelUpload);
//        
//        
//        
//        Button bSample = new Button("Go");
//        bSample.addStyleName("icon-ok");
//        content.addComponent(bSample);
//        content.setExpandRatio(bSample, 0);
//        bSample.addClickListener(new Button.ClickListener() {
//			
//			private static final long serialVersionUID = 1L;
//
//			public void buttonClick(ClickEvent event) {
//				//doExecute();
//				Notification.show("Wunderbar");
//			}
//		});
        
      
        
//        Panel panel = new Panel();
//        panel.addStyleName("layout-panel");
//        panel.setSizeFull();
//        content.addComponent(panel);
//        content.setExpandRatio(panel, 1);

//        panelContnent = new VerticalLayout();
//        // panelContnent.setWidth("100%");
//        panelContnent.setSizeUndefined();
//        panel.setContent(panelContnent);
        
		
	}

//	protected void doExecute() {
//		// load an service out of OSGi
//		BundleContext context = ((DataGenServlet)VaadinServlet.getCurrent()).getBundleContext();
//		
////		ServiceReference ref = context.getServiceReference(MyService.class.getName());
////		MyService service = (MyService) context.getService(ref);
//		
//		panelContnent.removeAllComponents();
//
//		{
//			// Use the service
//			Label label = new Label(  new Date().toString()  );
//			label.addStyleName("h1");
//			panelContnent.addComponent(label);
//		}
//		
//		// print all bundle names
//		for (Bundle bundle : context.getBundles()) {
//			Label label = new Label( bundle.getSymbolicName() + " : " + bundle.getVersion() );
//			if (bundle.getState() != Bundle.ACTIVE)
//				label.addStyleName("light");
//			panelContnent.addComponent(label);
//		}
//		
//		
//	}

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
					gossService.requestPMUData("01-01-2010 00:27:00", "01-04-2010 00:27:00", "5,6,7");
				
				
				
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
