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

import pnnl.goss.powergrid.datamodel.Load;

@XmlRootElement(name="Load")
public class GridpackLoad {

	// Bus number to which the load is connected
	// type: integer
	@XmlElement(name="LOAD_BUSNUMBER")
	public int busNumber;
	
	// One- or two-character uppercase nonblank alphanumeric load identifier used to distinguish 
	// among multiple loads connected to the same bus.
	// Default value is '1'
	// type: string
	@XmlElement(name="LOAD_ID")
	public String id;
	
	// Initial load status
	//  1: in-service
	//  0: out-of-service
	// Default value is 1
	// type: integer
	@XmlElement(name="LOAD_STATUS")
	public int status;
	
	// Area to which the load is assigned
	// type: integer
	@XmlElement(name="LOAD_AREA")
	public int area;
	
	// Zone to which the load is assigned
	// type: integer
	@XmlElement(name="LOAD_ZONE")
	public int zone;
	
	// Active power component of constant MVA load; entered in MW
	// type: real float
	@XmlElement(name="LOAD_PL")
	public double pL;
	
	// Reactive power component of constant MVA load; entered in MVar
	// type: real float
	@XmlElement(name="LOAD_QL")
	public double qL;
	
	// Active power component of constant current load; entered in MW at one per unit voltage
	// type: real float
	@XmlElement(name="LOAD_IP")
	public double iP;
	
	// Reactive power component of constant current load; entered in Mvar at one per unit voltage
	// type: real float
	@XmlElement(name="LOAD_IQ")
	public double iQ;
	
	// Active power component of constant admittance load; entered in MW at one per unit voltage
	// type: real float
	@XmlElement(name="LOAD_YP")
	public double yP;
	
	// Reactive power component of constant admittance load; entered in MVar at one per unit voltage
	// type: real float
	@XmlElement(name="LOAD_YQ")
	public double yQ;
	
	// Owner to which the load is assigned
	// type: integer
	@XmlElement(name="LOAD_OWNER")
	public int owner;
	
	private GridpackLoad(){
		
	}
	
	public static GridpackLoad buildFromObject(Load load) {
		GridpackLoad element = new GridpackLoad();
		
		element.busNumber = load.getBusNumber();
		element.status = 1;
		element.pL = load.getPload();
		element.qL = load.getQload();
	
		
		
		
		return element;
	}
	
}
