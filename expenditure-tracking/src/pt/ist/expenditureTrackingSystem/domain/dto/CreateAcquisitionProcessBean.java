package pt.ist.expenditureTrackingSystem.domain.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
import pt.ist.fenixWebFramework.util.DomainReference;

public class CreateAcquisitionProcessBean implements Serializable {

    private DomainReference<Unit> requestingUnit;
    private boolean requestUnitPayingUnit;
    private List<DomainReference<Supplier>> suppliers;
    private DomainReference<Person> requester;

    public CreateAcquisitionProcessBean() {
	setRequestingUnit(null);
	setSupplier(null);
	setRequestUnitPayingUnit(true);
    }

    public CreateAcquisitionProcessBean(AcquisitionRequest acquisitionRequest) {
	setRequestingUnit(acquisitionRequest.getRequestingUnit());
	setSuppliers(acquisitionRequest.getSuppliers());
	if (acquisitionRequest.getPayingUnits().contains(acquisitionRequest.getRequestingUnit())) {
	    setRequestUnitPayingUnit(true);
	}
    }

    public Unit getRequestingUnit() {
	return requestingUnit.getObject();
    }

    public void setRequestingUnit(Unit requestingUnit) {
	this.requestingUnit = new DomainReference<Unit>(requestingUnit);
    }

    public boolean isRequestUnitPayingUnit() {
	return requestUnitPayingUnit;
    }

    public void setRequestUnitPayingUnit(boolean requestUnitPayingUnit) {
	this.requestUnitPayingUnit = requestUnitPayingUnit;
    }

    public void setSupplier(Supplier supplier) {
	this.suppliers = new ArrayList<DomainReference<Supplier>>();
	this.suppliers.add(new DomainReference<Supplier>(supplier));
    }

    public Supplier getSupplier() {
	return this.suppliers.isEmpty() ? null : this.suppliers.get(0).getObject();
    }

    public void setSuppliers(List<Supplier> suppliers) {
	this.suppliers = new ArrayList<DomainReference<Supplier>>();
	for (Supplier supplier : suppliers) {
	    this.suppliers.add(new DomainReference<Supplier>(supplier));
	}
    }

    public List<Supplier> getSuppliers() {
	List<Supplier> suppliers = new ArrayList<Supplier>();
	for (DomainReference<Supplier> supplier : this.suppliers) {
	    suppliers.add(supplier.getObject());
	}
	return suppliers;
    }

    public void setRequester(Person requester) {
	this.requester = new DomainReference<Person>(requester);
    }

    public Person getRequester() {
	return requester.getObject();
    }

}
