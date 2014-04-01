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
package pnnl.goss.powergrid;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import pnnl.goss.core.Data;
import pnnl.goss.powergrid.datamodel.Area;
import pnnl.goss.powergrid.datamodel.Branch;
import pnnl.goss.powergrid.datamodel.Bus;
import pnnl.goss.powergrid.datamodel.Line;
import pnnl.goss.powergrid.datamodel.Load;
import pnnl.goss.powergrid.datamodel.Machine;
import pnnl.goss.powergrid.datamodel.Powergrid;
import pnnl.goss.powergrid.datamodel.Substation;
import pnnl.goss.powergrid.datamodel.SwitchedShunt;
import pnnl.goss.powergrid.datamodel.Transformer;
import pnnl.goss.powergrid.datamodel.Zone;

@XmlRootElement(name="PowergridModel")
public class PowergridModel extends Data {

	private static final long serialVersionUID = 2759654942517938088L;
	
	private static final int BRANCHES = 0;
	private static final int TRANSFORMERS = 1;
	private static final int LINES = 2;
	private static final int SUBSTATIONS = 3;
	private static final int LOADS = 4;
	private static final int MACHINES = 5;
	private static final int SWITCHEDSHUNTS = 6;
	//private static final int AREAS = 7;
	//private static final int ZONES = 8;
	private static final int BUSES = 9;
	
	List<Bus> buses = null;
	List<Branch> branches = null;
	List<Line> lines = null;
	List<Transformer> transformers = null;
	List<Substation> substations = null;
	List<Load> loads = null;
	List<Machine> machines = null;
	List<SwitchedShunt> switchShunts = null;
	List<Area> areas = null;
	List<Zone> zones = null;
	//List<Timestamp> timesteps = null;
	
	@XmlElement(name="Powergrid")
	Powergrid powergrid = null;
	
	@XmlTransient
	public HashMap<Integer, HashMap<Integer, Object>> elementMap = new HashMap<Integer, HashMap<Integer, Object>>();
	
	public PowergridModel(){
		reset();
	}
	
	public void reset(){
		elementMap.clear();
		elementMap.put(BUSES, new HashMap<Integer, Object>());
		elementMap.put(BRANCHES, new HashMap<Integer, Object>());
		elementMap.put(TRANSFORMERS, new HashMap<Integer, Object>());
		elementMap.put(LINES, new HashMap<Integer, Object>());
		elementMap.put(LOADS, new HashMap<Integer, Object>());
		elementMap.put(MACHINES, new HashMap<Integer, Object>());
		elementMap.put(SWITCHEDSHUNTS, new HashMap<Integer, Object>());
		elementMap.put(SUBSTATIONS, new HashMap<Integer, Object>());
		//elementMap.put(AREAS, new HashMap<Integer, Object>());
		//elementMap.put(ZONES, new HashMap<Integer, Object>());
	}
	
	
	
	private void addToMap(int whichMap, int id, Object object){
		elementMap.get(whichMap).put(id,  object);
	}
	
	private Object getFromMap(int whichMap, int id){
		return elementMap.get(whichMap).get(id);
	}
	
	public Transformer getTransformer(int id){
		return (Transformer)getFromMap(TRANSFORMERS, id);
	}
	
	public Load getLoad(int id){
		return (Load)getFromMap(LOADS, id);
	}
	
	public Machine getMachine(int id){
		return (Machine)getFromMap(MACHINES, id);
	}
	
	public SwitchedShunt getSwitchedShunt(int id){
		return (SwitchedShunt)getFromMap(SWITCHEDSHUNTS, id);
	}
	
	public Branch getBranch(int id){
		return (Branch)getFromMap(BRANCHES, id);
	}
	
	
	public Bus getBus(int busNumber){
		return (Bus)getFromMap(BUSES, busNumber);
	}
	
	public Substation getSubstation(int id){
		return (Substation)getFromMap(SUBSTATIONS, id);
	}
	
	public Substation getSubstation(Bus bus){
		return getSubstation(bus.getSubstationId());
	}
	
