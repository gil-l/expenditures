/*
 * @(#)UnconfirmInvoices.java
 *
 * Copyright 2009 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the Expenditure Tracking Module.
 *
 *   The Expenditure Tracking Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.activities;

import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;

import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundProcess;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;

/**
 * 
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
public class UnconfirmInvoices extends WorkflowActivity<RefundProcess, ActivityInformation<RefundProcess>> {

    @Override
    public boolean isActive(RefundProcess process, User user) {
        Person person = user.getExpenditurePerson();
        return isUserProcessOwner(process, user)
                && process.isActive()
                && !process.isPayed()
                && !process.getRequest().getConfirmedInvoices().isEmpty()
                && process.isRealValueFullyAttributedToUnits()
                && ((process.isAccountingEmployee(person) && !process.hasProjectsAsPayingUnits()) || (process
                        .isProjectAccountingEmployee(person) && process.hasProjectsAsPayingUnits()))
                && ((process.hasProjectsAsPayingUnits() && !process.getRequest()
                        .hasAllocatedFundsPermanentlyForAnyProjectFinancer()) || (!process.hasProjectsAsPayingUnits() && !process
                        .getRequest().hasAnyEffectiveFundAllocationId()));
//		&& (!process.getRequest().hasAllocatedFundsPermanentlyForAnyProjectFinancer());
    }

    @Override
    protected void process(ActivityInformation<RefundProcess> activityInformation) {
        final RefundProcess process = activityInformation.getProcess();
        process.unconfirmInvoicesByPerson(Person.getLoggedPerson());
        process.getRequest().cancelFundAllocationRequest(true);
    }

    @Override
    public String getLocalizedName() {
        return BundleUtil.getString(getUsedBundle(), "label." + getClass().getName());
    }

    @Override
    public String getUsedBundle() {
        return "resources/AcquisitionResources";
    }
}
