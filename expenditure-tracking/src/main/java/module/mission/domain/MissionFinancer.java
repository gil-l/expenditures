/*
 * @(#)MissionFinancer.java
 *
 * Copyright 2010 Instituto Superior Tecnico
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
package module.mission.domain;

import java.util.Set;

import module.finance.util.Money;
import module.organization.domain.Person;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;

import pt.ist.expenditureTrackingSystem._development.Bundle;
import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
import pt.ist.expenditureTrackingSystem.domain.organization.Project;
import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
import pt.ist.expenditureTrackingSystem.domain.util.DomainException;

/**
 * 
 * @author Luis Cruz
 * 
 */
public class MissionFinancer extends MissionFinancer_Base {

    public MissionFinancer() {
        super();
        setMissionSystem(MissionSystem.getInstance());
    }

    public MissionFinancer(final Mission mission, final Unit unit) {
        this(mission.getMissionVersion(), unit);
    }

    public MissionFinancer(final MissionVersion missionVersion, final Unit unit) {
        this();
        checkUnitIsActive(unit);
        setMissionVersion(missionVersion);
        setUnit(unit);
    }

    private void checkUnitIsActive(final Unit unit) {
        if (!unit.isActive()) {
            throw new DomainException(Bundle.MISSION, "error.mission.financer.closed");
        }
    }