	@XmlElementWrapper(name="Lines")
	@XmlElement(name="Line", type=Line.class)
	public List<Line> getLines() {
		return lines;
	}
	
	public void setLines(List<Line> lines) {
		for(Line item: lines){
			addToMap(LINES, item.getLineId(), item);			
		}
		this.lines = lines;
	}
	
	@XmlElementWrapper(name="Transformers")
	@XmlElement(name="Transformer", type=Transformer.class)
	public List<Transformer> getTransformers() {
		return transformers;
	}
	public void setTransformers(List<Transformer> transformers) {
		for(Transformer item: transformers){
			addToMap(TRANSFORMERS, item.getTransformerId(), item);			
		}
		this.transformers = transformers;
	}
	@XmlElementWrapper(name="Zones")
	@XmlElement(name="Zone", type=Zone.class)
	public List<Zone> getZones() {
		return zones;
	}
	public void setZones(List<Zone> zones) {
		/*for(Zone item: zones){
			addToMap(LINES, item.getz.getTransformerId(), item);			
		}*/
		this.zones = zones;
	}
	@XmlElementWrapper(name="Areas")
	@XmlElement(name="Area", type=Area.class)
	public List<Area> getAreas() {
		return areas;
	}
	public Powergrid getPowergrid() {
		return powergrid;
	}
	public void setPowergrid(Powergrid powergrid) {
		this.powergrid = powergrid;
	}
	@XmlElementWrapper(name="Branches")
	@XmlElement(name="Branch", type=Branch.class)
	public List<Branch> getBranches() {
		return branches;
	}
	public void setBranches(List<Branch> branches) {
		for(Branch item: branches){
			addToMap(BRANCHES, item.getBranchId(), item);			
		}
		this.branches = branches;
	}
	@XmlElementWrapper(name="Substations")
	@XmlElement(name="Substation", type=Substation.class)
	public List<Substation> getSubstations() {
		return substations;
	}
	public void setSubstations(List<Substation> substations) {
		for(Substation item: substations){
			addToMap(SUBSTATIONS, item.getSubstationId(), item);			
		}
		this.substations = substations;
	}
	@XmlElementWrapper(name="Loads")
	@XmlElement(name="Load", type=Load.class)
	public List<Load> getLoads() {
		return loads;
	}
	public void setLoads(List<Load> loads) {
		for(Load item: loads){
			addToMap(LOADS, item.getLoadId(), item);			
		}
		this.loads = loads;
	}
	@XmlElementWrapper(name="Machines")
	@XmlElement(name="Machine", type=Machine.class)
	public List<Machine> getMachines() {
		return machines;
	}
	public void setMachines(List<Machine> machines) {
		for(Machine item: machines){
			addToMap(MACHINES, item.getMachineId(), item);			
		}
		this.machines = machines;
	}
	@XmlElementWrapper(name="SwitchedShunts")
	@XmlElement(name="SwitchedShunt", type=SwitchedShunt.class)
	public List<SwitchedShunt> getSwitchedShunts() {
		return switchShunts;
	}
	public void setSwitchedShunts(List<SwitchedShunt> switchShunts) {
		for(SwitchedShunt item: switchShunts){
			addToMap(SWITCHEDSHUNTS, item.getSwitchedShuntId(), item);			
		}
		this.switchShunts = switchShunts;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@XmlElementWrapper(name="Buses")
	@XmlElement(name="Bus", type=Bus.class)
	public List<Bus> getBuses() {
		return buses;
	}

	public void setBuses(List<Bus> buses) {
		for(Bus item: buses){
			addToMap(BUSES, item.getBusNumber(), item);			
		}
		this.buses = buses;
	}

	public void setAreas(List<Area> areas) {
		this.areas = areas;
	}

	/*
	public List<Timestamp> getTimesteps() {
		return timesteps;
	}

	public void setTimesteps(List<Timestamp> timestamps) {
		this.timesteps = timestamps;
	}*/

}
