/*
	Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
 */
package pnnl.goss.sharedperspective.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.powergrid.datamodel.PowergridTimingOptions;
import pnnl.goss.powergrid.server.PowergridContextService;
import pnnl.goss.sharedperspective.SharedPerspectiveServerActivator;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegment;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegmentTest;
import pnnl.goss.sharedperspective.common.datamodel.ContingencyResult;
import pnnl.goss.sharedperspective.common.datamodel.ContingencyResultList;
import pnnl.goss.sharedperspective.common.datamodel.Location;
import pnnl.goss.sharedperspective.common.datamodel.Region;
import pnnl.goss.sharedperspective.common.datamodel.Substation;
import pnnl.goss.sharedperspective.common.datamodel.Topology;

import com.mysql.jdbc.PreparedStatement;

public class PowergridSharedPerspectiveDaoMySql extends PowergridDaoMySql implements PowergridSharedPerspectiveDao {

	private static Logger log = LoggerFactory.getLogger(PowergridSharedPerspectiveDaoMySql.class);

	public PowergridSharedPerspectiveDaoMySql(DataSource datasource) {
		super(datasource);
		log.debug("Creating " + PowergridSharedPerspectiveDaoMySql.class);
	}

	@Override
	public Topology getTopology(String powergridName) throws Exception {
		return getTopology(powergridName, null);
	}

	public Topology getTopology(String powergridName, String timestamp) throws Exception {
		Topology topology = new Topology();
		int powergridId = getPowergridId(powergridName);
		Region region = getRegion(powergridId);
		region.setSubstations(getSubstationList(powergridId, timestamp));
		topology.setRegion(region);
		topology.setAcLineSegments(getACLineSegments(powergridId, timestamp));
		return topology;
	}

	@Override
	public Topology getTopologyUpdate(String powergridName, String timestampStr) throws Exception {
		Topology topology = new Topology();
		int powergridId = getPowergridId(powergridName);
		Region region = getRegion(powergridId);
		topology.setRegion(region);
		topology.setAcLineSegments(getACLineSegmentsUpdate(powergridId, timestampStr));
		return topology;
	}

