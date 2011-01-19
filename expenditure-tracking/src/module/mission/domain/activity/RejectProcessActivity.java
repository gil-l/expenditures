package module.mission.domain.activity;

import module.mission.domain.MissionProcess;
import myorg.domain.User;
import myorg.util.BundleUtil;

public class RejectProcessActivity extends CancelProcessActivity {

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/MissionResources", "activity." + getClass().getSimpleName());
    }

    @Override
    public boolean isActive(final MissionProcess missionProcess, final User user) {
	return !missionProcess.isCanceled()
		// && missionProcess.isUnderConstruction()
		&& (missionProcess.canAuthoriseParticipantActivity()
			|| missionProcess.isPendingAuthorizationBy(user))
		;
    }

}
