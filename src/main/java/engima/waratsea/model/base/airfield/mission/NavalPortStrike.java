package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.aircraft.AttackType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class NavalPortStrike extends AirMissionExecutor implements AirMission {
    private static final int PORT_MODIFIER = 1; // Ships in port are easier to hit. Add a 1 to the modifier.
    private static final BigDecimal PERCENTAGE = new BigDecimal(100);

    private static final Set<Integer> SHIP_HIT_SET = new HashSet<>(Arrays.asList(1, 2, 3, 4));

    @Getter private final int id;
    @Getter private AirMissionState state;
    private final Game game;
    private final Dice dice;
    private final MissionAirRules rules;

    @Getter private final AirMissionType type = AirMissionType.NAVAL_PORT_STRIKE;
    @Getter private final Nation nation;
    @Getter private final Airbase airbase;
    @Getter private final MissionSquadrons squadrons;

    private final String targetBaseName;      //The name of the target port.
    private Target targetPort;                //The actual target port.
    private int startTurn;                    //The game turn on which the mission starts.
    private int turnsToTarget;                //The turns until the mission reaches its target.
    private int turnsToHome;                  //The turns until the mission returns to its home airbase.
    /**
     * Constructor called by guice.
     *
     * @param data The mission data read in from a JSON file.
     * @param squadrons The squadrons on this mission.
     * @param game The game.
     * @param rules The mission air rules.
     * @param dice The dice utility.
     */
    @Inject
    public NavalPortStrike(@Assisted final MissionData data,
                                     final MissionSquadrons squadrons,
                                     final Game game,
                                     final @Named("airStrike") MissionAirRules rules,
                                     final Dice dice) {
        id = data.getId();
        state = Optional.ofNullable(data.getState()).orElse(AirMissionState.READY);
        this.squadrons = squadrons;
        this.game = game;
        this.rules = rules;
        this.dice = dice;

        nation = data.getNation();

        airbase = data.getAirbase(); //Note, this is not read in from the JSON file. So no need to save it.

        squadrons.setSquadrons(airbase, data.getSquadronMap());

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
        data.setState(state);
        data.setType(AirMissionType.NAVAL_PORT_STRIKE);
        data.setNation(nation);
        data.setTarget(targetBaseName);
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

        // Resolve the squadron attack on the enemy port.
        targetPort.resolveAttack(squadrons);
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
     * Set all the squadrons to the correct state.
     */
    @Override
    public void addSquadrons() {
        getTarget(); // sets target port.

        squadrons.add(targetPort, AirMissionType.NAVAL_PORT_STRIKE, id);
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
        var warshipFactors = getAttackMap(AttackType.NAVAL_WARSHIP);

        ProbabilityStats warshipsHitProbability = ProbabilityStats
                .builder()
                .title("Warship Hits")
                .eventColumnTitle("Ship Hits")
                .metaData(getModifierMap())
                .probability(buildProbabilityShipHit(warshipFactors))
                .build();

        var transportFactors = getAttackMap(AttackType.NAVAL_TRANSPORT);

        ProbabilityStats transportHitProbability = ProbabilityStats
                .builder()
                .title("Transport Hits")
                .eventColumnTitle("Ship Hits")
                .metaData(getModifierMap())
                .probability(buildProbabilityShipHit(transportFactors))
                .build();

        return List.of(warshipsHitProbability, transportHitProbability);
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
     * Get the target port.
     *
     * @return The target port.
     */
    private Target getTargetPort() {
        targetPort = game
                .getPlayer(airbase.getSide())
                .getTargets(AirMissionType.NAVAL_PORT_STRIKE, nation)
                .stream()
                .filter(target -> target.getName().equalsIgnoreCase(targetBaseName))
                .findAny()
                .orElse(null);

        return targetPort;
    }

    /**
     * The squadrons in this mission will have varying attack modifiers. These modifiers
     * determine the probability of an individual squadron achieving a successful attack.
     * Map the squadrons naval attack probabilities to the total number of attack factors for
     * the given modifier.
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
     * @param attackType The attack type: Warship or Transport.
     * @return The attack map as described above.
     */
    private Map<Double, Integer> getAttackMap(final AttackType attackType) {
        return squadrons.get(MissionRole.MAIN)
                .stream()
                .collect(Collectors.toMap(squadron -> getNavalProbability(attackType, squadron),
                        squadron -> getFactor(attackType, squadron),
                        Integer::sum));
    }

    /**
     * Build the naval strike's ship hit probability map for this mission. This is a map of the number
     * of enemy ships hit to the probability that this number is actually achieved.
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
    private Map<String, Integer> buildProbabilityShipHit(final Map<Double, Integer> factors) {
        return SHIP_HIT_SET
                .stream()
                .collect(Collectors.toMap(numHits -> numHits + "",
                        numHits -> getProbability(numHits, factors)));
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
     * Get the naval strike modifier for the given squadron. This modifier includes the global game
     * naval attack modifiers. An example of a global game modifier is the current weather conditions.
     *
     * @param attackType The attack type: Warship or Transport.
     * @param squadron The squadron for which the naval attack modifier is obtained.
     * @return The squadron's naval attack modifier, including any global game naval attack modifiers.
     */
    private double getNavalProbability(final AttackType attackType, final Squadron squadron) {
        return squadron.getHitIndividualProbability(attackType, getTarget(), rules.getModifier() + PORT_MODIFIER);
    }

    /**
     * Get the squadron's current naval attack factor.
     *
     * @param attackType The attack type: Warship or Transport.
     * @param squadron The squadron.
     * @return The squadron's current naval attack factor.
     */
    private int getFactor(final AttackType attackType, final Squadron squadron) {
        return squadron.getAttack(attackType).getFactor();
    }

    private Map<String, Integer> getModifierMap() {
        Map<String, Integer> modifiers = rules.getModifierMap();
        modifiers.put("In Port", PORT_MODIFIER);
        return modifiers;
    }
}
