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
package pnnl.goss.gridpack.common.datamodel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import pnnl.goss.powergrid.PowergridModel;
import pnnl.goss.powergrid.datamodel.Bus;
import pnnl.goss.powergrid.datamodel.Load;
import pnnl.goss.powergrid.datamodel.Machine;
import pnnl.goss.powergrid.datamodel.SwitchedShunt;

@XmlRootElement(name="GridpackPowergrid")
public class GridpackPowergrid {
	PowergridModel grid;
	List<GridpackBus> buses = new ArrayList<GridpackBus>();
	
	HashMap<Integer, List<GridpackShunt>> busShuntMap = new HashMap<Integer, List<GridpackShunt>>();
	HashMap<Integer, List<GridpackGenerator>> busGeneratorMap = new HashMap<Integer, List<GridpackGenerator>>();
	HashMap<Integer, List<GridpackLoad>> busLoadMap = new HashMap<Integer, List<GridpackLoad>>();
	
	/**
	 * Required or the jaxb will complain.
	 */
	@SuppressWarnings("unused")
	private GridpackPowergrid(){
		
	}
	
	public GridpackPowergrid(PowergridModel grid){
		this.grid = grid;
		
		for(SwitchedShunt item:grid.getSwitchedShunts()){
			if (!busShuntMap.containsKey(item.getBusNumber())){
				busShuntMap.put(item.getBusNumber(), new ArrayList<GridpackShunt>());
			}
			
			busShuntMap.get(item.getBusNumber()).add(GridpackShunt.buildFromObject(item));
		}
		
		for(Load item:grid.getLoads()){
			if (!busLoadMap.containsKey(item.getBusNumber())){
				busLoadMap.put(item.getBusNumber(), new ArrayList<GridpackLoad>());
			}
			
			busLoadMap.get(item.getBusNumber()).add(GridpackLoad.buildFromObject(item));
		}
		
		for(Machine item:grid.getMachines()){
			if (!busGeneratorMap.containsKey(item.getBusNumber())){
				busGeneratorMap.put(item.getBusNumber(), new ArrayList<GridpackGenerator>());
			}
			
			busGeneratorMap.get(item.getBusNumber()).add(GridpackGenerator.buildFromObject(item));
		}
		
		for(Bus bus:this.grid.getBuses()){
			GridpackBus newBus = GridpackBus.buildFromObject(bus);
			
			if(busShuntMap.containsKey(newBus.getBusNumber())){
				newBus.setShunts(busShuntMap.get(newBus.getBusNumber()));
			}
			
			if(busGeneratorMap.containsKey(newBus.getBusNumber())){
				newBus.setGenerators(busGeneratorMap.get(newBus.getBusNumber()));
			}
			
			if(busLoadMap.containsKey(newBus.getBusNumber())){
				newBus.setLoads(busLoadMap.get(newBus.getBusNumber()));
			}
			
			buses.add(newBus);
		}
	}
	
	@XmlElementWrapper(name="Buses")
	@XmlElement(name="Bus", type=GridpackBus.class)
	public Collection<GridpackBus> getBuses(){
		return this.buses;
	}
}
