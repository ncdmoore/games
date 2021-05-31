package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.Cap;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.path.AirMissionPath;
import engima.waratsea.model.base.airfield.mission.path.AirMissionPathDAO;
import engima.waratsea.model.base.airfield.mission.path.data.AirMissionPathData;
import engima.waratsea.model.base.airfield.mission.rules.MissionAirRules;
import engima.waratsea.model.base.airfield.mission.state.AirMissionAction;
import engima.waratsea.model.base.airfield.mission.state.AirMissionExecutor;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.Dice;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DistantCap extends AirMissionExecutor implements AirMission, Cap {

    @Getter private final int id;
    @Getter private AirMissionState state;
    private final Game game;
    private final Dice dice;
    private final MissionAirRules rules;

    @Getter private final AirMissionType type = AirMissionType.DISTANT_CAP;
    @Getter private final Nation nation;
    @Getter private final Airbase airbase;
    @Getter private final MissionSquadrons squadrons;

    private final String targetName;               //The name of the target task force.
    private final AirMissionPath missionPath;      //The grid path of the mission.
    private Target targetTaskForce;                //The actual target task force.
    private int range;
    private int startTurn;                         //The game turn on which the mission starts.
    private int turnsToTarget;                     //How many turns it takes to reach the target.
    private int turnsToHome;                       //How many turns it takes to return to the starting airbase.

    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param squadrons The squadrons on this mission.
     * @param game The game.
     * @param rules The mission air rules.
     * @param missionPathDAO The air mission path data abstraction object.
     * @param dice The dice utility.
     */
    @Inject
    public DistantCap(@Assisted final MissionData data,
                      final MissionSquadrons squadrons,
                      final Game game,
                      final @Named("airStrike") MissionAirRules rules,
                      final AirMissionPathDAO missionPathDAO,
                      final Dice dice) {
        id = data.getId();

        state = Optional
                .ofNullable(data.getState())
                .orElse(AirMissionState.READY);

        this.squadrons = squadrons;
        this.game = game;
        this.dice = dice;
        this.rules = rules;

        nation = data.getNation();

        airbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        //Note, we cannot go ahead and obtain the target task force as it might not have been created at
        //this point in time. So we just save the name of the target task force. The target air task force
        // must be determined outside the constructor.
        targetName = data.getTarget();

        squadrons.setSquadrons(airbase, data.getSquadronMap());

        AirMissionPathData pathData = Optional
                .ofNullable(data.getAirMissionPathData())
                .orElseGet(AirMissionPathData::new);

        pathData.setType(AirMissionType.DISTANT_CAP);

        this.missionPath = missionPathDAO.load(pathData);
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
        data.setType(AirMissionType.DISTANT_CAP);
        data.setNation(nation);
        data.setTarget(targetName);
        data.setSquadronMap(squadrons.getData());
        data.setAirMissionPathData(missionPath.getData());

        return data;
    }

    /**
     * This CAP intercepts enemy squadrons.
     *
     * @param enemySquadrons The enemy squadrons that are intercepted.
     */
    @Override
    public void intercept(final MissionSquadrons enemySquadrons) {

    }

    /**
     * Instruct the mission to carry out the given action.
     *
     * @param action The action that is done.
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
        targetTaskForce = getTarget();

        startTurn = game.getTurn().getNumber();

        range = squadrons.getMinimumRange();

        int distance = targetTaskForce.getDistance(airbase);
        int roundTrip = distance * 2;

        turnsToTarget = getTurnsToDistance(distance);
        turnsToHome = getTurnsToDistance(roundTrip);

        missionPath.start(airbase, targetTaskForce);
        squadrons.takeOff();
    }

    /**
     * Progress the mission forward.
     */
    @Override
    public void fly() {
        missionPath.progress(range);

    }

    /**
     * Execute the mission. The squadron have arrived at the target.
     */
    @Override
    public void execute() {
        targetTaskForce = getTarget();

        // Go on patrol for a single turn.
        targetTaskForce.patrol(PatrolType.CAP, squadrons.getAll());
    }

    /**
     * Recall the mission.
     */
    @Override
    public void recall() {
        missionPath.recall(state);

        // Get the distance to the original airbase from current grid.
        int distance = missionPath.getDistanceToEnd();

        turnsToHome = getTurnsToDistance(distance);
        turnsToTarget = -1; // This should not be used anymore. Set it to an invalid value.
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
    protected boolean reachedTarget() {
        // The + 1 accounts for the current turn.
        return getElapsedTurns() + 1 >= turnsToTarget;
    }

    /**
     * Determine if the mission has reached its home airbase.
     *
     * @return True if the mission has reached its home airbase. False otherwise.
     */
    @Override
    protected boolean reachedHome() {
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
        return Optional.ofNullable(targetTaskForce)
                .orElseGet(this::getTargetTaskForce);
    }

    /**
     * Set all of the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        targetTaskForce = getTarget();
        squadrons.add(targetTaskForce, AirMissionType.DISTANT_CAP);
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
        return rules.isAffectedByWeather();
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
     * Get the target task force.
     *
     * @return The target task force.
     */
    private Target getTargetTaskForce() {
        targetTaskForce = game
                .getPlayer(airbase.getSide())
                .getTargets(AirMissionType.DISTANT_CAP, nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetName))
                .findAny()
                .orElse(null);

        return targetTaskForce;
    }

    private int getTurnsToDistance(final int distance) {
        return (distance / range) + (distance % range > 0 ? 1 : 0);
    }
}
