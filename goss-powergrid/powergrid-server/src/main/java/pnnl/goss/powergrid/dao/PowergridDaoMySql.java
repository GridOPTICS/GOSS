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
package pnnl.goss.powergrid.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.datamodel.AlertContext;
import pnnl.goss.powergrid.datamodel.AlertContextItem;
import pnnl.goss.powergrid.datamodel.AlertSeverity;
import pnnl.goss.powergrid.datamodel.AlertType;
import pnnl.goss.powergrid.datamodel.Area;
import pnnl.goss.powergrid.datamodel.Branch;
import pnnl.goss.powergrid.datamodel.Bus;
import pnnl.goss.powergrid.datamodel.Line;
import pnnl.goss.powergrid.datamodel.Load;
import pnnl.goss.powergrid.datamodel.Machine;
import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.powergrid.datamodel.PowergridTimingOptions;
import pnnl.goss.powergrid.datamodel.Substation;
import pnnl.goss.powergrid.datamodel.SwitchedShunt;
import pnnl.goss.powergrid.datamodel.Transformer;
import pnnl.goss.powergrid.datamodel.Zone;
import pnnl.goss.server.core.InvalidDatasourceException;

public class PowergridDaoMySql implements PowergridDao {

	private static Logger log = LoggerFactory.getLogger(PowergridDaoMySql.class);
	protected DataSource datasource;
	private final AlertContext alertContext;
	private PowergridTimingOptions powergridTimingOptions;

	public PowergridDaoMySql() {
		log.debug("Creating " + PowergridDaoMySql.class);
		alertContext = new AlertContext();
		initializeAlertContext();
		
	}
	
	public PowergridDaoMySql(DataSource datasource) {
		log.debug("Creating " + PowergridDaoMySql.class);
		this.datasource = datasource;
		alertContext = new AlertContext();
		initializeAlertContext();
	}
	
	public AlertContext getAlertContext(int powergridId){
		return alertContext;
	}
	
	public void setPowergridTimingOptions(PowergridTimingOptions timingOptions){
		this.powergridTimingOptions = timingOptions;
	}
	
	public PowergridTimingOptions getPowergridTimingOptions(){
		return this.powergridTimingOptions;
	}
	
	private void initializeAlertContext(){
		
		alertContext.addContextElement(new AlertContextItem(AlertSeverity.SEVERITY_HIGH, AlertType.ALERTTYPE_BRANCH, 95.5, "mvar"));
		alertContext.addContextElement(new AlertContextItem(AlertSeverity.SEVERITY_WARN, AlertType.ALERTTYPE_BRANCH, 90.0, "mvar"));
		
		alertContext.addContextElement(new AlertContextItem(AlertSeverity.SEVERITY_HIGH, AlertType.ALERTTYPE_SUBSTATION, 0.1, "+- % nominal buses"));
		alertContext.addContextElement(new AlertContextItem(AlertSeverity.SEVERITY_WARN, AlertType.ALERTTYPE_SUBSTATION, 0.05, "+- % nominal buses"));
	}
	
	public void setDatasource(DataSource datasource){
		log.debug("Setting new datasource");
		this.datasource = datasource;
	}

	public List<Powergrid> getAvailablePowergrids() {
		List<Powergrid> grids = new ArrayList<Powergrid>();

		String dbQuery = "select pg.powergridId, pg.Name, a.mrid from powergrids pg inner join areas a on pg.Powergridid=a.PowergridId";
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = datasource.getConnection();
			Statement stmt = datasource.getConnection().createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				Powergrid item = new Powergrid();
				item.setPowergridId(rs.getInt(1));
				item.setName(rs.getString(2));
				item.setMrid(rs.getString("mrid"));
				grids.add(item);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return grids;
	}

	public List<String> getPowergridNames() {
		List<Powergrid> grids = getAvailablePowergrids();
		List<String> names = new ArrayList<String>();
		for (Powergrid g : grids) {
			names.add(g.getName());
		}
		return names;
	}

