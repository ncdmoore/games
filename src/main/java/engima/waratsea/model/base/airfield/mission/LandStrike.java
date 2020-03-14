package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.rules.MissionAirRules;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.Dice;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LandStrike implements AirMission {
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

    private final Game game;
    private final Dice dice;
    private final MissionAirRules rules;

    @Getter
    private final Nation nation;

    @Getter
    private final Airbase airbase;

    private final Map<MissionRole, List<Squadron>> squadronMap;

    private final String targetBaseName;      //The name of the target air base.
    private Target targetAirbase;             //The actual target air base.
    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param game The game.
     * @param rules The mission air rules.
     * @param dice The dice utility.
     */
    @Inject
    public LandStrike(@Assisted final MissionData data,
                                final Game game,
                                final @Named("airStrike") MissionAirRules rules,
                                final Dice dice) {
        this.game = game;
        this.dice = dice;
        this.rules = rules;

        nation = data.getNation();

        airbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        //Note, we cannot go ahead and obtain the target air base as it might not have been created at
        //this point in time. So we just save the name of the target air base. The target air base
        // must be determined outside the constructor.
        targetBaseName = data.getTarget();

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
    }

    /**
     * Get the persistent mission data.
     *
     * @return The persistent mission data.
     */
    @Override
    public MissionData getData() {
        MissionData data = new MissionData();

        data.setType(AirMissionType.LAND_STRIKE);
        data.setNation(nation);
        data.setTarget(targetBaseName);

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
        return Optional.ofNullable(targetAirbase)
                .orElseGet(this::getTargetAirbase);
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
        stepsDestroyedProbability.setProbability(buildProbabilityStepDestroyed(factors));

        ProbabilityStats capacityReducedProbability = new ProbabilityStats();
        capacityReducedProbability.setTitle("Airfield Capacity Reduced");
        capacityReducedProbability.setEventColumnTitle("Capacity Reduced");
        capacityReducedProbability.setProbability(buildProbabilityAirfieldDamaged(factors));

        return new ArrayList<>(Arrays.asList(stepsDestroyedProbability, capacityReducedProbability));
    }

    /**
     * Get the target air base.
     *
     * @return The target air base.
     */
    private Target getTargetAirbase() {
        targetAirbase = game
                .getPlayer(airbase.getSide())
                .getEnemyAirfieldTargets()
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
    private Map<Integer, Integer> buildProbabilityAirfieldDamaged(final Map<Double, Integer> factors) {
        return CAPACITY_HIT_SET
                .stream()
                .collect(Collectors.toMap(CAPACITY_REDUCED_MAP::get,
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
    private Map<Integer, Integer> buildProbabilityStepDestroyed(final Map<Double, Integer> factors) {
        return STEP_HIT_SET
                .stream()
                .collect(Collectors.toMap(STEP_ELIMINATED_MAP::get,
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
         return squadronMap.get(MissionRole.MAIN)
                .stream()
                .collect(Collectors.toMap(this::getLandAttackProbability,
                        Squadron::getLandFactor,
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
        return squadron.getLandHitIndividualProbability(getTarget(), rules.getModifier());
    }
}
