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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class Ferry implements AirMission {
    private final Game game;

    @Getter
    private final Nation nation;
    private final Airbase startingAirbase;

    @Getter
    private final List<Squadron> squadrons;
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
        this.game = game;
        nation = data.getNation();

        startingAirbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        // The squadrons can be created here as they are guarranted to be already created by the air base.
        squadrons = Optional.ofNullable(data.getSquadrons())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(startingAirbase::getSquadron)
                .collect(Collectors.toList());

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

        data.setType(MissionType.FERRY);
        data.setNation(nation);
        data.setTarget(endingAirbaseName);

        List<String> names = Optional
                .ofNullable(squadrons)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());

        data.setSquadrons(names);

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
     * Get the mission's type.
     *
     * @return The type of mission.
     */
    @Override
    public MissionType getType() {
        return MissionType.getType(this);
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
     * Get the squadrons on this mission that are serving as escort.
     * Ferry mission do not have escorts.
     *
     * @return A list of squadrons on escort duty for this mission.
     */
    @Override
    public List<Squadron> getEscort() {
        return Collections.emptyList();
    }

    /**
     * Get the number of steps assigned to this mission.
     *
     * @return the total number of steps assigned to this mission.
     */
    @Override
    public int getSteps() {
        int steps = squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();

        log.info("Mission {} target {} steps {}", new Object[]{this.getClass().getSimpleName(), getTarget().getName(), steps});
        return steps;
    }

    /**
     * Set all of the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        squadrons.forEach(squadron -> {
            SquadronState state = squadron.getSquadronState().transition(SquadronAction.ASSIGN_TO_MISSION);
            squadron.setSquadronState(state);
        });
    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {
        squadrons.forEach(squadron -> {
            SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_MISSION);
            squadron.setSquadronState(state);
        });

        squadrons.clear();
    }

    /**
     * Get the number of squadron in the mission.
     *
     * @return The number of squadrons in the mission.
     */
    @Override
    public int getNumber() {
        return squadrons.size();
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
                .getFriendlyAirfieldTargets(nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(endingAirbaseName))
                .findAny()
                .orElse(null);

        return endingAirbase;
    }
}
