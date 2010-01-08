package pt.ist.expenditureTrackingSystem.domain.acquisitions;

import java.util.List;

import org.apache.commons.collections.Predicate;

import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;

public class ProcessesThatAreAuthorizedByUserPredicate implements Predicate {

    private Person person;

    public ProcessesThatAreAuthorizedByUserPredicate(Person person) {
	this.person = person;
    }

    public boolean evaluate(Object arg0) {
	PaymentProcess process = (PaymentProcess) arg0;
	if (process.getRequest() == null) {
	    return false;
	}
	List<Unit> units = process.getPayingUnits();

	boolean evaluation = false;
	for (Unit unit : units) {
	    evaluation = evaluation || evaluate(unit, process);
	}

	return evaluation;
    }

    private boolean evaluate(Unit unit, PaymentProcess process) {

	if (unit.hasAuthorizationsFor(person)) {
	    return true;
	} else {
	    for (Authorization authorization : unit.getAuthorizations()) {
		Person person = authorization.getPerson();

		if (process.hasAnyAvailableActivity(person.getUser(), false)) {
		    return false;
		}
	    }
	    return unit.hasParentUnit() && evaluate(unit.getParentUnit(), process);
	}
    }
}
