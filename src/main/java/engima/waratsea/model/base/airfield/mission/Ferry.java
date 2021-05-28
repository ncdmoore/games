package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.state.AirMissionAction;
import engima.waratsea.model.base.airfield.mission.state.AirMissionExecutor;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The ferry air mission.
 */
@Slf4j
public class Ferry extends AirMissionExecutor implements AirMission  {
    private final Game game;

    @Getter private final AirMissionType type = AirMissionType.FERRY;
    @Getter private AirMissionState state;
    @Getter private final int id;
    @Getter private final Nation nation;

    private final Airbase startingAirbase;     //The mission's starting airbase.
    private String endingAirbaseName;          //The name of the destination air base.
    private Target endingAirbase;              //The actual destination air base.
    private final AirMissionPath missionPath;  //The grid path of the mission.
    @Getter private final MissionSquadrons squadrons;
    private int range;
    private int startTurn;                     //The game turn on which the mission starts.
    private int turnsToTarget;                 //How many game turns elapse before the mission lands at the ending airbase.


    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param game The game.
     * @param squadrons The squadron on this mission.
     * @param missionPath The mission's path.
     */
    @Inject
    public Ferry(@Assisted final MissionData data,
                           final Game game,
                           final MissionSquadrons squadrons,
                           final AirMissionPath missionPath) {
        id = data.getId();

        state = Optional
                .ofNullable(data.getState())
                .orElse(AirMissionState.READY);

        this.game = game;
        this.missionPath = missionPath;
        nation = data.getNation();
        this.squadrons = squadrons;

        startingAirbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        squadrons.setSquadrons(startingAirbase, data.getSquadronMap());

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
        data.setState(state);
        data.setType(AirMissionType.FERRY);
        data.setNation(nation);
        data.setTarget(endingAirbaseName);
        data.setSquadronMap(squadrons.getData());

        return data;
    }

    /**
     * Muster the mission. Prepare for launching.
     * @param action The action.
     */
    @Override
    public void doAction(final AirMissionAction action) {
        state = state.transition(action, this);
    }

    /**
     * Save the game turn when this mission launched.
     */
    @Override
    public void launch() {
        endingAirbase = getTarget();

        startTurn = game.getTurn().getNumber();

        range = squadrons.getMinimumRange();   // The mission moves the minimum range this turn.

        int distance = endingAirbase.getDistance(startingAirbase);
        turnsToTarget = getTurnsToTarget(distance);

        missionPath.build(startingAirbase, endingAirbase);

        squadrons.takeOff();

        missionPath.start();
    }

    /**
     * Progress the mission forward.
     */
    @Override
    public void fly() {





        // get enemy airfields that have CAP and one of the traversed grids is a CAP grid. Get the best grid for CAP intercept for the airfield/taskforce.
        //    in the future will need to account for cap mission zones.
        // for each airfield do cap intercept.
    }

    /**
     * Execute the mission. Ferry mission do not do anything until they land.
     */
    @Override
    public void execute() {
    }

    /**
     * Recall the mission. The mission will now land at the original airbase. The ferry is cancelled.
     */
    @Override
    public void recall() {
        // Set the grid path to be the grids already traversed, but in reverse order.
        // The squadrons are flying back to their original starting airbase.
        missionPath.recall(state);

        // Set the new ending airbase to the original starting airbase.
        endingAirbaseName = startingAirbase.getName();
        endingAirbase = getEndingAirbase(endingAirbaseName);

        // Get the distance to the original airbase from current grid.
        int distance = missionPath.getDistanceToEnd();

        // The turns to target is updated to reflect the turns to reach the original airbase.
        turnsToTarget = getTurnsToTarget(distance);
    }

    /**
     * Land the mission. Squadrons land.
     */
    @Override
    public void land() {
        squadrons.getAll().forEach(startingAirbase::removeSquadron);   // Remove the squadrons from their old airbase.
        endingAirbase = getTarget();
        endingAirbase.land(squadrons);
        missionPath.end();                                             // The mission has now landed at its new home airbase.
    }

    /**
     * Determine if the mission has reached its target.
     *
     * @return True if the mission has reached its target.
     */
    public boolean reachedTarget() {
        // The + 1 accounts for the current turn.
        return getElapsedTurns() + 1 >= turnsToTarget;
    }

    /**
     * Determine if the mission has returned home. Ferry missions do not return home.
     * The target destination is their new home.
     *
     * @return True if the mission has returned to its home airbase.
     */
    public boolean reachedHome() {
        return reachedTarget();
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
                .orElseGet(() -> getEndingAirbase(endingAirbaseName));
    }

    /**
     * Set all of the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        endingAirbase = getTarget();
        squadrons.add(endingAirbase, AirMissionType.FERRY);
    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {
        squadrons.remove();
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
     * Determine if the mission is adversely affected by the current weather conditions.
     *
     * @return True if the mission is affected by the current weather conditions. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return false;
    }

    /**
     * Get the number of elapsed turns. This is the number of turns for which this mission
     * has been in flight.
     *
     * @return The mission's elapsed turns.
     */
    @Override
    public int getElapsedTurns() {
        // If the start turn is 0, this means the mission has not launched yet; thus, elapsed time is 0.
        return startTurn == 0 ? 0 : game.getTurn().getNumber() - startTurn;
    }

    /**
     * Get the destination or target air base.
     *
     * @param airbaseName The ending airbase's name.
     * @return The destination air base.
     */
    private Target getEndingAirbase(final String airbaseName) {
        endingAirbase = game
                .getPlayer(startingAirbase.getSide())
                .getTargets(AirMissionType.FERRY, nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(airbaseName))
                .findAny()
                .orElse(null);

        return endingAirbase;
    }

    private int getTurnsToTarget(final int distance) {
        return (distance / range) + (distance % range > 0 ? 1 : 0);
    }

}