	@Override
	public int getPowergridId(String powergridName) throws Exception {
		Connection connection = null;
		int powergridId = 0;
		try {
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();
			String queryString = "select powergridid from powergrids where name = '" + powergridName + "'";
			ResultSet rs = stmt.executeQuery(queryString);
			if (rs.next()) {
				powergridId = rs.getInt("powergridid");
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			if (connection != null)
				connection.close();
			throw e;
		} finally {
			if (connection != null)
				connection.close();
		}
		return powergridId;
	}

	@Override
	public Region getRegion(int powergridId) throws Exception {
		Connection connection = null;
		Region region = null;
		try {
			connection = datasource.getConnection();
			log.debug(connection.toString());
			Statement stmt = connection.createStatement();
			String dbQuery = "select a.mrid, a.areaName, p.name from areas a, powergrids p where a.powergridid = p.powergridid and a.powergridid = " + powergridId;
			log.debug(dbQuery);
			ResultSet rs = stmt.executeQuery(dbQuery);
			rs.next();
			region = new Region();
			region.setName(rs.getString("areaname"));
			region.setMrid(rs.getString("mrid"));
			region.setOrganization(rs.getString("name"));
		} catch (Exception e) {
			log.error(e.getMessage());
			if (connection != null)
				connection.close();
			throw e;
		}
		return region;
	}

	/**
	 * Get a list of substations using passed timestamp. If the timestamp is
	 * null then the return value will be the initial value associated with the
	 * simulated day at the current time.
	 * 
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	public List<Substation> getSubstationList(Timestamp timestamp) throws Exception {

		Connection connection = null;
		List<Substation> substations = new ArrayList<Substation>();

		try {
			connection = datasource.getConnection();
			PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("{call proc_GetSubstations(?)}");
			stmt.setTimestamp(1, timestamp);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				// Populate a Substation object.
				Substation s = new Substation();

				s.setName(rs.getString("substationname"));

				s.setMrid(rs.getString("substationmrid"));
				s.setName(rs.getString("substationname"));

				// Area and region are synonomous currently
				s.setRegionMRID(rs.getString("areamrid"));
				s.setRegionName(rs.getString("areaname"));

				// New fields for shared perspective.
				s.setTotalPGen(rs.getDouble("sumpgen"));
				s.setTotalQGen(rs.getDouble("sumqgen"));
				s.setTotalPLoad(rs.getDouble("sumpload"));
				s.setTotalQLoad(rs.getDouble("sumqload"));
				s.setTotalMaxMva(rs.getDouble("totalmaxmvar"));

				Location location = new Location();
				location.setLatitude(rs.getDouble("latitude"));
				location.setLongitude(rs.getDouble("longitude"));
				s.setLocation(location);

				substations.add(s);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			connection.close();
			throw e;
		}
		return substations;

	}

	public List<Substation> getSubstationList(int powergridId, String timestampStr) throws Exception {

		Timestamp timestamp = convertTo3SecondTimestamp(timestampStr);
		return getSubstationList(timestamp);
	}

	/**
	 * Conv
	 * @param timestampStr
	 * @return
	 * @throws ParseException
	 */
	private Timestamp convertTo3SecondTimestamp(String timestampStr) throws ParseException {
		return convertTimestepToTimestamp(timestampStr, 0, 3);
	}

	@Override
	public List<ACLineSegment> getACLineSegments(int powergridId, String timestampStr) throws Exception {
		Timestamp timestamp = convertTo3SecondTimestamp(timestampStr);
		return getACLineSegments(powergridId, timestamp, getMridSubstationMap(timestamp));
	}

	@Override
	public List<ACLineSegment> getACLineSegmentsUpdate(int powergridId, String timestampStr) throws Exception {
		Timestamp timestamp = convertTo3SecondTimestamp(timestampStr);
		return getACLineSegmentsUpdate(powergridId, timestamp, getMridSubstationMap(timestamp));
	}

	private HashMap<String, Substation> getMridSubstationMap(Timestamp timestamp) throws Exception {
		List<Substation> substations = getSubstationList(timestamp);
		// A map from mrid to substation.
		HashMap<String, Substation> substationMap = new HashMap<String, Substation>();
		for (Substation s : substations) {
			substationMap.put(s.getMrid(), s);
		}
		return substationMap;
	}

	private List<ACLineSegment> getACLineSegments(int powergridId, Timestamp timestamp, HashMap<String, Substation> substationMap) throws Exception {
		Connection connection = null;
		List<ACLineSegment> acLineSegments = null;

		try {
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();

			log.debug("query: proc_GetAcLineSegments(" + timestamp + ")");
			// proc_GetAcLineSegments requires a timestamp parameter.
			PreparedStatement prepStmt = (PreparedStatement) connection.prepareStatement("{call proc_GetAcLineSegments(?)}");
			prepStmt.setTimestamp(1, timestamp);
			ResultSet rs = prepStmt.executeQuery();

			// Create list of segments.
			acLineSegments = new ArrayList<ACLineSegment>();
			ACLineSegment acLineSegment;

			while (rs.next()) {
				String acLineName = "";
				acLineSegment = new ACLineSegment();
				acLineSegment.setMrid(rs.getString("branchmrid")); // Branch's
																	// Mrid
				acLineSegment.setKvlevel(rs.getDouble("basekv")); // Base KV
																	// from
																	// buses
				acLineSegment.setRating(rs.getDouble("rating")); // branch
				acLineSegment.setStatus(rs.getInt("status")); // line timestep
				double mvaFlow = Math.sqrt((rs.getDouble("p") * rs.getDouble("p")) + (rs.getDouble("q") * rs.getDouble("q")));
				if (rs.getDouble("p") < 0)
					mvaFlow = -mvaFlow;
				acLineSegment.setMvaFlow(mvaFlow); // sqrt(P^2+Q^2), if P is +
													// then positive , if Q is -
													// then negative value.

				String fromSubMrid = rs.getString("fromsubstationmrid");
				String toSubMrid = rs.getString("tosubstationmrid");

				List<Substation> substationList = new ArrayList<Substation>();

				substationList.add(substationMap.get(fromSubMrid));
				substationList.add(substationMap.get(toSubMrid));

				int subNumber = 0;
				// Create the acLineSegmentName and look for invalid line
				// mappings.
				for (Substation s : substationList) {
					if (s == null) {
						log.error("Invalid substation reference at linesegment " + acLineSegment.getMrid());
					} else {
						if (subNumber == 0) {
							acLineName = s.getName();
						} else {
							// Make sure the name isn't null in ordere to
							// append.
							if (acLineName != null) {
								acLineName += "_" + s.getName();
							} else {
								acLineName = "_" + s.getName();
							}
						}
					}
				}

				acLineSegment.setName(acLineName); // SubstationFromName_SubstationToName
													// from branch
				acLineSegment.setSubstations(substationList);

				acLineSegments.add(acLineSegment);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			if (connection != null)
				connection.close();
			throw e;
		}

		return acLineSegments;
	}

	private List<ACLineSegment> getACLineSegmentsUpdate(int powergridId, Timestamp timestamp, HashMap<String, Substation> substationMap) throws Exception {
		List<ACLineSegment> acLineSegments = new ArrayList<ACLineSegment>();

		// Line segments for the original timestamp passed.
		List<ACLineSegment> ls1 = getACLineSegments(powergridId, timestamp, substationMap);

		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(new java.util.Date());
		cal1.set(2013, 7, 1);
		cal1.set(Calendar.SECOND, cal1.get(Calendar.SECOND) - cal1.get(Calendar.SECOND) % 3);
		cal1.set(Calendar.MILLISECOND, 0);

		Timestamp currentTimestamp = new Timestamp(cal1.getTime().getTime());
		HashMap<String, Substation> currentSubstationMap = getMridSubstationMap(currentTimestamp);
		// Line segments for the original timestamp passed.
		List<ACLineSegment> currentACLineSegments = getACLineSegments(powergridId, currentTimestamp, currentSubstationMap);

		if (currentACLineSegments.size() != ls1.size()) {
			throw new Exception("The aclinesegments must have the same number of elements in them in order to compare them.");
		}

		for (int i = 0; i < currentACLineSegments.size(); i++) {
			ACLineSegment orig = ls1.get(i);
			ACLineSegment current = currentACLineSegments.get(i);
			// Sanity check to make sure we aren't comparing two different line
			// segment's data.
			if (!orig.getName().equals(current.getName())) {
				throw new Exception("Ordering of aclinesegments is probably messed up!");
			}
			if (!(orig.getStatus() == current.getStatus() && Math.abs(orig.getMvaFlow() - current.getMvaFlow()) < 0.00001)) {

				acLineSegments.add(current);
			}
		}

		return acLineSegments;
	}

	@Override
	public Topology getLineLoad(int powergridId, String timestamp) throws Exception {

		Connection connection = null;
		List<ACLineSegment> acLineSegments = null;
		Topology topology = new Topology();
		try {
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();

			ResultSet rs = null;

			String dbQuery = "";

			Timestamp timestamp_;
			if (timestamp == null) {
				// Get current time -> set date to 2013-08-01 -> make sure that
				// second value is multiple of 3
				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date());
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				timestamp_ = new Timestamp(cal.getTime().getTime());
			} else {
				Calendar cal = Calendar.getInstance();
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
				java.util.Date parsedDate = sdf.parse(timestamp);
				timestamp_ = new Timestamp(parsedDate.getTime());
			}

			dbQuery = "select mbr.mrid, lt.p, lt.q " + "from mridbranches mbr, branches br, buses bu, linetimesteps lt, lines_ l " + "where br.branchid = mbr.branchid " + "and br.frombusnumber = bu.busnumber " + "and l.lineid = lt.lineid " + "and l.branchid = br.branchid " + "and mbr.powergridid = br.powergridid " + "and bu.powergridid = br.powergridid " + "and lt.powergridid = br.powergridid " + "and l.powergridid = br.powergridid " + "and br.powergridid = " + powergridId + " " + "and lt.timestep ='" + timestamp_ + "'";

			log.debug(dbQuery);

			rs = stmt.executeQuery(dbQuery);
			acLineSegments = new ArrayList<ACLineSegment>();
			ACLineSegment acLineSegment;
			while (rs.next()) {
				acLineSegment = new ACLineSegment();
				acLineSegment.setMrid(rs.getString("mrid")); // Branch's Mrid
				// acLineSegment.setKvlevel(rs.getDouble("basekv")); //Base KV
				// from buses
				// acLineSegment.setRating(rs.getDouble("rating")); //branch
				// acLineSegment.setStatus(rs.getInt("status")); //line timestep
				double mvaFlow = Math.sqrt((rs.getDouble("p") * rs.getDouble("p")) + (rs.getDouble("q") * rs.getDouble("q")));
				if (rs.getDouble("p") < 0)
					mvaFlow = -mvaFlow;
				acLineSegment.setMvaFlow(mvaFlow);
				acLineSegments.add(acLineSegment);
			}

			topology.setAcLineSegments(acLineSegments);

		} catch (Exception e) {
			log.error(e.getMessage());
			if (connection != null)
				connection.close();
			throw e;
		}

		if (connection != null)
			connection.close();

		return topology;

	}

	@Override
	public ACLineSegmentTest getLineLoadTest(int powergridId, String timestamp, int lineId) throws Exception {

		Connection connection = null;
		ACLineSegmentTest acLineSegment = null;
		ResultSet rs = null;

		try {
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();

			String dbQuery = "";

			Timestamp timestamp_;
			if (timestamp == null) {
				// Get current time -> set date to 2013-08-01 -> make sure that
				// second value is multiple of 3
				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date());
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				timestamp_ = new Timestamp(cal.getTime().getTime());
			} else {
				Calendar cal = Calendar.getInstance();
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
				java.util.Date parsedDate = sdf.parse(timestamp);
				timestamp_ = new Timestamp(parsedDate.getTime());
			}

			dbQuery = "select mbr.mrid, lt.p, lt.q,  bu.BaseKV, br.Rating, br.Status " + "from mridbranches mbr, branches br, buses bu, linetimesteps lt, lines_ l " + "where br.branchid = mbr.branchid " + "and br.frombusnumber = bu.busnumber " + "and l.lineid = " + lineId + " " + "and l.branchid = br.branchid " + "and mbr.powergridid = br.powergridid " + "and bu.powergridid = br.powergridid " + "and lt.powergridid = br.powergridid " + "and l.powergridid = br.powergridid " + "and br.powergridid = " + powergridId + " " + "and lt.timestep ='" + timestamp_ + "'";

			log.debug(dbQuery);

			rs = stmt.executeQuery(dbQuery);

			if (rs.next()) {
				acLineSegment = new ACLineSegmentTest();
				// acLineSegment.setMrid(rs.getString("mrid")); //Branch's Mrid
				acLineSegment.setKvlevel(rs.getDouble("BaseKV")); // Base KV
																	// from
																	// buses
				acLineSegment.setRating(rs.getDouble("Rating")); // branch
				acLineSegment.setStatus(rs.getInt("Status")); // line timestep
				double mvaFlow = Math.sqrt((rs.getDouble("p") * rs.getDouble("p")) + (rs.getDouble("q") * rs.getDouble("q")));
				if (rs.getDouble("p") < 0)
					mvaFlow = -mvaFlow;
				acLineSegment.setMvaFlow(mvaFlow);
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			if (connection != null)
				connection.close();
			throw e;
		} finally {
			if (rs != null)
				rs.close();
			if (connection != null)
				connection.close();
		}

		return acLineSegment;

	}

	@Override
	public ContingencyResultList getContingencyResults(Timestamp timestamp) throws Exception {

		Connection connection = null;

		// The list that is going to be sent back through the DataResponse
		// object.
		ContingencyResultList resultList = new ContingencyResultList();

		/*
		 * The stored procedure to return the data for a cim branch violation.
		 * This data will return all branch violations at a specific timestep
		 * for all of the contingencies.
		 */
		String PROC_CTG_BR_VIO_AT_TS = "proc_GetContingencyBranchViolationsAtTimestepCim";

		// A stored procedure for getting out of service branches for a specific
		// contingency.
		String PROC_CTG_BR = "proc_GetContingencyOutOfServiceBranchesCim";

		try {

			connection = datasource.getConnection();
			// Prepare to call the stored procedure.
			CallableStatement proc = connection.prepareCall("{ CALL " + PROC_CTG_BR_VIO_AT_TS + "(?) }");
			// Set the value of the parameter.
			proc.setTimestamp(1, timestamp);
			// Execute the procedure on the database.
			ResultSet ctgResultSet = proc.executeQuery();
			int contingencyId = 0;
			ContingencyResult result = null;

			while (ctgResultSet.next()) {

				if (ctgResultSet.getInt("contingencyid") != contingencyId) {
					if (result != null) {
						resultList.addResultList(result);
						result = null;
					}
					contingencyId = ctgResultSet.getInt("contingencyid");
					result = new ContingencyResult();
					result.setContingencyId(contingencyId);
					result.setContingencyName(ctgResultSet.getString("name"));
					result.setTimestamp(timestamp.toString());

					// Prepare to call the stored procedure.
					CallableStatement procBranch = connection.prepareCall("{ CALL " + PROC_CTG_BR + "(?) }");

					// Pass the contingencyId
					procBranch.setInt(1, contingencyId);

					// Execute the stored procedure.
					ResultSet brResultSet = procBranch.executeQuery();

					// Loop over branches add them to the out of service
					// category.
					while (brResultSet.next()) {
						result.addOutOfServiceACLineSegments(brResultSet.getString("mrid"));
					}
					brResultSet.close();
					brResultSet = null;
				}

				result.addViolationACLineSegments_Value(ctgResultSet.getString("mrid"), ctgResultSet.getDouble("value"));

			}

			if (result != null) {
				resultList.addResultList(result);
			}
			ctgResultSet.close();

		} catch (Exception e) {
			log.error(e.getMessage());
			if (connection != null)
				connection.close();
			throw e;
		} finally {
			if (connection != null)
				connection.close();
		}
		return resultList;
	}

	@Override
	public Timestamp convertTimestepToTimestamp(String timestampAsString, int minuteInterval, int secondInterval) throws ParseException {
		Timestamp timestamp;
		log.debug("converting timestamp: " + timestampAsString);

		PowergridContextService serviceContext = SharedPerspectiveServerActivator.getPowergridContextService();
		PowergridTimingOptions timeOptions = null;
		int hoursOffset = 0;
		int minOffset = 0;

		if (serviceContext != null) {
			timeOptions = serviceContext.getPowergridTimingOptions();

			if (timeOptions != null) {
				if (timeOptions.getTimingOption().equals(PowergridTimingOptions.TIME_OPTION_STATIC)) {
					timestampAsString = timeOptions.getTimingOptionArgument();
				} else if (timeOptions.getTimingOption().equals(PowergridTimingOptions.TIME_OPTION_OFFSET)) {
					String[] hourMinSplit = timeOptions.getTimingOptionArgument().split(":");
					hoursOffset = Integer.parseInt(hourMinSplit[0]);
					minOffset = Integer.parseInt(hourMinSplit[1]);
					// TODO Do some subtraction/addition of timestring.
				}
			} else {
				log.debug("timeOptions is null");
			}
		} else {
			log.debug("serviceContext is null");
		}

		if (timestampAsString == null) {
			// Get current time -> set date to 2013-08-01 -> make sure that
			// second value is multiple of 3
			Calendar cal = Calendar.getInstance();
			cal.setTime(new java.util.Date());
			// Do the hour min seconds first so that the offsets will wrap up and then
			// the hard coded dates can be set last.
			cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + minOffset);
			cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + hoursOffset);
			cal.set(2013, 7, 1);
			
			timestamp = new Timestamp(cal.getTime().getTime());
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
			java.util.Date parsedDate = sdf.parse(timestampAsString);
			timestamp = new Timestamp(parsedDate.getTime());
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(timestamp.getTime()));
			// Do the hour min seconds first so that the offsets will wrap up and then
			// the hard coded dates can be set last.
			cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + minOffset);
			cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + hoursOffset);
			cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(2013, 7, 1);
			
			timestamp = new Timestamp(cal.getTime().getTime());
		}

		return timestamp;
	}

	@Override
	public PowergridModel getPowergridModelAtTime(int powergridId, String timestep) {

		PowergridModel model = null;
		try {
			Timestamp timestamp = convertTo3SecondTimestamp(timestep);
			model = getPowergridModelAtTime(powergridId, timestamp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return model;
	}

}
