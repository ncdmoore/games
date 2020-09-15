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
public class Ferry implements AirMission {
    private final Game game;

    @Getter private final AirMissionType type = AirMissionType.FERRY;
    @Getter private final int id;
    @Getter private final Nation nation;
    @Getter private final Map<MissionRole, List<Squadron>> squadronMap;

    private final Airbase startingAirbase;
    private final String endingAirbaseName;   //The name of the destination air base.
    private Target endingAirbase;             //The actual destination air base.

    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param game The game.
     */
    @Inject
    public Ferry(@Assisted final MissionData data,
                           final Game game) {
        id = data.getId();
        this.game = game;
        nation = data.getNation();

        startingAirbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        squadronMap = Optional
                .ofNullable(data.getSquadronMap())
                .orElseGet(Collections::emptyMap)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry
                        .getValue()
                        .stream()
                        .map(startingAirbase::getSquadron)
                        .collect(Collectors.toList())));

        //Note, we cannot go ahead and obtain the destination air base. as it might not have been created at
        //this point in time. So we just save the name of the destination air base. The destination air base
        // must be determined outside the constructor.
        endingAirbaseName = data.getTarget();
    }

    /**
     * Get the persistent Ferry data.
     *
     * @return The persistent Ferry data.
     */
    @Override
    public MissionData getData() {
        MissionData data = new MissionData();

        data.setId(id);
        data.setType(AirMissionType.FERRY);
        data.setNation(nation);
        data.setTarget(endingAirbaseName);

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
     * Get the airbase from which the mission was launched.
     *
     * @return The airbase from which the mission was launched.
     */
    @Override
    public Airbase getAirbase() {
        return startingAirbase;
    }

    /**
     * Get this mission's target.
     *
     * @return The mission's target.
     */
    @Override
    public Target getTarget() {
        return Optional.ofNullable(endingAirbase)
                .orElseGet(this::getEndingAirbase);
    }

    /**
     * Get the squadrons on the mission.
     *
     * @param role The squadron's mission role.
     * @return A list of squadrons on the mission.
     */
    @Override
    public List<Squadron> getSquadrons(final MissionRole role) {
        return squadronMap.get(role);
    }

    /**
     * Get both the squadrons on the mission and the squadrons on escort duty.
     * For this type of mission there are no escorts. So just the squadrons
     * are returned.
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
        getTarget();

        squadronMap
                .get(MissionRole.MAIN)
                .forEach(squadron -> {
                    squadron.setState(SquadronAction.ASSIGN_TO_MISSION);
                    squadron.equip(endingAirbase, AirMissionType.FERRY, MissionRole.MAIN);
                });
    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {
        squadronMap.get(MissionRole.MAIN).forEach(squadron -> {
            squadron.setState(SquadronAction.REMOVE_FROM_MISSION);
            squadron.unEquip();
        });

        squadronMap.get(MissionRole.MAIN).clear();
    }

    /**
     * Get the number of squadron in the mission.
     *
     * @return The number of squadrons in the mission.
     */
    @Override
    public int getNumber() {
        return squadronMap.get(MissionRole.MAIN).size();
    }

    /**
     * Ferry missions do not have a mission success probability.
     *
     * @return an empty list.
     */
    @Override
    public List<ProbabilityStats> getMissionProbability() {
        return Collections.emptyList();
    }

    /**
     * Get the destination or target air base.
     *
     * @return The destination air base.
     */
    private Target getEndingAirbase() {
        endingAirbase = game
                .getPlayer(startingAirbase.getSide())
                .getTargets(AirMissionType.FERRY, nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(endingAirbaseName))
                .findAny()
                .orElse(null);

        return endingAirbase;
    }
}