    public void delete() {
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            missionItemFinancer.delete();
        }
        setMissionVersion(null);
        setUnit(null);
        setMissionSystem(null);
        setAccountingUnit(null);
        deleteDomainObject();
    }

    public Money getAmount() {
        Money result = Money.ZERO;
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            final Money amount = missionItemFinancer.getAmount();
            if (amount != null) {
                result = result.add(amount);
            }
        }
        return result;
    }

    public boolean isPendingApprovalBy(final User user) {
        return !hasApproval() && canApprove(user);
    }

    public boolean isPendingAuthorizationBy(final User user) {
        return !hasAuthorization() && canAuthorize(user);
    }

    public boolean isPendingDirectAuthorizationBy(final User user) {
        return !hasAuthorization() && canAuthorizeDirect(user);
    }

    public boolean isPendingCheckByUnderlings(final User user) {
        final Unit unit = getUnit();
        return isPendingCheckByUnderlings(user, unit);
    }

    private boolean isPendingCheckByUnderlings(final User user, final Unit unit) {
        if (unit == null) {
            return false;
        }
        final Set<Unit> checked = getCheckedUnitSet();
        if (!checked.contains(unit)) {
            for (final Authorization authorization : unit.getAuthorizationsSet()) {
                if (authorization.isValid() && authorization.getMaxAmount().isGreaterThanOrEqual(getAmount())) {
                    return false;
                }
            }
            for (final Authorization authorization : unit.getAuthorizationsSet()) {
                if (authorization.isValid() && authorization.getPerson() == user.getExpenditurePerson()) {
                    return true;
                }
            }
        }
        return isPendingCheckByUnderlings(user, unit.getParentUnit());
    }

    public boolean canApprove(final User user) {
        final Unit unit = getUnit();
        return unit.isResponsible(user.getExpenditurePerson());
    }

    public boolean canAuthorize(final User user) {
        final Unit unit = getUnit();
        return unit.isResponsible(user.getExpenditurePerson(), getAmount());
    }

    public boolean canAuthorizeDirect(final User user) {
        final Unit unit = getUnit();
        return unit.isMostDirectAuthorization(user.getExpenditurePerson(), getAmount());
    }

    public void approve(final User user) {
        if (!hasApproval()) {
            final Authorization authorization = findAuthorizationForApproval(user);
            if (authorization != null) {
                setApproval(authorization);
            }
        }
    }

    public void authorize(final User user) {
        if (!hasAuthorization()) {
            final Authorization authorization = findAuthorizationForAuthorization(user);
            if (authorization != null) {
                setAuthorization(authorization);
            }
        }
    }

    public void preAuthorize(final User user) {
        if (!hasAuthorization()) {
            final Authorization authorization = findAuthorizationForApproval(user);
            if (authorization != null) {
                markCheckedUnits(authorization, getUnit());
            }
        }
    }

    private void markCheckedUnits(final Authorization authorization, final Unit unit) {
        if (unit != null) {
            getCheckedUnitSet().add(unit);
            if (authorization.getUnit() != unit) {
                markCheckedUnits(authorization, unit.getParentUnit());
            }
        }
    }

    private Authorization findAuthorizationForApproval(final User user) {
        return findAuthorization(user, Money.ZERO);
    }

    private Authorization findAuthorizationForAuthorization(final User user) {
        return findAuthorization(user, getAmount());
    }

    private Authorization findAuthorization(final User user, final Money amount) {
        final Unit unit = getUnit();
        return unit.findClosestAuthorization(user.getExpenditurePerson(), amount);
    }

    public void unapprove(final User user) {
        if (hasApproval() && canApprove(user)) {
            setApproval(null);
        }
    }

    public void unauthorize(final User user) {
        if (hasAuthorization() && canAuthorize(user)) {
            setAuthorization(null);
        }
    }

    public void unPreAuthorize(final User user) {
        final Authorization authorization = findAuthorizationForApproval(user);
        if (authorization != null) {
            unMarkCheckedUnits(authorization, getUnit());
        }
    }

    private void unMarkCheckedUnits(final Authorization authorization, final Unit unit) {
        if (unit != null) {
            getCheckedUnitSet().remove(unit);
            if (authorization.getUnit() != unit) {
                unMarkCheckedUnits(authorization, unit.getParentUnit());
            }
        }
    }

    public boolean canRemoveApproval(final User user) {
        return !hasAnyAllocatedFunds() && hasApproval() && canApprove(user);
    }

    public boolean canRemoveAuthorization(final User user) {
        return hasAuthorization() && canAuthorize(user);
    }

    public boolean canRemovePreAuthorization(final User user) {
        return !hasAuthorization() && findAuthorizationForApproval(user) != null;
    }

    public boolean hasAllAllocatedFunds() {
        if (!hasAnyMissionItemFinancers()) {
            return false;
        }
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            final MissionItem missionItem = missionItemFinancer.getMissionItem();
            if (missionItem.requiresFundAllocation()) {
                final Money amount = missionItemFinancer.getAmount();
                if (amount != null && amount.isPositive() && missionItemFinancer.getFundAllocationId() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasAnyAllocatedFunds() {
        if (!hasAnyMissionItemFinancers()) {
            return false;
        }
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            final MissionItem missionItem = missionItemFinancer.getMissionItem();
            if (missionItem.requiresFundAllocation()) {
                final Money amount = missionItemFinancer.getAmount();
                if (amount != null && amount.isPositive() && missionItemFinancer.getFundAllocationId() != null
                        && !missionItemFinancer.getFundAllocationId().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAllAllocatedProjectFunds() {
        if (!hasAnyMissionItemProjectFinancers()) {
            return false;
        }
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            if (missionItemFinancer instanceof MissionItemProjectFinancer) {
                final MissionItemProjectFinancer missionItemProjectFinancer = (MissionItemProjectFinancer) missionItemFinancer;
                final Money amount = missionItemProjectFinancer.getAmount();
                if (amount != null && amount.isPositive() && missionItemProjectFinancer.getProjectFundAllocationId() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasAnyAllocatedProjectFunds() {
        if (!hasAnyMissionItemProjectFinancers()) {
            return false;
        }
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            if (missionItemFinancer instanceof MissionItemProjectFinancer) {
                final MissionItemProjectFinancer missionItemProjectFinancer = (MissionItemProjectFinancer) missionItemFinancer;
                final Money amount = missionItemProjectFinancer.getAmount();
                if (amount != null && amount.isPositive() && missionItemProjectFinancer.getProjectFundAllocationId() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnyMissionItemProjectFinancers() {
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            if (missionItemFinancer instanceof MissionItemProjectFinancer) {
                return true;
            }
        }
        return false;
    }

    public boolean canAllocateFunds(final Person person) {
        return person != null && hasAccountingUnit()
                && getAccountingUnit().getPeopleSet().contains(person.getUser().getExpenditurePerson());
    }

    public boolean isAccountManager(final Person person) {
        final Unit unit = getUnit();
        if (!unit.hasSomeAccountManager()) {
            return true;
        } else if (person == null) {
            return false;
        } else {
            return unit.isAccountManager(person.getUser().getExpenditurePerson());
        }
    }

    public boolean canAllocateProjectFunds(final Person person) {
        return person != null && hasAccountingUnit()
                && getAccountingUnit().getProjectAccountantsSet().contains(person.getUser().getExpenditurePerson())
                && hasPendingProjectFundAllocations();
    }

    public boolean isDirectResponsibleForPendingProjectFundAllocation(final Person person) {
        return canAllocateProjectFunds(person) && isAccountManager(person);
    }

    private boolean hasPendingProjectFundAllocations() {
        final Mission mission = getMissionVersion().getMission();
        final MissionProcess missionProcess = mission.getMissionProcess();
        if (missionProcess.isCanceled()) {
            for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
                if (missionItemFinancer instanceof MissionItemProjectFinancer) {
                    final MissionItemProjectFinancer missionItemProjectFinancer =
                            (MissionItemProjectFinancer) missionItemFinancer;
                    final String allocationId = missionItemProjectFinancer.getProjectFundAllocationId();
                    if (allocationId != null && !allocationId.isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
                if (missionItemFinancer instanceof MissionItemProjectFinancer) {
                    final MissionItemProjectFinancer missionItemProjectFinancer =
                            (MissionItemProjectFinancer) missionItemFinancer;
                    final String allocationId = missionItemProjectFinancer.getProjectFundAllocationId();
                    if (allocationId == null || allocationId.isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public void unAllocateFunds(final Person person) {
        if (canAllocateFunds(person)) {
            for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
                missionItemFinancer.setFundAllocationId(null);
            }
        }
    }

    public String getFundAllocationId() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            final String fundAllocationId = missionItemFinancer.getFundAllocationId();
            if (fundAllocationId != null) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(fundAllocationId);
            }
        }
        return stringBuilder.toString();
    }

    public String getProjectFundAllocationId() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            if (missionItemFinancer instanceof MissionItemProjectFinancer) {
                final MissionItemProjectFinancer missionItemProjectFinancer = (MissionItemProjectFinancer) missionItemFinancer;
                final String projectFundAllocationId = missionItemProjectFinancer.getProjectFundAllocationId();
                if (projectFundAllocationId != null) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(missionItemProjectFinancer.getProjectFundAllocationId());
                }
            }
        }
        return stringBuilder.toString();
    }

    public void unAllocateProjectFunds(Person person) {
        if (getAccountingUnit().getProjectAccountantsSet().contains(person.getUser().getExpenditurePerson())) {
            for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
                if (missionItemFinancer instanceof MissionItemProjectFinancer) {
                    final MissionItemProjectFinancer missionItemProjectFinancer =
                            (MissionItemProjectFinancer) missionItemFinancer;
                    missionItemProjectFinancer.setProjectFundAllocationId(null);
                }
            }
        }
    }

    public boolean isProjectFinancer() {
        final Unit unit = getUnit();
        return unit instanceof Project || unit instanceof SubProject;
    }

    public boolean isAuthorized() {
        return hasAuthorization();
    }

    public void clearFundAllocations() {
        for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
            missionItemFinancer.clearFundAllocations();
        }
    }

    MissionFinancer createNewVersion(final MissionVersion missionVersion) {
        final MissionFinancer missionFinancer = new MissionFinancer(missionVersion, getUnit());
        missionFinancer.setApproval(getApproval());
        missionFinancer.setAuthorization(getAuthorization());
        return missionFinancer;
    }

    public boolean isCurrentUserAccountant() {
        final Unit unit = getUnit();
        final User user = Authenticate.getUser();
        return unit.isAccountingEmployee(user.getExpenditurePerson());
    }

    public boolean isCurrentUserProjectAccountant() {
        final Unit unit = getUnit();
        final User user = Authenticate.getUser();
        return unit.isProjectAccountingEmployee(user.getExpenditurePerson());
    }

    public boolean isCurrentUserDirectProjectAccountant() {
        final Unit unit = getUnit();
        final User user = Authenticate.getUser();
        return unit.isProjectAccountingEmployee(user.getExpenditurePerson())
                && (!unit.hasSomeAccountManager() || unit.isAccountManager(user.getExpenditurePerson()));
    }

    @Override
    public AccountingUnit getAccountingUnit() {
        final AccountingUnit accountingUnit = super.getAccountingUnit();
        return accountingUnit == null && hasUnit() ? getUnit().getAccountingUnit() : accountingUnit;
    }

    public boolean isUnitObserver(final User user) {
        return getUnit().isUnitObserver(user);
    }

    public pt.ist.expenditureTrackingSystem.domain.organization.Person getAccountManager() {
        final Unit unit = getUnit();
        return unit == null ? null : unit.getAccountManager();
    }

    @Deprecated
    public java.util.Set<module.mission.domain.MissionItemFinancer> getMissionItemFinancers() {
        return getMissionItemFinancersSet();
    }

    @Deprecated
    public boolean hasAnyMissionItemFinancers() {
        return !getMissionItemFinancersSet().isEmpty();
    }

    @Deprecated
    public boolean hasCommitmentNumber() {
        return getCommitmentNumber() != null;
    }

    @Deprecated
    public boolean hasMissionVersion() {
        return getMissionVersion() != null;
    }

    @Deprecated
    public boolean hasUnit() {
        return getUnit() != null;
    }

    @Deprecated
    public boolean hasMissionSystem() {
        return getMissionSystem() != null;
    }

    @Deprecated
    public boolean hasAuthorization() {
        return getAuthorization() != null;
    }

    @Deprecated
    public boolean hasAccountingUnit() {
        return getAccountingUnit() != null;
    }

    @Deprecated
    public boolean hasApproval() {
        return getApproval() != null;
    }

    public boolean hasBeenCheckedByUnderlings() {
        final Unit unit = getUnit();
        return hasBeenCheckedByUnderlings(unit);
    }

    private boolean hasBeenCheckedByUnderlings(final Unit unit) {
        if (unit == null) {
            return false;
        }
        final Set<Unit> checked = getCheckedUnitSet();
        boolean hasAnyUnderlings = false;
        for (final Authorization authorization : unit.getAuthorizationsSet()) {
            if (authorization.isValid()) {
                if (authorization.getMaxAmount().isGreaterThanOrEqual(getAmount())) {
                    return true;
                } else {
                    hasAnyUnderlings = true;
                }
            }
        }
        return ((!hasAnyUnderlings) || checked.contains(unit)) && hasBeenCheckedByUnderlings(unit.getParentUnit());
    }

}
