package engima.waratsea.presenter.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.MissionType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.WarnDialog;
import engima.waratsea.view.airfield.mission.MissionDetailsView;
import engima.waratsea.view.airfield.mission.MissionView;
import lombok.Setter;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * This class is a utility class for the mission add and mission edit dialog classes.
 * These two classes share some logic in this class.
 */
public class MissionDetails {
    private final Provider<WarnDialog> warnDialogProvider;

    @Setter
    private Airbase airbase;

    @Setter
    private Nation nation;

    @Setter
    private Target selectedTarget;

    @Setter
    private MissionDetailsView view;

    @Setter
    private Map<Nation, MissionView> missionView;

    /**
     * Constructor called by guice.
     *
     * @param warnDialogProvider Provides warning dialogs.
     */
    @Inject
    public MissionDetails(final Provider<WarnDialog> warnDialogProvider) {
        this.warnDialogProvider = warnDialogProvider;
    }

    /**
     * Determine if the selected target is at max squadron step capacity.
     *
     * @return True if the target has capacity for more squadron steps. False otherwise.
     */
    public boolean hasCapacity() {
        return selectedTarget.hasAirbaseCapacity(airbase, getMissionSteps(selectedTarget));
    }

    /**
     * Determine if the selected target's region is at max squadron step capacity.
     *
     * @return True if the target's region has capacity for more squadron steps. False otherwise.
     */
    public boolean hasRegionCapacity() {
        return selectedTarget.hasRegionCapacity(nation, airbase, getMissionSteps(selectedTarget));
    }

    /**
     * Get the currently selected squadron from the available squadron list.
     *
     * @return The currently selected available squadron.
     */
    public Optional<Squadron> getSelectedAvailableSquadron() {
        return Optional.ofNullable(
                view.getMissionList()
                .getAvailable()
                .getSelectionModel()
                .getSelectedItem());
    }

    /**
     * Get the currently selected squadron from the assigned squadron list.
     *
     * @return The currently selected assigned squadron.
     */
    public Optional<Squadron> getSelectedAssignedSquadron() {
        return Optional.ofNullable(
                view.getMissionList()
                .getAssigned()
                .getSelectionModel()
                .getSelectedItem());
    }

    /**
     * Update the target view.
     *
     * @param mission The current mission.
     * @param selectedMissionType The mission type.
     */
    public void updateTargetView(@Nullable final AirMission mission, final MissionType selectedMissionType) {
        Optional.ofNullable(selectedTarget).ifPresent(target -> {
            int inRouteStepsFromThisAirbase = getMissionSteps(mission, target);
            int inRouteStepsFromOtherAirbases = target.getMissionSteps(airbase);
            int inRouteSteps =  inRouteStepsFromOtherAirbases + inRouteStepsFromThisAirbase;

            int inRouteRegionStepsFromThisAirbase = getMissionStepsEnteringRegion(mission, selectedMissionType, selectedTarget);
            int inRouteRegionStepsFromOtherAirbases = target.getMissionStepsEnteringRegion(selectedMissionType, nation, airbase);
            int inRouteRegionSteps = inRouteRegionStepsFromOtherAirbases + inRouteRegionStepsFromThisAirbase;

            int outRouteRegionFromThisAirbase = getMissionStepsLeavingRegion(mission, selectedMissionType);
            int outRouteRegionFromOtherAirbases = target.getMissionStepsLeavingRegion(selectedMissionType, nation, airbase);
            int outRouteRegionSteps = outRouteRegionFromThisAirbase + outRouteRegionFromOtherAirbases;

            TargetStats targetStats = new TargetStats(target, airbase, inRouteSteps);
            RegionStats targetRegionStats = new RegionStats(nation, target, inRouteRegionSteps);
            RegionStats airbaseRegionStats = new RegionStats(nation, airbase, outRouteRegionSteps);
            SuccessStats successStats = new SuccessStats(selectedMissionType, nation, airbase, view.getMissionList().getAssigned().getItems(), target);

            MissionStats missionStats = new MissionStats();
            missionStats.setMissionType(selectedMissionType);
            missionStats.setTargetStats(targetStats);
            missionStats.setTargetRegionStats(targetRegionStats);
            missionStats.setAirfieldRegionStats(airbaseRegionStats);
            missionStats.setSuccessStats(successStats);

            view.getTargetView().show(missionStats);
        });
    }

