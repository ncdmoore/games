package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.state.AirMissionAction;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class NavalTaskForceStrike implements AirMission {
    @Getter private final int id;
    @Getter private AirMissionState state;
    private final Game game;

    @Getter private final AirMissionType type = AirMissionType.NAVAL_TASK_FORCE_STRIKE;
    @Getter private final Nation nation;
    @Getter private final Airbase airbase;
    @Getter private final Squadrons squadrons;

    private final String targetName;               //The name of the target task force.
    private Target targetTaskForce;                //The actual target task force.

    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param squadrons The squadrons on this mission.
     * @param game The game.
     */
    @Inject
    public NavalTaskForceStrike(@Assisted final MissionData data,
                                          final Squadrons squadrons,
                                          final Game game) {
        id = data.getId();
        state = Optional.ofNullable(data.getState()).orElse(AirMissionState.READY);
        this.squadrons = squadrons;
        this.game = game;
        nation = data.getNation();

        airbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        squadrons.setSquadrons(airbase, data.getSquadronMap());

        //Note, we cannot go ahead and obtain the target task force as it might not have been created at
        //this point in time. So we just save the name of the target task force. The target task force
        // must be determined outside the constructor.
        targetName = data.getTarget();

    }
    /**
     * Get the persistent mission data.
     *
     * @return The persistent mission data.
     */
    @Override
    public MissionData getData() {
        MissionData data = new MissionData();

        data.setId(id);
        data.setState(state);
        data.setType(AirMissionType.NAVAL_TASK_FORCE_STRIKE);
        data.setNation(nation);
        data.setTarget(targetName);
        data.setSquadronMap(squadrons.getData());

        return data;
    }

    /**
     * Set the air mission's current state.
     *
     * @param action The air mission action.
     */
    @Override
    public void setState(final AirMissionAction action) {
        state = state.transition(action);
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }

    /**
     * Get the mission's target.
     *
     * @return The mission's target.
     */
    @Override
    public Target getTarget() {
        return Optional.ofNullable(targetTaskForce)
                .orElseGet(this::getTargetTaskForce);
    }

    /**
     * Get the squadrons on the mission.
     *
     * @param role The squadron's mission role.
     * @return A list of squadrons on the mission.
     */
    @Override
    public List<Squadron> getSquadrons(final MissionRole role) {
        return squadrons.get(role);
    }

    /**
     * Get both the squadrons on the mission and the squadrons on escort duty.
     *
     * @return All of the squadrons involved with this mission.
     */
    @Override
    public List<Squadron> getSquadronsAllRoles() {
        return squadrons.getAllRoles();
    }

    /**
     * Get a map of mission role to list of squadrons performing that role for this mission.
     *
     * @return A map of mission role to list of squadron performing tht role.
     */
    @Override
    public Map<MissionRole, List<Squadron>> getSquadronMap() {
        return squadrons.getSquadronMap();
    }

    /**
     * Get the number of steps assigned to this mission.
     *
     * @return the total number of steps assigned to this mission.
     */
    @Override
    public int getSteps() {
        return squadrons.getSteps();
    }

    /**
     * Set all of the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        getTarget();

        squadrons.add(targetTaskForce, AirMissionType.NAVAL_TASK_FORCE_STRIKE);
    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {
        squadrons.remove();
    }

    /**
     * Get the number of squadron in the mission.
     *
     * @return The number of squadrons in the mission.
     */
    @Override
    public int getNumber() {
        return squadrons.getNumber();
    }

    /**
     * Get the probability of success for this mission.
     *
     * @return The probability that this mission will be successful.
     */
    @Override
    public List<ProbabilityStats> getMissionProbability() {
        return null;
    }

    /**
     * Determine if the mission is adversely affected by the current weather conditions.
     *
     * @return True if the mission is affected by the current weather conditions. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return false;
    }

    /**
     * Get the target task force.
     *
     * @return The target task force.
     */
    private Target getTargetTaskForce() {
        targetTaskForce = game
                .getPlayer(airbase.getSide())
                .getTargets(AirMissionType.NAVAL_TASK_FORCE_STRIKE, nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetName))
                .findAny()
                .orElse(null);

        return targetTaskForce;
    }
}
