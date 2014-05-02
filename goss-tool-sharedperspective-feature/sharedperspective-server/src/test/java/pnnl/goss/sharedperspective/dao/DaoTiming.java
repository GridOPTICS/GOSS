package pnnl.goss.sharedperspective.dao;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import javax.sql.DataSource;

import org.junit.Before;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.datamodel.PowergridTimingOptions;
import pnnl.goss.powergrid.server.PowergridContextService;
import pnnl.goss.sharedperspective.SharedPerspectiveServerActivator;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory;

public class DaoTiming {
	
	private static Logger log = LoggerFactory.getLogger(DaoTiming.class);
	private long startTime;
	private long stopTime;
	private PowergridTimingOptions mockOptions;
	private PowergridContextService mockPowergridContext;
	private DataSource mockDatasource;
	private BundleContext mockBundleContext;
	private ServiceReference mockServiceRef;
	private SharedPerspectiveServerActivator activator;
	
	public void initializeActivator(){
		String timestamp = "2013-08-01 00:10:00";
		mockOptions = mock(PowergridTimingOptions.class);
		mockPowergridContext = mock(PowergridContextService.class);
		mockDatasource = mock(DataSource.class);
		mockBundleContext = mock(BundleContext.class);
		mockServiceRef = mock(ServiceReference.class);
		
		when(mockOptions.getTimingOption()).thenReturn(PowergridTimingOptions.TIME_OPTION_STATIC);
		when(mockOptions.getTimingOptionArgument()).thenReturn(timestamp);
		when(mockPowergridContext.getPowergridTimingOptions()).thenReturn(mockOptions);
		when(mockBundleContext.getServiceReference(PowergridContextService.class.getName())).thenReturn(mockServiceRef);
		when(mockBundleContext.getService(mockServiceRef)).thenReturn(mockPowergridContext);
		
		activator = new SharedPerspectiveServerActivator();
		activator.setBundleContext(mockBundleContext);
		
	}
	
	private void stopAndLogTime(String message){
		stopTime = System.nanoTime();
		String msg = String.format("time for %s is %5.3f ms",  message, (stopTime-startTime) * 0.000001);
		System.out.println(msg);
	}
	
	private void startTime(){
		startTime = System.nanoTime();
	}

	public static void main(String[] args) {
		String timestamp = "2013-08-01 00:10:00";
		DaoTiming timing = new DaoTiming();
		// Initialize the activator that is dependent on the data.
		timing.initializeActivator();

		MysqlDataSource datasource = new MysqlDataSource();
		datasource.setUrl("jdbc:mysql://eioc-goss.pnl.gov:3306/north?user=root&password=goss!4evr");
				
		PowergridSharedPerspectiveDaoMySql mysqlDao = new PowergridSharedPerspectiveDaoMySql(datasource);
		timing.startTime();
		mysqlDao.getAvailablePowergrids();
		timing.stopAndLogTime("getAvailablePowergrids");
		
		
		try {
			timing.startTime();
			mysqlDao.getAvailablePowergrids();
			timing.stopAndLogTime("getAvailablePowergrids");
						
			timing.startTime();
			mysqlDao.getBranches(1); 
			timing.stopAndLogTime("getBranches");
			
			timing.startTime();
			mysqlDao.getBuses(1); 
			timing.stopAndLogTime("getBuses");
			
			timing.startTime();
			mysqlDao.getSubstations(1);
			timing.stopAndLogTime("getSubstations");
			
			timing.startTime();
			mysqlDao.getPowergridById(1);
			timing.stopAndLogTime("getPowergridById");
			
			timing.startTime();
			mysqlDao.getPowergridModel(1);
			timing.stopAndLogTime("getPowergridModel");
			
			timing.startTime();
			mysqlDao.getPowergridModelAtTime(1,timestamp);
			timing.stopAndLogTime("getPowergridModelAtTime");
			
			timing.startTime();
			mysqlDao.getAlertContext(1);
			timing.stopAndLogTime("getAlertContext");
			
			timing.startTime();
			mysqlDao.getPowergridModelAtTime(1, timestamp);
			timing.stopAndLogTime("getPowergridModelAtTime");
			
			timing.startTime();
			mysqlDao.getACLineSegments(1, timestamp);
			timing.stopAndLogTime("getACLineSegments");
			
			timing.startTime();
			mysqlDao.getACLineSegmentsUpdate(1, timestamp);
			timing.stopAndLogTime("getACLineSegmentsUpdate");
			
			Timestamp ts = mysqlDao.convertTimestepToTimestamp(timestamp, 5, 0);
			timing.startTime();
			mysqlDao.getContingencyResults(ts);
			timing.stopAndLogTime("getContingencyResults");
			
			timing.startTime();
			mysqlDao.getTopology("Greek-118-South", timestamp);
			timing.stopAndLogTime("getTopology");
			
			timing.startTime();
			mysqlDao.getTopologyUpdate("Greek-118-South", timestamp);
			timing.stopAndLogTime("getTopologyUpdate");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
		
		
		
	}

}