    /**
     * Determine if the squadron may be added to the current mission.
     *
     * @param selectedMission The selected mission.
     * @param missionType The mission type.
     * @param squadron The proposed squadron to add.
     * @return True if the squadron may be added. False otherwise.
     */
    public boolean mayAddSquadronToMission(@Nullable final AirMission selectedMission, final MissionType missionType, final Squadron squadron) {
        //Note, that all nations that are allowed to specify the selected target as a target are considered
        //when determining if target has capacity.
        if (!hasCapacity(selectedMission, squadron)) {
            warnDialogProvider.get().show("Unable to assign squadron to the target. Target: " + selectedTarget.getTitle()
                    + ", is at capacity: " + selectedTarget.getCapacitySteps());
            return false;
        }

        //Note, regions are per nation, so only the squadrons for the selected nation are considered when
        //determining if the target's region has capacity.
        if (!hasRegionCapacity(selectedMission, missionType, squadron)) {
            warnDialogProvider.get().show("Unable to assign squadron to the target. Target: " + selectedTarget.getTitle()
                    + ", region is at capacity: " + selectedTarget.getRegion(nation).getMaxSteps());
            return false;
        }

        //Note, regions are per nation, so only the squadrons for the selected nation are considered when
        //determining if the selected airbase's region will still satify its minimum squadron requirement.
        if (!isRegionMinimumStillSatisfied(selectedMission, missionType, squadron)) {
            warnDialogProvider.get().show("Unable to assign squadron as region: " + airbase.getRegion(nation).getName()
                    + ", minimum required steps: " + airbase.getRegion(nation).getMinSteps() + ", would be violated.");
            return false;
        }

        return true;
    }

    /**
     * Add a squadron to the current mission under construction.
     *
     * @param squadron The added squadron.
     */
    public void addSquadron(final Squadron squadron) {
        view.assign(squadron);
    }

    /**
     * Remove a squadron from the current mission under construction.
     */
    public void removeSquadron() {
        view.remove();
    }

    /**
     * Determine if the selected target has capacity for the given squadron. This really only comes into
     * play for ferry missions to friendly airbase targets.
     *
     * @param mission The selected mission.
     * @param squadron The squadron to ferry.
     * @return True if the squadron may be ferried. False otherwise.
     */
    private boolean hasCapacity(final AirMission mission, final Squadron squadron) {
        //Get the total number of squadron steps currently assigned to the mission under construction.
        int totalStepsCurrentMission = getCurrentMissionSteps();

        //Get the total number of squadron steps from other missions originating from the selected
        //airbase that are assigned to this target. This is the total number of squadron steps
        //for all nations at the selected airbase.
        int totalStepsOtherMissions = getOtherMissionSteps(mission, selectedTarget);

        //Total squadron steps from this airbase that are assigned missions with the selected target
        //as the target.
        int totalMissionSquadronSteps = totalStepsCurrentMission + totalStepsOtherMissions + squadron.getSteps().intValue();

        // The target will consider all the other airbases and the total mission squadron steps from this
        // airbase and determine if the target has capacity.
        return selectedTarget.hasAirbaseCapacity(airbase, totalMissionSquadronSteps);
    }

    /**
     * Determine if the selected target's region has capacity for the given squadron. This really only comes
     * into play for ferry missions to friendly airbase targets.
     *
     * @param mission The selected mission.
     * @param missionType The mission type.
     * @param squadron The squadron to ferry.
     * @return True if the squadron may be ferried. False otherwise.
     */
    private boolean hasRegionCapacity(@Nullable final AirMission mission, final MissionType missionType, final Squadron squadron) {
        //Get the total number of squadron steps currently assigned to the mission under construction.
        int totalStepsCurrentMission = getCurrentMissionStepsEnteringRegion(selectedTarget);

        //Get the total number of squadron steps from other missions originating from the selected
        //airbase that are assigned this target's region. Note, the mission type of the other missions
        //must match the mission type of the mission under construction.
        int totalStepsOtherMissions = getOtherMissionStepsEnteringRegion(mission, missionType, selectedTarget);

        //Total squadron steps from this airbase that are assigned missions with the selected target's
        //region
        int totalMissionSquadronSteps = totalStepsCurrentMission + totalStepsOtherMissions + squadron.getSteps().intValue();

        //The target will consider all the other airbases and the total mission squadrons steps from this airbase
        //to the target's region and determine if the target's region has capacity.
        return selectedTarget.hasRegionCapacity(nation, airbase, totalMissionSquadronSteps);
    }

