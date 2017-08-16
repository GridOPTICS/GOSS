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
package gov.pnnl.goss.client;

import java.io.Serializable;

import gov.pnnl.goss.client.api.Event;
import gov.pnnl.goss.client.api.SeverityType;

public class GossEvent implements Serializable, Event {

	private static final long serialVersionUID = -1962993549035537429L;

	int id;
	String status; // Active,Closed
	protected SeverityType severity;
	protected String eventType;
	protected String description;
	int relatedEventId;

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#getSeverityTypes()
	 */
	@Override
	public SeverityType[] getSeverityTypes() {
		return SeverityType.values();
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#getId()
	 */
	@Override
	public int getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#setId(int)
	 */
	@Override
	public void setId(int id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#getStatus()
	 */
	@Override
	public String getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#setStatus(java.lang.String)
	 */
	@Override
	public void setStatus(String status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#getSeverity()
	 */
	@Override
	public SeverityType getSeverity() {
		return severity;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#setSeverity(gov.pnnl.goss.client.GossEvent.SeverityType)
	 */
	@Override
	public void setSeverity(SeverityType severity) {
		this.severity = severity;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#getEventType()
	 */
	@Override
	public String getEventType() {
		return eventType;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#setEventType(java.lang.String)
	 */
	@Override
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#getRelatedEventId()
	 */
	@Override
	public int getRelatedEventId() {
		return relatedEventId;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.goss.client.Event#setRelatedEventId(int)
	 */
	@Override
	public void setRelatedEventId(int relatedEventId) {
		this.relatedEventId = relatedEventId;
	}

}
