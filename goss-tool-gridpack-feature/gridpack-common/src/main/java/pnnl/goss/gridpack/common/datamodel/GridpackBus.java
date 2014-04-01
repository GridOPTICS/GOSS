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

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import pnnl.goss.powergrid.datamodel.Bus;

@XmlRootElement(name="Bus")
public class GridpackBus {
	/**
	 * Field loads - A link to the loads that are contained at this bus.
	 */
	private List<GridpackLoad> loads;

	/**
	 * Field shunts - A link to the shunts that are contained at this bus.
	 */
	private List<GridpackShunt> shunts;

	/**
	 * Field generators - A link to the generators that are contained at this
	 * bus.
	 */
	private List<GridpackGenerator> generators;

	/**
	 * Field mrid
	 */
	private String mrid;

	/**
	 * Field busNumber.
	 */
	private int busNumber;

	/**
	 * Field baseKV.
	 */
	private double baseKV;

	/**
	 * Field code.
	 */
	private int code;

	/**
	 * Field va.
	 */
	private double va;

	/**
	 * Field vm.
	 */
	private double vm;

	/**
	 * Field powerGridId.
	 */
	private int powerGridId;

	/**
	 * Field busName.
	 */
	private java.lang.String busName;

	/**
	 * Field loadPl
	 */
	private double loadPl;

	/**
	 * Field loadQl
	 */
	private double loadQl;

	/**
	 * Field shuntGl
	 */
	private double shuntGl;

	/**
	 * Field shuntBl
	 */
	private double shuntBl;

	/**
	 * Field area
	 */
	private int area;

	/**
	 * Field zone
	 */
	private int zone;

	/**
	 * Field owner
	 */
	private int owner;

	@XmlElementWrapper(name = "Loads")
	@XmlElement(name = "Load", type = GridpackLoad.class)
	public Collection<GridpackLoad> getLoads() {
		return this.loads;
	}

	public void setLoads(List<GridpackLoad> list) {
		loads = list;
	}

	@XmlElementWrapper(name = "Shunts")
	@XmlElement(name = "Shunt", type = GridpackShunt.class)
	public Collection<GridpackShunt> getShunts() {
		return this.shunts;
	}

	public void setShunts(List<GridpackShunt> list) {
		shunts = list;
	}

	@XmlElementWrapper(name = "Generators")
	@XmlElement(name = "Generator", type = GridpackGenerator.class)
	public Collection<GridpackGenerator> getGenerators() {
		return this.generators;
	}

	public void setGenerators(List<GridpackGenerator> list) {
		generators = list;
	}

	/**
	 * @return the owner
	 */
	@XmlElement(name = "BUS_OWNER")
	public int getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(int owner) {
		this.owner = owner;
	}

	/**
	 * @return the mrid
	 */
	@XmlElement(name = "BUS_MRID")
	public String getMrid() {
		return mrid;
	}

	/**
	 * @param mrid
	 *            the mrid to set
	 */
	public void setMrid(String mrid) {
		this.mrid = mrid;
	}

	/**
	 * @return the powerGridId
	 */
	public int getPowerGridId() {
		return powerGridId;
	}

	/**
	 * @param powerGridId
	 *            the powerGridId to set
	 */
	public void setPowerGridId(int powerGridId) {
		this.powerGridId = powerGridId;
	}

	/**
	 * @return the loadPl
	 */
	@XmlElement(name = "BUS_LOAD_PL")
	public double getLoadPl() {
		return loadPl;
	}

	/**
	 * @param loadPl
	 *            the loadPl to set
	 */
	public void setLoadPl(double loadPl) {
		this.loadPl = loadPl;
	}

	/**
	 * @return the loadQl
	 */
	@XmlElement(name = "BUS_LOAD_QL")
	public double getLoadQl() {
		return loadQl;
	}

	/**
	 * @param loadQl
	 *            the loadQl to set
	 */
	public void setLoadQl(double loadQl) {
		this.loadQl = loadQl;
	}

	/**
	 * @return the shuntGl
	 */
	@XmlElement(name = "BUS_SHUNT_GL")
	public double getShuntGl() {
		return shuntGl;
	}

	/**
	 * @param shuntGl
	 *            the shuntGl to set
	 */
	public void setShuntGl(double shuntGl) {
		this.shuntGl = shuntGl;
	}

	/**
	 * @return the shuntBl
	 */
	@XmlElement(name = "BUS_SHUNT_BL")
	public double getShuntBl() {
		return shuntBl;
	}

	/**
	 * @param shuntBl
	 *            the shuntBl to set
	 */
	public void setShuntBl(double shuntBl) {
		this.shuntBl = shuntBl;
	}

	/**
	 * @return the area
	 */
	@XmlElement(name = "BUS_AREA")
	public int getArea() {
		return area;
	}

	/**
	 * @param area
	 *            the area to set
	 */
	public void setArea(int area) {
		this.area = area;
	}

	/**
	 * @return the zone
	 */
	@XmlElement(name = "BUS_ZONE")
	public int getZone() {
		return zone;
	}

	/**
	 * @param zone
	 *            the zone to set
	 */
	public void setZone(int zone) {
		this.zone = zone;
	}

	@XmlElement(name = "BUS_NUMBER")
	public int getBusNumber() {
		return busNumber;
	}

	public void setBusNumber(int busNumber) {
		this.busNumber = busNumber;
	}

	@XmlElement(name = "BUS_BASEKV")
	public double getBaseKV() {
		return baseKV;
	}

	public void setBaseKV(double baseKV) {
		this.baseKV = baseKV;
	}

	@XmlElement(name = "BUS_TYPE")
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@XmlElement(name = "BUS_VOLTAGE_ANG")
	public double getVa() {
		return va;
	}

	public void setVa(double va) {
		this.va = va;
	}

	@XmlElement(name = "BUS_VOLTAGE_MAG")
	public double getVm() {
		return vm;
	}

	public void setVm(double vm) {
		this.vm = vm;
	}

	@XmlElement(name = "BUS_NAME")
	public java.lang.String getBusName() {
		return busName;
	}

	public void setBusName(java.lang.String busName) {
		this.busName = busName;
	}

	private GridpackBus() {

	}

	private GridpackBus(Bus bus) {
		setBusNumber(bus.getBusNumber());
		setBusName(bus.getBusName());
		setVm(bus.getVm());
		setVa(bus.getVa());
		setCode(bus.getCode());
		setBaseKV(bus.getBaseKv());
	}

	public static GridpackBus buildFromObject(Bus bus) {
		return new GridpackBus(bus);
	}
}