    /**
     * Determine if assigning the given squadron to the mission would violate the mission's origin airbase's
     * minimum squadron step requirement.
     *
     * Currently this method only has meaning for Ferry missions to friendly airbase targets.
     *
     * @param mission The selected mission.
     * @param missionType The mission type.
     * @param squadron The squadron to add to the mission.
     * @return True if assigning the given squadron will still satisfy the minimum squadron step requirements
     * of the airbase that is creating the mission, or if the current selected target is not contained within
     * a region or just doesn't dependent upon its region then true is returned. False otherwise.
     */
    private boolean isRegionMinimumStillSatisfied(@Nullable final AirMission mission, final MissionType missionType, final Squadron squadron) {
        Region missionOriginRegion = airbase.getRegion(nation);             // All airbase's should have a region.
        Region missionDestinationRegion = selectedTarget.getRegion(nation); // Not all targets are in a region.

        //Get the total number of squadron steps currently assigned to the mission under construction
        //that are leaving the current airbase's region. Note, that  if the current mission is within
        //a region (origin airbase's region and target's region are equal) this number is zero.
        int totalStepsCurrentMission = getCurrentMissionStepsLeavingRegion(mission);

        //Get all of this airbase's missions of the same type that are leaving the current airbase's region.
        int totalStepsOtherMissions = getOtherMissionStepsLeavingRegion(mission, missionType);

        //This is the total of all the squadron steps already added to the current mission and all other missions of
        //the same type and the squadron that is attempting to be assigned to the current mission.
        int totalMissionSquadronSteps = totalStepsCurrentMission + totalStepsOtherMissions + squadron.getSteps().intValue();

        //Total squadron steps that are leaving the airbase's region for the entire side.
        int totalSteps = totalMissionSquadronSteps + selectedTarget.getMissionStepsLeavingRegion(missionType, nation, airbase);

        //If the target has no region then this method just returns true.
        return Optional
                .ofNullable(missionDestinationRegion)
                .filter(destRegion -> destRegion != missionOriginRegion)
                .map(dummy -> missionOriginRegion.minimumSatisfied(totalSteps))
                .orElse(true);
    }

    /**
     * Get the steps in route to the target from this airbase. This includes the mission under construction as well
     * as all other missions for all nations at the selected airbase.
     *
     * @param selectedMission The selected mission.
     * @param target The selected target.
     * @return The squadron steps in route to the target from this airbase for all nations.
     */
    private int getMissionSteps(@Nullable final AirMission selectedMission, final Target target) {
        //The current mission steps may change as squadrons are added and deleted so
        //it must be calculated separately from the other missions.
        return getCurrentMissionSteps() + getOtherMissionSteps(selectedMission, target);
    }

    /**
     * Get the mission steps in route to the target's region steps. This includes the mission under construction as
     * well as all other missions.
     *
     * @param selectedMission The selected mission.
     * @param missionType The mission type.
     * @param target The selected target.
     * @return The squadron steps.
     */
    private int getMissionStepsEnteringRegion(@Nullable final AirMission selectedMission, final MissionType missionType, final Target target) {
        //The current mission steps may change as squadrons are added and deleted so
        //it must be calculated separately from the other missions.
        return getCurrentMissionStepsEnteringRegion(target) + getOtherMissionStepsEnteringRegion(selectedMission, missionType, target);
    }

    /**
     * Get the mission squadron steps leaving the current airfield's region.
     *
     * @param selectedMission The selected mission.
     * @param missionType The type of mission.
     * @return The total number of mission squadron steps leaving the current airfield's region.
     */
    private int getMissionStepsLeavingRegion(@Nullable final AirMission selectedMission, final MissionType missionType) {
        //The current mission steps may change as squadrons are added and deleted so
        //it must be calculated separately from the other missions.
        return getCurrentMissionStepsLeavingRegion(selectedMission) + getOtherMissionStepsLeavingRegion(selectedMission, missionType);
    }

    /**
     * Get the steps in route to the target from this airbase's mission table.
     * This is all missions from the selected airbase to the given target.
     * This is a convenience method for adding missions. It is only used when adding a new mission.
     *
     * @param target The airbase's mission's target.
     * @return The number of steps in route (on missions) to this target from this airbase.
     */
    private int getMissionSteps(final Target target) {
        return missionView
                .get(nation)
                .getTable()
                .getItems()
                .stream()
                .filter(mission -> mission.getTarget().isEqual(target))
                .map(AirMission::getSteps)
                .reduce(0, Integer::sum);
    }

