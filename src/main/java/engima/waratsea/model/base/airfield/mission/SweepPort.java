package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.aircraft.AttackType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.rules.MissionAirRules;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.Dice;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class SweepPort implements AirMission {
    private static final int INTERCEPT_FACTOR = 3; // CAP interception occurs on values 3-6 of a single six-sided die roll.
    private static final BigDecimal PERCENTAGE = new BigDecimal(100);
    private static final Set<Integer> STEP_HIT_SET = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));

    private final Game game;
    private final MissionAirRules rules;
    private final Dice dice;

    @Getter private final AirMissionType type = AirMissionType.SWEEP_PORT;
    @Getter private final int id;
    @Getter private final Nation nation;
    @Getter private final Airbase airbase;
    @Getter private final Map<MissionRole, List<Squadron>> squadronMap;

    private final String targetBaseName;      //The name of the target port.
    private Target targetPort;                //The actual target port.

    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param game The game.
     * @param rules The air sweep rules.
     * @param dice The dice utility.
     */
    @Inject
    public SweepPort(@Assisted final MissionData data,
                     final Game game,
                     final @Named("airSweep") MissionAirRules rules,
                     final Dice dice) {
        id = data.getId();
        this.game = game;
        this.rules = rules;
        this.dice = dice;

        nation = data.getNation();

        airbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        // The squadrons can be created here as they are guaranteed to be already created by the air base.
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

        //Note, we cannot go ahead and obtain the target port as it might not have been created at
        //this point in time. So we just save the name of the target port. The target port
        // must be determined outside the constructor.
        targetBaseName = data.getTarget();
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
        data.setType(AirMissionType.SWEEP_PORT);
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
     * For sweep mission there are not escorts. All of the squadrons are
     * fighters anyway.
     *
     * @return All of the squadrons involved with this mission.
     */
    @Override
    public List<Squadron> getSquadronsAllRoles() {
        return MissionRole
                .stream()
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
        return MissionRole
                .stream()
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
                .forEach((role, squadrons) -> squadrons
                        .forEach(squadron -> {
                            squadron.setState(SquadronAction.ASSIGN_TO_MISSION);
                            squadron.equip(targetPort, AirMissionType.NAVAL_TASK_FORCE_STRIKE, role);
                        }));
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
     * Get the probability of success for this mission.
     *
     * @return The probability that this mission will be successful.
     */
    @Override
    public List<ProbabilityStats> getMissionProbability() {
        Map<Double, Integer> factors = getAttackMap();

        ProbabilityStats interceptionProbability = new ProbabilityStats();
        interceptionProbability.setTitle("Interception");
        interceptionProbability.setEventColumnTitle("Intercept");
        interceptionProbability.setProbability(buildProbabilityIntercept());

        ProbabilityStats stepHitProbability = new ProbabilityStats();
        stepHitProbability.setTitle("Steps Hit");
        stepHitProbability.setEventColumnTitle("Steps Hit");
        stepHitProbability.setProbability(buildProbabilityAirHit(factors));

        return List.of(interceptionProbability, stepHitProbability);    }

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
     * Get the target port.
     *
     * @return The target port.
     */
    private Target getTargetPort() {
        targetPort = game
                .getPlayer(airbase.getSide())
                .getTargets(AirMissionType.SWEEP_PORT, nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetBaseName))
                .findAny()
                .orElse(null);

        return targetPort;
    }

    /**
     * Build the probability that the sweep successfully intercepts the CAP over the target.
     *
     * @return A fixed map that indicates the chance of successfully intercepting the CAP.
     */
    private Map<String, Integer> buildProbabilityIntercept() {
        Map<String, Integer> prob = new HashMap<>();
        prob.put("CAP", dice.probabilityPercentage(INTERCEPT_FACTOR + rules.getModifier(), 1));
        return prob;
    }

    /**
     * The squadrons in this mission will have varying attack modifiers. These modifiers
     * determine the probability of an individual squadron achieving a successful attack.
     * Map the squadrons air attack probabilities to the total number of attack factors for
     * the given air attack modifier.
     *
     * For example if this mission has 3 squadrons with the following stats.
     *   squadron one: probability = 0.2, Factor = 5
     *   squadron two: probability = 0.1, Factor = 3
     *   squadron three: probability = 0.1, Factor = 2
     *
     * then the map will look like:
     *
     *    probability => Total Factor
     *       0.2     ->      5
     *       0.1     ->      5
     *
     * @return The attack map as described above.
     */
    private Map<Double, Integer> getAttackMap() {
        return squadronMap.get(MissionRole.MAIN)
                .stream()
                .collect(Collectors.toMap(this::getAirProbability,
                        this::getFactor,
                        Integer::sum));
    }

    /**
     * Build the air-to-air hit probability map for this mission. This is a map of the number
     * of enemy steps hit to the probability that this number is actually achieved.
     * Like so:
     *
     * <ul>
     *   <li>1 step destroyed  => probability percentage 1 step destroyed</li>
     *   <li>2 steps destroyed => probability percentage 2 steps destroyed</li>
     *   <li>...</li>
     * </ul>
     *
     * @param factors The squadrons on this mission individual air attack probability and factors.
     * @return The probability map as illustrated above.
     */
    private Map<String, Integer> buildProbabilityAirHit(final Map<Double, Integer> factors) {
        return STEP_HIT_SET
                .stream()
                .collect(Collectors.toMap(numHits -> numHits + "",
                        numHits -> getProbability(numHits, factors)));
    }

    /**
     * Get the probability that this mission will achieve the given number of hits.
     *
     * @param numHits The desired number of hits.
     * @param factors The mission factors. This is a map of attack probability to attack factor.
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
     * Get the air-to-air hit probability for the given squadron. This probability includes the global game
     * air attack modifiers. An example of a global game modifier is the current weather conditions.
     *
     * @param squadron The squadron for which the air-to-air probability is obtained.
     * @return The squadron's air-to-air hit probability, including any global game air attack modifiers.
     */
    private double getAirProbability(final Squadron squadron) {
        return squadron.getHitIndividualProbability(AttackType.AIR, getTarget(), 0);
    }

    /**
     * Get the squadron's current air attack factor.
     *
     * @param squadron The squadron.
     * @return The squadron's current air attack factor.
     */
    private int getFactor(final Squadron squadron) {
        return squadron.getAttack(AttackType.AIR).getFactor();
    }
}
