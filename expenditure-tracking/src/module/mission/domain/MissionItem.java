package module.mission.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import module.mission.domain.activity.DistributeItemCostsActivityInformation.MissionItemFinancerBean;
import module.mission.domain.activity.DistributeItemCostsActivityInformation.MissionItemFinancerBeanCollection;
import module.mission.domain.activity.ItemActivityInformation;
import module.organization.domain.Person;
import myorg.domain.util.Money;
import myorg.util.BundleUtil;

public abstract class MissionItem extends MissionItem_Base {

    public static final Comparator<MissionItem> COMPARATOR_BY_OID = new Comparator<MissionItem>() {

	@Override
	public int compare(final MissionItem missionItem1, final MissionItem missionItem2) {
	    return missionItem1.getExternalId().compareTo(missionItem2.getExternalId());
	}

    };

    public MissionItem() {
	super();
	setOjbConcreteClass(getClass().getName());
	setMissionSystem(MissionSystem.getInstance());
	new TemporaryMissionItemEntry(this);
    }

    @Deprecated
    public void setMission(final Mission mission) {
	setMissionVersion(mission);
    }

    public void setMissionVersion(final Mission mission) {
	setMissionVersion(mission.getMissionVersion());
    }

    @Override
    public void setMissionVersion(final MissionVersion missionVersion) {
	super.setMissionVersion(missionVersion);
	final TemporaryMissionItemEntry temporaryMissionItemEntry = getTemporaryMissionItemEntry();
	if (missionVersion != null && temporaryMissionItemEntry != null) {
	    temporaryMissionItemEntry.delete();
	}
    }

    public void delete() {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    missionItemFinancer.delete();
	}
	getPeopleSet().clear();
	removeMissionVersion();
	final TemporaryMissionItemEntry temporaryMissionItemEntry = getTemporaryMissionItemEntry();
	if (temporaryMissionItemEntry != null) {
	    temporaryMissionItemEntry.delete();
	}
	removeMissionSystem();
	deleteDomainObject();
    }

    private static final String BUNDLE = "resources.MissionResources";
    private static final String KEY_PREFIX = "label.";

    public String getLocalizedName() {
	final String key = KEY_PREFIX + getClass().getName();
	return BundleUtil.getStringFromResourceBundle(BUNDLE, key);
    }

    public void setMissionItemFinancers(final MissionItemFinancerBeanCollection missionItemFinancerBeans) {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    final MissionFinancer missionFinancer = missionItemFinancer.getMissionFinancer();
	    if (!missionItemFinancerBeans.containsWithValue(missionFinancer)) {
		missionItemFinancer.delete();
	    }
	}
	for (final MissionItemFinancerBean missionItemFinancerBean : missionItemFinancerBeans) {
	    setMissionItemFinancer(missionItemFinancerBean);
	}
    }

    public void setMissionItemFinancer(final MissionItemFinancerBean missionItemFinancerBean) {
	final MissionFinancer missionFinancer = missionItemFinancerBean.getMissionFinancer();
	final Money amount = missionItemFinancerBean.getAmount();
	if (amount != null && amount.isPositive()) {
	    final MissionItemFinancer missionItemFinancer = findOrCreateMissionItemFinancer(missionFinancer);
	    missionItemFinancer.setAmount(amount);
	}
    }

    private MissionItemFinancer findOrCreateMissionItemFinancer(final MissionFinancer missionFinancer) {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    if (missionFinancer == missionItemFinancer.getMissionFinancer()) {
		return missionItemFinancer;
	    }
	}
	return missionFinancer.isProjectFinancer() ? new MissionItemProjectFinancer(this, missionFinancer) : new MissionItemFinancer(this, missionFinancer);
    }

    public abstract String getItemDescription();

    public Money getPrevisionaryCosts() {
	return Money.ZERO;
    }

    public boolean isVehicleItem() {
	return false;
    }

    public Money getValue() {
	return Money.ZERO;
    }

    public boolean isPersonelExpenseItem() {
	return false;
    }

    public boolean isConsistent() {
	return true;
    }

    public void setInfo(final ItemActivityInformation itemActivityInformation) {
	final MissionProcess missionProcess = itemActivityInformation.getProcess();
	final Mission mission = missionProcess.getMission();

	setMission(mission);

	final Collection<Person> people = itemActivityInformation.getPeople();
	final Set<Person> participants = getPeopleSet();
	participants.addAll(people);
	participants.retainAll(people);
	if (mission.getParticipantesCount() == 1) {
	    participants.addAll(mission.getParticipantesSet());
	}
    }

    public void distributeCosts(final MissionItemFinancerBeanCollection missionItemFinancerBeanCollection) {
	if (missionItemFinancerBeanCollection.size() == 1) {
	    final MissionItemFinancerBean missionItemFinancerBean = missionItemFinancerBeanCollection.iterator().next();
	    missionItemFinancerBean.setAmount(getValue());
	}
	setMissionItemFinancers(missionItemFinancerBeanCollection);
    }

    public boolean areAllCostsDistributed() {
	Money sum = Money.ZERO;
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    sum = sum.add(missionItemFinancer.getAmount());
	}
	return sum.equals(getValue());
    }

    public void hookAfterChanges() {
    }

    public void distributeCosts() {
	final Money money = getValue();
	final Mission mission = getMissionVersion().getMission();
	int numberFinancers = mission.getFinancerSet().size();
	final Money shareValues[] = money.allocate(numberFinancers);
	int i = 0;
	for (final MissionFinancer missionFinancer : mission.getFinancerSet()) {
	    final MissionItemFinancer missionItemFinancer = findOrCreateMissionItemFinancer(missionFinancer);
	    missionItemFinancer.setAmount(shareValues[i++]);
	}
    }

    public boolean requiresFundAllocation() {
	return true;
    }

    MissionItem createNewVersion(final MissionVersion missionVersion) {
	final MissionItem missionItem = createNewVersionInstance(missionVersion);
	missionItem.setMissionVersion(missionVersion);
	missionItem.getPeopleSet().addAll(getPeopleSet());
	setNewVersionInformation(missionItem);
	return missionItem;
    }

    protected abstract MissionItem createNewVersionInstance(final MissionVersion missionVersion);

    protected abstract void setNewVersionInformation(final MissionItem missionItem);

    public boolean isAvailableForEdit() {
	final MissionVersion missionVersion = getMissionVersion();
	final Mission mission = missionVersion.getMission();
	final MissionProcess missionProcess = mission.getMissionProcess();
	return missionProcess.isUnderConstruction();
    }

    public void autoArchive() {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    if (canAutoArchive()) {
		missionItemFinancer.autoArchive();
	    }
	}
    }

    protected boolean canAutoArchive() {
	return true;
    }

    public boolean isArchived() {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    if (!missionItemFinancer.isArchived()) {
		return false;
	    }
	}
	return true;
    }

    public void archive() {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    missionItemFinancer.archiveForAccountingUnit();
	}
    }

    public boolean isAccountantForUnArchivedMissionItemFinancer() {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    if (missionItemFinancer.isAccountantForUnArchivedMissionItemFinancer()) {
		return true;
	    }
	}
	return false;
    }

    public boolean isDirectAccountantForUnArchivedMissionItemFinancer() {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    if (missionItemFinancer.isDirectAccountantForUnArchivedMissionItemFinancer()) {
		return true;
	    }
	}
	return false;
    }

    public void unArchive() {
	for (final MissionItemFinancer missionItemFinancer : getMissionItemFinancersSet()) {
	    missionItemFinancer.unArchive();
	}
    }

}