    /**
     * Get the total squadron steps currently assigned to the mission under construction. This is the total
     * squadron steps of all the squadrons currently included in the mission assigned list.
     *
     * @return The total squadron steps currently assigned to the mission under construction.
     */
    private int getCurrentMissionSteps() {
        return view
                .getMissionList()
                .getAssigned()
                .getItems()
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }

    /**
     * Get the steps in route to the target from this airbase's mission excluding the mission
     * under construction. For adds this selected mission will be null. Note, that this method
     * returns all steps in route to the given target for all nations.
     *
     * @param selectedMission The selected mission.
     * @param target The airbase's mission's target.
     * @return The number of steps in route (on missions) to this target from this airbase
     * excluding the mission under construction for all nations.
     */
    private int getOtherMissionSteps(@Nullable final AirMission selectedMission, final Target target) {
        return missionView
                .values()
                .stream()
                .map(mv -> mv.getTable()
                            .getItems()
                            .stream()
                            .filter(mission -> mission != selectedMission)
                            .filter(mission -> mission.getTarget().isEqual(target))
                            .map(AirMission::getSteps)
                            .reduce(0, Integer::sum))
                .reduce(0, Integer::sum);

    }

    /**
     * Get the squadron steps that enter a different region other than the selected airbase for the current
     * mission under construction. This value will always be either the sum of the steps of all
     * the squadrons assigned the current mission or 0. It simply depends upon whether the airbase's
     * region is the same region as the target's region. If they are the same region then 0 is returned.
     *
     * @param target The selected target.
     * @return The number of steps that will enter the target's region.
     */
    private int getCurrentMissionStepsEnteringRegion(final Target target) {
        return (airbase.getRegion(nation) != target.getRegion(nation)) ? getCurrentMissionSteps() : 0;
    }

    /**
     * Get the mission steps that enter the target's region excluding the current mission under construction.
     *
     * @param selectedMission The selected mission.
     * @param missionType The mission type.
     * @param target The selected target.
     * @return The squadron mission steps that enter the target's region excluding the current mission.
     */
    private int getOtherMissionStepsEnteringRegion(@Nullable final AirMission selectedMission, final MissionType missionType, final Target target) {
        return missionView
                .get(nation)
                .getTable()
                .getItems()
                .stream()
                .filter(mission -> mission != selectedMission)
                .filter(mission -> mission.getType() == missionType)
                .filter(mission -> mission.getAirbase().getRegion(nation) != target.getRegion(nation))
                .filter(mission -> mission.getTarget().getRegion(nation) == target.getRegion(nation))
                .map(AirMission::getSteps)
                .reduce(0, Integer::sum);
    }

    /**
     * Get the total squadron steps currently assigned to the mission under construction that are
     * leaving the region of the origin airbase.
     *
     * @param selectedMission The selected mission.
     * @return The total mission squadron steps of the current mission leaving the region of the selected airbase.
     */
    private int getCurrentMissionStepsLeavingRegion(@Nullable final AirMission selectedMission) {
        return Optional.ofNullable(selectedMission).map(mission -> {
            Airbase originAirbase = selectedMission.getAirbase();
            Target target = selectedMission.getTarget();

            return (originAirbase.getRegion(nation) != target.getRegion(nation)) ? getCurrentMissionSteps() : 0;
        }).orElse(0);
    }


    /**
     * Get the total squadron steps currently assigned to missions that are leaving the region of the origin airbase,
     * excluding the current mission under construction.
     *
     * @param selectedMission The mission.
     * @param missionType The mission type.
     * @return The total number of steps in this airbase's mission table that are assigned
     * the given type of mission.
     */
    private int getOtherMissionStepsLeavingRegion(@Nullable final AirMission selectedMission, final MissionType missionType) {
        return missionView
                .get(nation)
                .getTable()
                .getItems()
                .stream()
                .filter(mission -> mission != selectedMission)
                .filter(mission -> mission.getType() == missionType)
                .filter(mission -> mission.getAirbase().getRegion(nation) != mission.getTarget().getRegion(nation))
                .map(AirMission::getSteps)
                .reduce(0, Integer::sum);
    }
}