	public Powergrid getPowergridById(int powergridId) {
		String dbQuery = "select pg.PowergridId, pg.Name, a.mrid from powergrids pg INNER JOIN areas a ON pg.PowergridId=a.PowergridId where pg.PowergridId = " + powergridId;
		Powergrid grid = new Powergrid();
		ResultSet rs = null;
		Connection conn = null;

		try {
			log.debug(dbQuery);
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());
			rs.next();
			grid.setPowergridId(rs.getInt(1));
			grid.setName(rs.getString(2));
			grid.setMrid(rs.getString("mrid"));
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return grid;
	}

	public Powergrid getPowergridByName(String powergridName) {
		String dbQuery = "select * from powergrids where name = '" + powergridName + "'";
		Powergrid grid = new Powergrid();
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());
			rs.next();
			grid.setPowergridId(rs.getInt(1));
			grid.setName(rs.getString(2));
			grid.setMrid(rs.getString("mrid"));
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return grid;
	}

	public PowergridModel getPowergridModelAtTime(int powergridId, Timestamp timestep) {
		PowergridModel model = getPowergridModel(powergridId);
		updateModelToTimestep(model, timestep);
		return model;
	}

	/**
	 * Constructs a powergrid model out of the mysql database. The caller can
	 * then use the powergrid model passed back as it's datasource.
	 */
	public PowergridModel getPowergridModel(int powergridId) {
		PowergridModel model = new PowergridModel(alertContext);

		model.setAreas(getAreas(powergridId));
		model.setBranches(getBranches(powergridId));
		model.setSubstations(getSubstations(powergridId));
		try {
			model.setBuses(getBuses(powergridId));
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		model.setLines(getLines(powergridId));
		model.setLoads(getLoads(powergridId));
		model.setMachines(getMachines(powergridId));
		model.setPowergrid(getPowergridById(powergridId));


		model.setSwitchedShunts(getSwitchedShunts(powergridId));
		// model.setTimesteps(getTimeSteps(powergridId));
		model.setTransformers(getTransformers(powergridId));
		model.setZones(getZones(powergridId));

		return model;
	}

	public List<Timestamp> getTimeSteps(int powergridId) {
		List<Timestamp> items = new ArrayList<Timestamp>();
		String dbQuery = "select * from powergridtimesteps where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				items.add(rs.getTimestamp(2));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;

	}

	public List<Area> getAreas(int powergridId) {
		List<Area> items = new ArrayList<Area>();
		String dbQuery = "select * from areas where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				Area area = new Area();
				area.setPowergridId(powergridId);
				area.setAreaName(rs.getString(2));
				items.add(area);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;
	}

	public List<Branch> getBranches(int powergridId) {
		List<Branch> items = new ArrayList<Branch>();
		String dbQuery = "select * from branches where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				Branch branch = new Branch();
				branch.setPowergridId(powergridId);
				branch.setBranchId(rs.getInt(1));
				branch.setFromBusNumber(rs.getInt(3));
				branch.setToBusNumber(rs.getInt(4));
				branch.setIndexNum(rs.getInt(5));
				branch.setR(rs.getDouble(6));
				branch.setX(rs.getDouble(7));
				branch.setRating(rs.getDouble(8));
				branch.setStatus(rs.getInt(9));
				branch.setP(rs.getDouble("P"));
				branch.setQ(rs.getDouble("Q"));
				items.add(branch);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;
	}

	public List<Bus> getBuses(int powergridId) {
		List<Bus> items = new ArrayList<Bus>();
		String dbQuery = "select * from buses where PowerGridId = " + powergridId + " ORDER BY BusNumber";

		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				Bus bus = new Bus();
				bus.setPowergridId(powergridId);
				bus.setBusNumber(rs.getInt(1));
				bus.setSubstationId(rs.getInt(3));
				bus.setBusName(rs.getString(4));
				bus.setBaseKv(rs.getDouble(5));
				bus.setCode(rs.getInt(6));
				bus.setVa(rs.getDouble(7));
				bus.setVm(rs.getDouble(8));
				items.add(bus);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;
	}

	public List<Line> getLines(int powergridId) {
		List<Line> items = new ArrayList<Line>();
		String dbQuery = "select * from lines_ where PowerGridId = " + powergridId + " ORDER BY LineId";
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				Line line = new Line();
				line.setPowergridId(powergridId);
				line.setLineId(rs.getInt(1));
				line.setBcap(rs.getDouble(4));
				line.setBranchId(rs.getInt(3));
				items.add(line);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;

	}

	public List<Load> getLoads(int powergridId) {
		List<Load> items = new ArrayList<Load>();
		String dbQuery = "select * from loads where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				Load load = new Load();
				load.setPowergridId(powergridId);
				load.setBusNumber(rs.getInt(3));
				load.setLoadId(rs.getInt(1));
				load.setLoadName(rs.getString(4));
				load.setPload(rs.getDouble(5));
				load.setQload(rs.getDouble(6));
				items.add(load);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;
	}

	public List<Machine> getMachines(int powergridId) {
		List<Machine> items = new ArrayList<Machine>();
		String dbQuery = "select * from machines where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				Machine machine = new Machine();
				machine.setPowergridId(powergridId);
				machine.setBusNumber(rs.getInt(3));
				machine.setIsSvc(rs.getInt(12));
				machine.setMachineId(rs.getInt(1));
				machine.setMachineName(rs.getString(4));
				machine.setMaxPgen(rs.getDouble(7));
				machine.setMaxQgen(rs.getDouble(8));
				machine.setMinPgen(rs.getDouble(9));
				machine.setMinQgen(rs.getDouble(10));
				machine.setPgen(rs.getDouble(5));
				machine.setQgen(rs.getDouble(6));
				machine.setStatus(rs.getInt(11));
				items.add(machine);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;
	}

	public List<SwitchedShunt> getSwitchedShunts(int powergridId) {
		List<SwitchedShunt> items = new ArrayList<SwitchedShunt>();
		String dbQuery = "select * from switchedshunts where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				SwitchedShunt shunt = new SwitchedShunt();
				shunt.setPowergridId(powergridId);
				shunt.setBinit(rs.getDouble(7));
				shunt.setBshunt(rs.getDouble(6));
				shunt.setBusNumber(rs.getInt(3));
				shunt.setStatus(rs.getInt(5));
				shunt.setSwitchedShuntId(rs.getInt(1));
				shunt.setSwitchedShuntName(rs.getString(4));
				items.add(shunt);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;
	}

	public List<Substation> getSubstations(int powergridId) {
		List<Substation> items = new ArrayList<Substation>();
		String dbQuery = "select * from substations where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();

			rs = stmt.executeQuery(dbQuery.toLowerCase());

			while (rs.next()) {
				Substation substation = new Substation();
				substation.setPowergridId(powergridId);
				substation.setAreaName(rs.getString(3));
				substation.setLatitude(rs.getDouble(6));
				substation.setLongitude(rs.getDouble(7));
				substation.setSubstationId(rs.getInt(1));
				substation.setSubstationName(rs.getString(5));
				substation.setZoneName(rs.getString(4));
				substation.setMrid(rs.getString("mrid"));
				items.add(substation);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;
	}

	public List<Transformer> getTransformers(int powergridId) {
		List<Transformer> items = new ArrayList<Transformer>();
		String dbQuery = "select * from transformers where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());
			while (rs.next()) {
				Transformer transformer = new Transformer();
				transformer.setPowergridId(powergridId);
				transformer.setBranchId(rs.getInt(3));
				transformer.setRatio(rs.getDouble(4));
				transformer.setTapPosition(rs.getDouble(5));
				transformer.setTransformerId(rs.getInt(1));
				items.add(transformer);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;
	}

	public List<Zone> getZones(int powergridId) {
		List<Zone> items = new ArrayList<Zone>();
		String dbQuery = "select * from zones where PowerGridId = " + powergridId;
		ResultSet rs = null;
		Connection conn = null;

		try {
			conn = datasource.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(dbQuery.toLowerCase());
			while (rs.next()) {
				Zone zone = new Zone();
				zone.setPowergridId(powergridId);
				zone.setZoneName(rs.getString(1));
				items.add(zone);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return items;

	}

	private void updateModelToTimestep(PowergridModel model, Timestamp timestamp) {
		// Build the sql using the databasename as a format parameter
		String queryLine = "SELECT lts.LineId, lts.Status, lts.P as PFlow, lts.Q as QFlow from linetimesteps lts WHERE lts.PowergridId=? and lts.TimeStep=?";
		String queryMachine = "SELECT mts.MachineId, mts.PGen, mts.QGen, mts.Status from machinetimesteps mts WHERE mts.PowergridId=? and mts.TimeStep=?";
		String queryLoads = "SELECT lts.LoadId, lts.PLoad, lts.QLoad from loadtimesteps lts WHERE lts.PowergridId=? and lts.TimeStep=?";
		String queryShunts = "SELECT sts.SwitchedShuntId, sts.Status from switchedshunttimesteps sts WHERE sts.PowergridId=? and sts.TimeStep=?";
		try {
			int powergridId = model.getPowergrid().getPowergridId();
			String timestep = timestamp.toString();

			// Prepare and execute results.
			ResultSet rs = prepareAndExecute(queryLine, powergridId, timestep);
			HashSet<Integer> doneLines = new HashSet<Integer>();
			while (rs.next()) {
				int lineId = rs.getInt("lts.LineId");

				if (!doneLines.contains(lineId)) {
					Branch branch = model.getBranch(lineId);
					branch.setP(rs.getDouble("PFlow"));
					branch.setQ(rs.getDouble("QFlow"));
					branch.setStatus(rs.getInt("lts.Status"));
					doneLines.add(lineId);
				}
			}

			// Prepare and execute results.
			rs = prepareAndExecute(queryMachine, powergridId, timestep);
			HashSet<Integer> doneMachines = new HashSet<Integer>();
			while (rs.next()) {
				int id = rs.getInt("mts.MachineId");

				if (!doneMachines.contains(id)) {
					Machine item = model.getMachine(id);
					
					if (item == null){
						log.error("Machine is null can't update it! for id " + id);
						continue;
					}
					item.setPgen(rs.getDouble("mts.PGen"));
					item.setQgen(rs.getDouble("mts.QGen"));
					item.setStatus(rs.getInt("mts.Status"));

					doneMachines.add(id);
				}
			}

			// Prepare and execute results.
			rs = prepareAndExecute(queryLoads, powergridId, timestep);
			HashSet<Integer> doneLoads = new HashSet<Integer>();
			while (rs.next()) {
				int id = rs.getInt("lts.LoadId");

				if (!doneLoads.contains(id)) {
					Load item = model.getLoad(id);
					if (item == null){
						log.error("Load is null can't update it! for id " + id);
						continue;
					}
					item.setPload(rs.getDouble("lts.PLoad"));
					item.setQload(rs.getDouble("lts.QLoad"));

					doneLoads.add(id);
				}
			}

			// Prepare and execute results.
			rs = prepareAndExecute(queryShunts, powergridId, timestep);
			HashSet<Integer> doneShunts = new HashSet<Integer>();
			while (rs.next()) {
				int id = rs.getInt("sts.SwitchedShuntId");

				if (!doneShunts.contains(id)) {
					SwitchedShunt item = model.getSwitchedShunt(id);
					item.setStatus(rs.getInt("sts.Status"));

					doneShunts.add(id);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			// Null powergrid == something failed!
			model = null;
		}
	}

	private ResultSet prepareAndExecute(String query, int powergridId, String timestep) throws ParseException, SQLException, InvalidDatasourceException {
		PreparedStatement stmt = datasource.getConnection().prepareStatement(query);
		stmt.setInt(1, powergridId);
		stmt.setString(2, timestep);
		return stmt.executeQuery();
	}

}
