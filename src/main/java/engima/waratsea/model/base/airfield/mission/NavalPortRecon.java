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
 * This class represents a naval port reconnaissance mission.
 */
@Slf4j
public class NavalPortRecon extends AirMissionExecutor implements AirMission {
    @Getter private final int id;
    @Getter private AirMissionState state;
    private final Game game;

    @Getter private final AirMissionType type = AirMissionType.NAVAL_PORT_RECON;
    @Getter private final Nation nation;
    @Getter private final Airbase airbase;
    @Getter private final MissionSquadrons squadrons;

    private final String targetName;               //The name of the port.
    private Target targetPort;                     //The actual port.
    private int startTurn;                         //The game turn on which the missions starts.
    private int turnsToTarget;                     //The turns until the mission reaches its target.
    private int turnsToHome;                       //The turns until the mission returns to its home airbase.
    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param squadrons The squadrons on this mission.
     * @param game The game.
     */
    @Inject
    public NavalPortRecon(@Assisted final MissionData data,
                          final MissionSquadrons squadrons,
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
        data.setType(AirMissionType.NAVAL_PORT_RECON);
        data.setNation(nation);
        data.setTarget(targetName);
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
     * Launch the mission. Squadrons take off.
     */
    @Override
    public void launch() {
        targetPort = getTarget();

        startTurn = game.getTurn().getNumber();

        int distance = targetPort.getDistance(airbase);
        int roundTrip = distance * 2;
        int minimumRange = squadrons.getMinimumRange();

        turnsToTarget = (distance / minimumRange) + (distance % minimumRange > 0 ? 1 : 0);
        turnsToHome = (roundTrip / minimumRange) + (roundTrip % minimumRange > 0 ? 1 : 0);

        squadrons.takeOff();
    }

    /**
     * Progress the mission forward.
     */
    @Override
    public void fly() {

    }

    /**
     * Execute the mission.
     */
    @Override
    public void execute() {
        targetPort = getTarget();

    }

    /**
     * Recall the mission.
     */
    @Override
    public void recall() {

    }

    /**
     * Land the mission. Squadrons land.
     */
    @Override
    public void land() {
        squadrons.land();
    }

    /**
     * Determine if the mission has reached its target.
     *
     * @return True if the mission has reached its target. False otherwise.
     */
    @Override
    public boolean reachedTarget() {
        // The + 1 accounts for the current turn.
        return getElapsedTurns() + 1 >= turnsToTarget;
    }

    /**
     * Determine if the mission has reached its home airbase.
     *
     * @return True if the mission has reached its home airbase. False otherwise.
     */
    @Override
    public boolean reachedHome() {
        // The + 1 accounts for the current turn.
        return getElapsedTurns() + 1 >= turnsToHome;
    }

    /**
     * Get the mission's target.
     *
     * @return The mission's target.
     */
    @Override
    public Target getTarget() {
        return Optional.ofNullable(targetPort)
                .orElseGet(this::getTargetPort);
    }

    /**
     * Set all of the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        getTarget();

        squadrons.add(targetPort, AirMissionType.NAVAL_PORT_RECON, id);
    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {
        squadrons.remove();
    }

    /**
     * Get the probability of success for this mission.
     *
     * @return The probability that this mission will be successful.
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
     * Get the target port.
     *
     * @return The target port.
     */
    private Target getTargetPort() {
        targetPort = game
                .getPlayer(airbase.getSide())
                .getTargets(AirMissionType.NAVAL_PORT_RECON, nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetName))
                .findAny()
                .orElse(null);

        return targetPort;
    }
}
