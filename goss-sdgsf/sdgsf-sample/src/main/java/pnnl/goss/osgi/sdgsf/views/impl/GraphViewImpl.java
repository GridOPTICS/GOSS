package pnnl.goss.osgi.vaadin.views.impl;

import java.awt.Color;

import org.dussan.vaadin.dcharts.DCharts;

import pnnl.goss.osgi.vaadin.handlers.GraphHandler;
import pnnl.goss.osgi.vaadin.util.DemoConstants;
import pnnl.goss.osgi.vaadin.views.GraphView;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label; 
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class GraphViewImpl extends VerticalLayout implements GraphView{
	private GraphHandler handler;
	private DCharts chart;
	
	@Override
	public void setHandler(GraphHandler handler) {
		this.handler = handler;
	}
	
	public void init(){
		System.out.println("INIT GRAPH VIEW");
		Label success = new Label("Congratulations, you are special!");
		addComponent(success);
		
		//Stop poll for data button (disabled unless polling)
        Button stopPollingBtn = new Button("Stop Polling");
        stopPollingBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				//TODO stop polling and navigate back to options
				UI.getCurrent().getNavigator().navigateTo(DemoConstants.OPTIONS_VIEW);
			}
		});
        addComponent(stopPollingBtn);
		
        chartInit();
        addComponent(chart);
        //tell handler to start polling for data and updatinging graph
//        handler.pollForData();
        
	}
	
	
//	  /**
//     * Returns a sample dataset.
//     * 
//     * @return The dataset.
//     */
//    private static CategoryDataset createDataset() {
//
//        // row keys...
//        String y2009 = "2009";
//        String y2008 = "2008";
//        String y2007 = "2007";
//
//        // column keys...
//        String under5 = "< 5";
//        String between5_9 = "5-9";
//        String between10_14 = "10-14";
//        String between15_19 = "15-19";
//        String between20_24 = "20-24";
//
//        // create the dataset...
//        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//
//        dataset.addValue(21299656, y2009, under5);
//        dataset.addValue(20609634, y2009, between5_9);
//        dataset.addValue(19973564, y2009, between10_14);
//        dataset.addValue(21537837, y2009, between15_19);
//        dataset.addValue(21539559, y2009, between20_24);
//
//        dataset.addValue(21005852, y2008, under5);
//        dataset.addValue(20065249, y2008, between5_9);
//        dataset.addValue(20054627, y2008, between10_14);
//        dataset.addValue(21514358, y2008, between15_19);
//        dataset.addValue(21058981, y2008, between20_24);
//
//        dataset.addValue(20724125, y2007, under5);
//        dataset.addValue(19849628, y2007, between5_9);
//        dataset.addValue(20314309, y2007, between10_14);
//        dataset.addValue(21473690, y2007, between15_19);
//        dataset.addValue(21032396, y2007, between20_24);
//
//        return dataset;
//
//    }
	
	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		System.out.println("ENTER GRAPH VIEW");
		handler.pollForData();
		
	}

	@Override
	public void updateGraph(Object data) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void chartInit()
	{
		
		chart = new DCharts();

		chart.setEnableChartDataClickEvent(true);
		chart.setEnableChartDataRightClickEvent(true);
//		CategoryDataset dataset = new DefaultCategoryDataset();
//		chart = ChartFactory.createLineChart("PMU data", "Frequency", "Time", createDataset(), PlotOrientation.VERTICAL, true, true, false);
//		chart.setBackgroundPaint(Color.white);
		
	}

}
