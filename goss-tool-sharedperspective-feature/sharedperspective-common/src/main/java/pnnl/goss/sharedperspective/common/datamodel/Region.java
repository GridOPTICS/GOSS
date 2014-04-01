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
package pnnl.goss.sharedperspective.common.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of the model object '<em><b>Region</b></em>'. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public class Region extends IdentifiedObject {

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private List<Substation> substations = new ArrayList<Substation>();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private String organization = null;

	/**
	 * Returns the value of '<em><b>substations</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>substations</b></em>' feature
	 * @generated
	 */
	public List<Substation> getSubstations() {
		return substations;
	}

	/**
	 * Adds to the <em>substations</em> feature.
	 * 
	 * @param substationsValue
	 *            the value to add
	 * 
	 * @generated
	 */
	public boolean addToSubstations(Substation substationsValue) {
		if (!substations.contains(substationsValue)) {
			boolean result = substations.add(substationsValue);
			return result;
		}
		return false;
	}

	/**
	 * Removes from the <em>substations</em> feature.
	 * 
	 * @param substationsValue
	 *            the value to remove
	 * 
	 * @generated
	 */
	public boolean removeFromSubstations(Substation substationsValue) {
		if (substations.contains(substationsValue)) {
			boolean result = substations.remove(substationsValue);
			return result;
		}
		return false;
	}

	/**
	 * Clears the <em>substations</em> feature.
	 * 
	 * @generated
	 */
	public void clearSubstations() {
		while (!substations.isEmpty()) {
			removeFromSubstations(substations.iterator().next());
		}
	}

	/**
	 * Sets the '{@link Region#getSubstations() <em>substations</em>}' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newSubstations
	 *            the new value of the '{@link Region#getSubstations()
	 *            substations}' feature.
	 * @generated
	 */
	public void setSubstations(List<Substation> newSubstations) {
		substations = newSubstations;
	}

	/**
	 * Returns the value of '<em><b>organization</b></em>' feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @return the value of '<em><b>organization</b></em>' feature
	 * @generated
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * Sets the '{@link Region#getOrganization() <em>organization</em>}'
	 * feature.
	 * 
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param newOrganization
	 *            the new value of the '{@link Region#getOrganization()
	 *            organization}' feature.
	 * @generated
	 */
	public void setOrganization(String newOrganization) {
		organization = newOrganization;
	}

	/**
	 * A toString method which prints the values of all EAttributes of this
	 * instance. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		return "Region " + " [organization: " + getOrganization() + "]";
	}
}
