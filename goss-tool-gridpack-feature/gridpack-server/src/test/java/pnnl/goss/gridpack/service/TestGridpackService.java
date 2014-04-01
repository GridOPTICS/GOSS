package pnnl.goss.gridpack.service;

import java.util.Collection;

import pnnl.goss.gridpack.common.datamodel.GridpackBus;
import pnnl.goss.gridpack.common.datamodel.GridpackPowergrid;

public class TestGridpackService {

	public static void main(String[] args) {
		GridpackServiceImpl service = new GridpackServiceImpl();
		
		//GridpackBuses buses = service.getBuses("Greek-118");
		//Collection<GridpackBus> buses = service.getBuses("Greek-118");
		Object pg = service.getGridpackGrid("Greek-118");
		
		if (pg instanceof GridpackPowergrid)
		{
			Collection<GridpackBus> buses = ((GridpackPowergrid)pg).getBuses(); 
			
			for (GridpackBus bus:buses){
				System.out.println(bus.getBusName());
				if (bus.getLoads() != null && bus.getLoads().size() > 0){
					System.out.println(String.format("Bus %d has %d Loads", bus.getBusNumber(), bus.getLoads().size()));
				}
				
				if (bus.getShunts() != null && bus.getShunts().size() > 0){
					System.out.println(String.format("Bus %d has %d Shunts", bus.getBusNumber(), bus.getShunts().size()));
				}
				
				if (bus.getGenerators() != null && bus.getGenerators().size() > 0){
					System.out.println(String.format("Bus %d has %d Generators", bus.getBusNumber(), bus.getGenerators().size()));
				}
			}
		}
	}

}
