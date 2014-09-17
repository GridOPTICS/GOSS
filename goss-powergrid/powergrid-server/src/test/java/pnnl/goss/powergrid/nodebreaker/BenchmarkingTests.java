package pnnl.goss.powergrid.nodebreaker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.nodebreaker.Breaker;
import pnnl.goss.powergrid.topology.nodebreaker.Terminal;
import pnnl.goss.topology.nodebreaker.dao.BreakerDao;

public class BenchmarkingTests {

	public static Result runInsertionTest(String persistUnit, int inserts, int threadCount){
		Result result = new Result();
		
		BreakerDao dao = new BreakerDao(persistUnit);
		System.out.println("Start write test: "+persistUnit);
		result.start();
		for(int i=0; i< inserts; i++){
			UUID uuid = UUID.randomUUID();
			List<Terminal> terminals = new ArrayList<Terminal>();
			for(int j=0; j<10; j++){
				Terminal t = new Terminal();
				UUID uuid2 = UUID.randomUUID();
//				t.setMrid(uuid2.toString());
//				t.setIdentAlias("junky "+j);
				terminals.add(t);
			}
			
			IdentifiedObject identifier = new IdentifiedObject();
			identifier.setMrid(uuid.toString());
			IdentifiedObject id = new IdentifiedObject();
			identifier.setAlias("What a name");
			identifier.setName("This is data");
			identifier.setPath("A path is here!");
			
//			Breaker b = new Breaker();
//			//b.setIdentifiedObject(identifier);
//
//			b.setRatedCurrent(50.595);
//			b.setSwitchNormalOpen(true);
//			//b.setIdentPathName((null);
//			b.setRatedCurrent(50.5);
//			
//			dao.persist(b);
//			result.addItem(uuid);
//			
//			Breaker b2 = (Breaker) dao.get(Breaker.class, identifier.getMrid());
//			//assert b2.getMrid() != null;
//			assert b2.getMrid() != null;
		//	assert b2.getTerminals().size() == 10;
		}
		result.stop();
		System.out.println("End write test: "+persistUnit);
		
		return result;
	}
	
//	public static Result seekResultTest(Result writeResult, String persistUnit,int retrieves){
//		Result result = new Result();
//		
//		BreakerDao dao = new BreakerDao(persistUnit);
//		System.out.println("Start read test: "+persistUnit);
//		result.start();
//		for(int i=0; i< retrieves; i++){
//			Breaker b = (Breaker) dao.get(Breaker.class, writeResult.getUuidString(i));
//			assert b.getMrid() != null;
//		}
//		result.stop();
//		System.out.println("End read test: "+persistUnit);
//		return result;
//	}
	
	public static void main(String[] args) {
		int runTimes = 1000;
		String[] persistUnits = {"nodebreaker_cass_pu"};
		
				//"mysql-pu"}; 
			//"nodebreaker_cass_thrift_pu"}; //,
//			};
//		
		for(int i=0; i< persistUnits.length; i++){
			String unit = persistUnits[i];
			
			Result result = runInsertionTest(unit, runTimes, 0);
			double resultSeconds = (double)result.getDiff() / 1000000000.0;
			System.out.println(unit + " Write test on "+runTimes+ ": "+resultSeconds);
			
//			result = seekResultTest(result, unit, runTimes);
//			double resultSecondReads = (double)result.getDiff() / 1000000000.0;
//			System.out.println(unit + " Read test on "+runTimes+ ": "+resultSecondReads+"\n\n");
		}

	}
	


}
