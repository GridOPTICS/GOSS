package pnnl.goss.sharedperspective.dao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import pnnl.goss.powergrid.datamodel.PowergridTimingOptions;
import pnnl.goss.powergrid.server.PowergridContextService;
import pnnl.goss.powergrid.server.datasources.PowergridDataSources;
import pnnl.goss.sharedperspective.SharedPerspectiveServerActivator;

public class TestPowergridsharedPerspectiveDao{

	private PowergridTimingOptions mockOptions;
	private PowergridContextService mockPowergridContext;
	private DataSource mockDatasource;
	private BundleContext mockBundleContext;
	private ServiceReference mockServiceRef;
	@Before
	public void setup(){
		
		mockOptions = mock(PowergridTimingOptions.class);
		mockPowergridContext = mock(PowergridContextService.class);
		mockDatasource = mock(DataSource.class);
		mockBundleContext = mock(BundleContext.class);
		mockServiceRef = mock(ServiceReference.class);
		
		when(mockPowergridContext.getPowergridTimingOptions()).thenReturn(mockOptions);
		when(mockBundleContext.getServiceReference(PowergridContextService.class.getName())).thenReturn(mockServiceRef);
		when(mockBundleContext.getService(mockServiceRef)).thenReturn(mockPowergridContext);
	}
	
	/*
	public void testPowergridSharedPerspectiveDaoMySql() {
		fail("Not yet implemented");
	}

	public void testGetTopologyString() {
		fail("Not yet implemented");
	}

	public void testGetTopologyStringString() {
		fail("Not yet implemented");
	}

	public void testGetTopologyUpdate() {
		fail("Not yet implemented");
	}

	public void testGetPowergridId() {
		fail("Not yet implemented");
	}

	public void testGetRegion() {
		fail("Not yet implemented");
	}

	public void testGetSubstationListTimestamp() {
		fail("Not yet implemented");
	}

	public void testGetSubstationListIntString() {
		fail("Not yet implemented");
	}

	public void testGetACLineSegments() {
		fail("Not yet implemented");
	}

	public void testGetACLineSegmentsUpdate() {
		fail("Not yet implemented");
	}

	public void testGetLineLoad() {
		fail("Not yet implemented");
	}

	public void testGetLineLoadTest() {
		fail("Not yet implemented");
	}

	public void testGetContingencyResults() {
		fail("Not yet implemented");
	}
	*/
	
	@Test
	public void testConvertTimestepToTimestampAsOffsetOption() {
		String passedDate = "2013-08-01 05:00:00";
		String expectedDate = "2013-08-01 03:00:00";
		// - 2 hours from date
		String offset = "-02:00";
		// return the static option for this step.
		when(mockOptions.getTimingOption()).thenReturn(PowergridTimingOptions.TIME_OPTION_OFFSET);
		when(mockOptions.getTimingOptionArgument()).thenReturn(offset);
				
		SharedPerspectiveServerActivator activator = new SharedPerspectiveServerActivator();
		activator.setBundleContext(mockBundleContext);
				
		PowergridSharedPerspectiveDao dao = new PowergridSharedPerspectiveDaoMySql(mockDatasource);
		
		try {
			Timestamp expectedTsTimestamp = Timestamp.valueOf(expectedDate); //new Timestamp(expectedDate.getTime());
			//Date expectedDate = format.parse(passedDate);
			Timestamp ts = dao.convertTimestepToTimestamp(passedDate, 0, 3);
			
			assertEquals(expectedTsTimestamp,  ts);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testConvertTimestepToTimestampAsOffsetOptionAddition() {
		String passedDate = "2013-08-01 23:00:00";
		String expectedDate = "2013-08-01 01:00:00";
		// - 2 hours from date
		String offset = "02:00";
		// return the static option for this step.
		when(mockOptions.getTimingOption()).thenReturn(PowergridTimingOptions.TIME_OPTION_OFFSET);
		when(mockOptions.getTimingOptionArgument()).thenReturn(offset);
				
		SharedPerspectiveServerActivator activator = new SharedPerspectiveServerActivator();
		activator.setBundleContext(mockBundleContext);
				
		PowergridSharedPerspectiveDao dao = new PowergridSharedPerspectiveDaoMySql(mockDatasource);
		
		try {
			Timestamp expectedTsTimestamp = Timestamp.valueOf(expectedDate); //new Timestamp(expectedDate.getTime());
			//Date expectedDate = format.parse(passedDate);
			Timestamp ts = dao.convertTimestepToTimestamp(passedDate, 0, 3);
			
			assertEquals(expectedTsTimestamp,  ts);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test
	public void testConvertTimestepToTimestampAsStaticOption() {
		String passedDate = "2013-08-01 05:00:00";
		
		// return the static option for this step.
		when(mockOptions.getTimingOption()).thenReturn(PowergridTimingOptions.TIME_OPTION_STATIC);
		when(mockOptions.getTimingOptionArgument()).thenReturn(passedDate);
				
		SharedPerspectiveServerActivator activator = new SharedPerspectiveServerActivator();
		activator.setBundleContext(mockBundleContext);
				
		PowergridSharedPerspectiveDao dao = new PowergridSharedPerspectiveDaoMySql(mockDatasource);
		
		try {
			Timestamp expectedTsTimestamp = Timestamp.valueOf(passedDate); //new Timestamp(expectedDate.getTime());
			//Date expectedDate = format.parse(passedDate);
			Timestamp ts = dao.convertTimestepToTimestamp("2010-01-30 00:01:01", 0, 3);
			
			assertEquals(expectedTsTimestamp,  ts);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

//	public void testGetPowergridModelAtTimeIntString() {
//		fail("Not yet implemented");
//	}

}
