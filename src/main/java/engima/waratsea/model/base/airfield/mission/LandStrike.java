package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.aircraft.AttackType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.path.AirMissionPath;
import engima.waratsea.model.base.airfield.mission.path.AirMissionPathDAO;
import engima.waratsea.model.base.airfield.mission.path.data.AirMissionPathData;
import engima.waratsea.model.base.airfield.mission.rules.MissionAirRules;
import engima.waratsea.model.base.airfield.mission.state.AirMissionAction;
import engima.waratsea.model.base.airfield.mission.state.AirMissionExecutor;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.Dice;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class LandStrike extends AirMissionExecutor implements AirMission  {
    private static final BigDecimal PERCENTAGE = new BigDecimal(100);

    private static final int ONE_STEP_ELIMINATED = 3;  // The number of successful die rolls required to eliminate one step of aircraft.
    private static final int TWO_STEP_ELIMINATED = 6;  // THe number of successful die rolls required to eliminate tow steps of aircraft.
    private static final Set<Integer> STEP_HIT_SET = new HashSet<>(Arrays.asList(ONE_STEP_ELIMINATED, TWO_STEP_ELIMINATED));

    private static final Map<Integer, Integer> STEP_ELIMINATED_MAP = new HashMap<>();
    private static final Map<Integer, Integer> CAPACITY_REDUCED_MAP = new HashMap<>();

    private static final int AIRFIELD_CAPACITY_REDUCED_BY_1 = 4;
    private static final int AIRFIELD_CAPACITY_REDUCED_BY_2 = 8;
    private static final Set<Integer> CAPACITY_HIT_SET = new HashSet<>(Arrays.asList(AIRFIELD_CAPACITY_REDUCED_BY_1, AIRFIELD_CAPACITY_REDUCED_BY_2));

    static {
        STEP_ELIMINATED_MAP.put(ONE_STEP_ELIMINATED, 1);
        STEP_ELIMINATED_MAP.put(TWO_STEP_ELIMINATED, 2);

        CAPACITY_REDUCED_MAP.put(AIRFIELD_CAPACITY_REDUCED_BY_1, 1);
        CAPACITY_REDUCED_MAP.put(AIRFIELD_CAPACITY_REDUCED_BY_2, 2);
    }

    @Getter private final int id;
    @Getter private AirMissionState state;
    private final Game game;
    private final Dice dice;
    private final MissionAirRules rules;

    @Getter private final AirMissionType type = AirMissionType.LAND_STRIKE;
    @Getter private final Nation nation;
    @Getter private final Airbase airbase;
    @Getter private final MissionSquadrons squadrons;

    private final String targetBaseName;      //The name of the target air base.
    private final AirMissionPath missionPath; //The grid path of the mission.
    private Target targetAirbase;             //The actual target air base.
    private int range;
    private int startTurn;                    //The game turn on which the mission starts.
    private int turnsToTarget;                //How many turns it takes to reach the target.
    private int turnsToHome;                  //How many turns it takes to return to the starting airbase.

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
    public LandStrike(@Assisted final MissionData data,
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

        //Note, we cannot go ahead and obtain the target air base as it might not have been created at
        //this point in time. So we just save the name of the target air base. The target air base
        // must be determined outside the constructor.
        targetBaseName = data.getTarget();

        squadrons.setSquadrons(airbase, data.getSquadronMap());

        AirMissionPathData pathData = Optional
                .ofNullable(data.getAirMissionPathData())
                .orElseGet(AirMissionPathData::new);

        pathData.setType(AirMissionType.LAND_STRIKE);

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
        data.setType(AirMissionType.LAND_STRIKE);
        data.setNation(nation);
        data.setTarget(targetBaseName);
        data.setSquadronMap(squadrons.getData());
        data.setAirMissionPathData(missionPath.getData());

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
        targetAirbase = getTarget();

        startTurn = game.getTurn().getNumber();

        range = squadrons.getMinimumRange();   // The mission moves the minimum range this turn.

        int distance = targetAirbase.getDistance(airbase);
        int roundTrip = distance * 2;

        turnsToTarget = getTurnsToDistance(distance);
        turnsToHome = getTurnsToDistance(roundTrip);

        missionPath.start(airbase, targetAirbase);
        squadrons.takeOff();
    }

    /**
     * Progress the mission forward.
     */
    @Override
    public void fly() {
        missionPath.progress(range);

        // get enemy airfields that have CAP and one of the traversed grids is a CAP grid. Get the best grid for CAP intercept for the airfield/taskforce.
        //    in the future will need to account for cap mission zones.
        // for each airfield do cap intercept.
    }

    /**
     * Execute the mission.
     */
    @Override
    public void execute() {
        // Resolve the squadrons attack on the enemy airfield.
        targetAirbase = getTarget();
        targetAirbase.resolveAttack(squadrons);
    }

    /**
     * Recall the mission. The squadrons return to base without having executed the mission.
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
        return Optional.ofNullable(targetAirbase)
                .orElseGet(this::getTargetAirbase);
    }

    /**
     * Set all of the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        targetAirbase = getTarget();
        squadrons.add(targetAirbase, AirMissionType.LAND_STRIKE);
    }

    /**
     * Remove all the squadrons from the mission.
     */
    @Override
    public void removeSquadrons() {
        squadrons.remove();
    }

    /**
     * Get this mission's probability of success.
     *
     * @return A list of mission probabilities. One probability is for the destruction of
     * squadron steps on the ground at the target airfield. One probability is for the
     * reduction of the target airfield's capacity to station squadron steps.
     */
    @Override
    public List<ProbabilityStats> getMissionProbability() {
        Map<Double, Integer> factors = getAttackMap();

        ProbabilityStats stepsDestroyedProbability = new ProbabilityStats();
        stepsDestroyedProbability.setTitle("Squadron Steps Destroyed");
        stepsDestroyedProbability.setEventColumnTitle("Steps Destroyed");
        stepsDestroyedProbability.setMetaData(rules.getModifierMap());
        stepsDestroyedProbability.setProbability(buildProbabilityStepDestroyed(factors));

        ProbabilityStats capacityReducedProbability = new ProbabilityStats();
        capacityReducedProbability.setTitle("Airfield Capacity Reduced");
        capacityReducedProbability.setEventColumnTitle("Capacity Reduced");
        capacityReducedProbability.setMetaData(rules.getModifierMap());
        capacityReducedProbability.setProbability(buildProbabilityAirfieldDamaged(factors));

        return List.of(stepsDestroyedProbability, capacityReducedProbability);
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
     * Get the target air base.
     *
     * @return The target air base.
     */
    private Target getTargetAirbase() {
        targetAirbase = game
                .getPlayer(airbase.getSide())
                .getTargets(AirMissionType.LAND_STRIKE, nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetBaseName))
                .findAny()
                .orElse(null);

        return targetAirbase;
    }

    /**
     * Build the land strike's airfield damaged probability map for this mission. This is a map of the amount
     * the airfield is damaged (reduction in capacity) to the probability that this damage is actually achieved.
     * Like so:
     *
     * <ul>
     *   <li>Airfield capacity reduced by 1  => probability percentage airfield capacity reduced by 1</li>
     *   <li>Airfield capacity reduced by 2  => probability percentage Airfield capacity reduced by 2</li>
     *   <li>...</li>
     * </ul>
     *
     * @param factors The squadrons on this mission land attack modifiers and factors.
     * @return The probability map as illustrated above.
     */
    private Map<String, Integer> buildProbabilityAirfieldDamaged(final Map<Double, Integer> factors) {
        return CAPACITY_HIT_SET
                .stream()
                .collect(Collectors.toMap(numHits -> CAPACITY_REDUCED_MAP.get(numHits) + "",
                        numHits -> getProbability(numHits, factors)));
    }

    /**
     * Build the land strike's step destruction probability map for this mission. This is a map of the number
     * of enemy aircraft steps destroyed to the probability that this number is actually achieved.
     * Like so:
     *
     * <ul>
     *   <li>1 step destroyed  => probability percentage 1 step destroyed</li>
     *   <li>2 steps destroyed => probability percentage 2 steps destroyed</li>
     *   <li>...</li>
     * </ul>
     *
     * @param factors The squadrons on this mission land attack modifiers and factors.
     * @return The probability map as illustrated above.
     */
    private Map<String, Integer> buildProbabilityStepDestroyed(final Map<Double, Integer> factors) {
        return STEP_HIT_SET
                .stream()
                .collect(Collectors.toMap(numHits -> STEP_ELIMINATED_MAP.get(numHits) + "",
                                          numHits -> getProbability(numHits, factors)));
    }

    /**
     * The squadrons in this mission will have varying attack modifiers. These modifiers
     * determine the probability of an individual squadron achieving a successful attack.
     * Map the squadrons land attack probability to the total number of attack factors for
     * the given probability.
     *
     * For example if this mission has 3 squadrons with the following stats.
     *   squadron one: Probability = 0.2, Factor = 5
     *   squadron two: Probability = 0.1, Factor = 3
     *   squadron three: Probability = 0.1, Factor = 2
     *
     * then the map will look like:
     *
     *    Probability => Total Factor
     *       0.2     ->      5
     *       0.1     ->      5
     *
     * @return The attack map as described above.
     */
    private Map<Double, Integer> getAttackMap() {
         return squadrons.get(MissionRole.MAIN)
                .stream()
                .collect(Collectors.toMap(this::getLandAttackProbability,
                        this::getFactor,
                        Integer::sum));
    }

    /**
     * Get the probability that this mission will achieve the given number of hits.
     *
     * @param numHits The desired number of hits.
     * @param factors The mission factors. This is a map of attack modifier to attack factor.
     * @return The probability as a percentage that this mission achieves the given number
     * of hits.
     */
    private int getProbability(final int numHits, final Map<Double, Integer> factors) {
        BigDecimal probabilityNoHits = factors
                .entrySet()
                .stream()
                .map(entry -> getProbability(numHits, entry.getValue(), entry.getKey()))
                .reduce(BigDecimal.ONE, BigDecimal::multiply);

        BigDecimal probability = BigDecimal.ONE.subtract(probabilityNoHits);

        return probability.multiply(PERCENTAGE).toBigInteger().intValue();
    }

    /**
     * Determine the probability that the given number of hits is achieved given an attack factor (which
     * implies the total number of dice rolled) and given the attack modifier (which implies the percentage
     * chance of success of one die roll).
     *
     * @param hits The desired number of hits.
     * @param factor The attack factor -> number of dice to roll.
     * @param individualProbability The individual probability of a single successful die roll.
     * @return The probability of achieving the given number of hits.
     */
    private BigDecimal getProbability(final int hits, final int factor, final double individualProbability) {
        double probabilityHis = dice.probabilityHits(hits, factor, individualProbability);
        double probabilityNoHits = 1.0 - probabilityHis;
        return new BigDecimal(probabilityNoHits);
    }

    /**
     * Get the land strike modifier for the given squadron. This modifier includes the global game
     * land attack modifiers. An example of a global game modifier is the current weather conditions.
     *
     * @param squadron The squadron for which the land attack modifier is obtained.
     * @return The squadron's land attack modifier, including any global game land attack modifiers.
     */
    private double getLandAttackProbability(final Squadron squadron) {
        return squadron.getHitIndividualProbability(AttackType.LAND, getTarget(), rules.getModifier());
    }

    /**
     * Get the squadron's current land attack factor.
     *
     * @param squadron The squadron.
     * @return The squadron's current land attack factor.
     */
    private int getFactor(final Squadron squadron) {
        return squadron.getAttack(AttackType.LAND).getFactor();
    }

    private int getTurnsToDistance(final int distance) {
        return (distance / range) + (distance % range > 0 ? 1 : 0);
    }
}
