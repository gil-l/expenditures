package pt.ist.expenditureTrackingSystem.domain.acquisitions.activities;

import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessState;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;

public class RejectAcquisitionProcess extends GenericAcquisitionProcessActivity{

    @Override
    protected boolean isAccessible(AcquisitionProcess process) {
	User user = getUser();
	return process.isResponsibleForUnit() && user != null && !process.getAcquisitionRequest().hasBeenApprovedBy(user.getPerson());
    }

    @Override
    protected boolean isAvailable(AcquisitionProcess process) {
	return process.isPendingApproval();
    }

    @Override
    protected void process(AcquisitionProcess process, Object... objects) {
	new AcquisitionProcessState(process, AcquisitionProcessStateType.REJECTED);
    }

}
