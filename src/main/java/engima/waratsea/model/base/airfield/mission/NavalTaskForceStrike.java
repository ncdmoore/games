package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class NavalTaskForceStrike implements AirMission {
    private final Game game;

    @Getter
    private final Nation nation;

    @Getter
    private final Airbase airbase;

    private final Map<MissionRole, List<Squadron>> squadronMap;

    private final String targetName;               //The name of the target task force.
    private Target targetTaskForce;                //The actual target task force.

    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param game The game.
     */
    @Inject
    public NavalTaskForceStrike(@Assisted final MissionData data,
                                          final Game game) {
        this.game = game;
        nation = data.getNation();

        airbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        squadronMap = Optional
                .ofNullable(data.getSquadronMap())
                .orElseGet(Collections::emptyMap)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry
                        .getValue()
                        .stream()
                        .map(airbase::getSquadron)
                        .collect(Collectors.toList())));

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

        data.setType(AirMissionType.NAVAL_TASK_FORCE_STRIKE);
        data.setNation(nation);
        data.setTarget(targetName);

        data.setSquadronMap(squadronMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry
                        .getValue()
                        .stream()
                        .map(Squadron::getName)
                        .collect(Collectors.toList()))));

        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }

    /**
     * Get the mission's type.
     *
     * @return The type of mission.
     */
    @Override
    public AirMissionType getType() {
        return AirMissionType.getType(this);
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
        return squadronMap.getOrDefault(role, Collections.emptyList());
    }

    /**
     * Get both the squadrons on the mission and the squadrons on escort duty.
     *
     * @return All of the squadrons involved with this mission.
     */
    @Override
    public List<Squadron> getSquadronsAllRoles() {
        return Stream
                .of(MissionRole.values())
                .flatMap(role -> squadronMap.get(role).stream())
                .collect(Collectors.toList());
    }

    /**
     * Get the number of steps assigned to this mission.
     *
     * @return the total number of steps assigned to this mission.
     */
    @Override
    public int getSteps() {
        return Stream
                .of(MissionRole.values())
                .flatMap(role -> squadronMap.get(role).stream())
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }

    /**
     * Set all of the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        getSquadronsAllRoles()
                .forEach(squadron -> {
                    SquadronState state = squadron.getSquadronState().transition(SquadronAction.ASSIGN_TO_MISSION);
                    squadron.setSquadronState(state);
                    equipWithDropTanks(squadron); // Automatically equip squadron with drop tanks if needed to reach target.
                });
    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {
        getSquadronsAllRoles()
                .forEach(squadron -> {
                    SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_MISSION);
                    squadron.setSquadronState(state);
                    squadron.removeDropTanks();
                });

        getSquadronsAllRoles().clear();
    }

    /**
     * Get the number of squadron in the mission.
     *
     * @return The number of squadrons in the mission.
     */
    @Override
    public int getNumber() {
        return Stream
                .of(MissionRole.values())
                .map(role -> squadronMap.get(role).size())
                .reduce(0, Integer::sum);
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
     * Get the target task force.
     *
     * @return The target task force.
     */
    private Target getTargetTaskForce() {
        targetTaskForce = game
                .getPlayer(airbase.getSide())
                .getEnemyTaskForceTargets()
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetName))
                .findAny()
                .orElse(null);

        return targetTaskForce;
    }

    /**
     * Equip a squadron on this mission with drop tanks if it requires drop tanks in order to
     * reach the target. If the given squadron can reach the target without drop tanks then
     * do not equip.
     *
     * @param squadron The target that may be equipped with drop tanks.
     */
    private void equipWithDropTanks(final Squadron squadron) {
        if (targetTaskForce.inRangeWithoutDropTanks(squadron)) {
            return;                                                 // Drop tanks are not needed.
        }

        if (targetTaskForce.inRange(squadron)) {
            squadron.equipWithDropTanks();                          // Drop tanks are needed.
            return;
        }

        log.error("This squadron: '{}' is not in range of target: '{}'", squadron.getTitle(), targetTaskForce.getTitle());
    }
}
