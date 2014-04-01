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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pnnl.goss.powergrid.datamodel.Machine;

@XmlRootElement(name="Generator")
public class GridpackGenerator {

	// Bus number to which the generator is connected
	// type: integer
	@XmlElement(name="GENERATOR_BUSNUMBER")
	public int busNumber;
	
	// One- or two-character uppercase nonblank alphanumeric machine identifier 
	// used to distinguish among multiple machines connected to the same bus    
	// type: string
	@XmlElement(name="GENERATOR_ID")
	public String generatorId;
	
	// Generator active power output, entered in MW    
	// type: real float
	@XmlElement(name="GENERATOR_PG")
	public double pG;
	
	// Generator reactive power output, entered in MVar
	// type: real float
	@XmlElement(name="GENERATOR_QG")
	public double qG;
	
	// Maximum generator reactive power output; entered in Mvar
	// type: real float
	@XmlElement(name="GENERATOR_QMAX")
	public double qMax;
	
	// Minimum generator reactive power output; entered in Mvar
	// type: real float
	@XmlElement(name="GENERATOR_QMIN")
	public double Min;
	
	// Regulated voltage setpoint; entered in pu
	// type: real float
	@XmlElement(name="GENERATOR_VS")
	public double vs;
	
	// Bus number of a remote type 1 or 2 bus whose voltage is to be regulated by this plant to the
	// value specified by GENERATOR_VS
	// type: integer
	@XmlElement(name="GENERATOR_IREG")
	public int iReg;
	
	// Total MVA base of the units represented by this machine; entered in MVA. 
	// This quantity is not needed in normal power flow and equivalent construction work,
	// but is required for switching studies, fault analysis, and dynamic simulation.
	// type: real float
	@XmlElement(name="GENERATOR_MBASE")
	public double mBase;
	
	// Complex machine impedance entered in pu on GENERATOR_MBASE base. 
	// This data is not needed in normal power flow and equivalent construction work
	// but is required for switching studies, fault analysis, and dynamic simulation. 
	// For dynamic simulation, this impedance must be set equal to the subtransient impedance 
	// for those generators to be modeled by subtransient level machine models, 
	// and to transient impedance for those to be modeled by classical or transient level models
	// type: complex
	@XmlElement(name="GENERATOR_ZSORCE")
	public String zSorce;
	
	// Step-up transformer impedance; entered in pu on GENERATOR_MBASE base. 
	// It should be entered as zero if the step-up transformer is explicitly modeled
	// type: complex
	@XmlElement(name="GENERATOR_XTRAN")
	public String xTran;
	
	// Machine impedance, pu on MBASE
	@XmlElement(name="GENERATOR_ZR")
	public double zR;
	
	// Machine impedance, pu on MBASE
	@XmlElement(name="GENERATOR_ZX")
	public double zX;
	
	//Step up transformer impedance, p.u. on MBASE
	@XmlElement(name="GENERATOR_RT")
	public double rT;
	
	//Step up transformer impedance, p.u. on MBASE
	@XmlElement(name="GENERATOR_XT")
	public double xT;
	
	// Step-up transformer off-nominal turns ratio; entered in pu
	// type: real float
	@XmlElement(name="GENERATOR_GTAP")
	public double gTap;
	
	// Initial machine status
	// 1: in-service
	// 0: out-of-service
	// type: integer
	@XmlElement(name="GENERATOR_STAT")
	public int status;
	
	// Percent of the total Mvar required to hold the voltage at the bus controlled by bus 
	// that are to be contributed by the generation. It must be positive
	// type: real float
	@XmlElement(name="GENERATOR_RMPCT")
	public double rmPct;
	
	// Maximum generator active power output; entered in MW
	// type: real float
	@XmlElement(name="GENERATOR_PMAX")
	public double pMax;
	
	// Minimum generator active power output; entered in MW
	// type: real float
	@XmlElement(name="GENERATOR_PMIN")
	public double pMin;
	
	// Generator owner number    
	// type: integer
	@XmlElement(name="GENERATOR_OWNER")
	public int owner;


	private GridpackGenerator(){
		
	}
	
	public static GridpackGenerator buildFromObject(Machine item) {
		GridpackGenerator element = new GridpackGenerator();
		
		element.busNumber = item.getBusNumber();
		element.generatorId = item.getMachineName();
		element.pG = item.getMaxPgen();
		element.qG = item.getQgen();
		element.pMin = item.getMinPgen();
		element.pMax = item.getMaxPgen();
		element.status = item.getStatus();
		
		return element ;
	}

}
