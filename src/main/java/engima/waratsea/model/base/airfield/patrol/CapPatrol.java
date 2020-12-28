package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirRules;
import engima.waratsea.model.base.airfield.patrol.stats.PatrolStat;
import engima.waratsea.model.base.airfield.patrol.stats.PatrolStats;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.rules.GameRules;
import engima.waratsea.model.game.rules.SquadronConfigRulesDTO;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class CapPatrol implements Patrol {
    private static final LinkedHashSet<SquadronConfig> VALID_SQUADRON_CONFIGS = new LinkedHashSet<>(Collections.singletonList(SquadronConfig.NONE));

    private static final int RADIUS = 2;

    private final PatrolAirRules capRules;
    private final GameRules gameRules;
    private final List<Squadron> squadrons;
    @Getter private final Airbase airbase;
    @Getter private int maxRadius;

    /**
     * The constructor.
     *
     * @param data The CAP patrol data read in from a JSON file.
     * @param capRules The CAP air rules.
     * @param gameRules The game rules.
     */
    @Inject
    public CapPatrol(@Assisted final PatrolData data,
                               final @Named("cap") PatrolAirRules capRules,
                               final GameRules gameRules) {
        this.capRules = capRules;
        this.gameRules = gameRules;

        airbase = data.getAirbase();

        squadrons = Optional.ofNullable(data.getSquadrons())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(airbase::getSquadron)
                .collect(Collectors.toList());

        if (!squadrons.isEmpty()) {
            maxRadius = RADIUS;
        }
    }

    /**
     * Get the Patrol data.
     *
     * @return The Patrol data.
     */
    @Override
    public PatrolData getData() {
        PatrolData data = new PatrolData();

        List<String> names = Optional
                .ofNullable(squadrons)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());

        data.setSquadrons(names);

        return  data;
    }

    /**
     * Get the Patrol type.
     *
     * @return The patrol type.
     */
    @Override
    public PatrolType getType() {
        return PatrolType.CAP;
    }

    /**
     * Get the squadrons on patrol.
     *
     * @return A list of squadrons on the patrol.
     */
    @Override
    public List<Squadron> getAssignedSquadrons() {
        return squadrons;
    }

    /**
     * Get the squadrons on patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on the patrol.
     */
    @Override
    public List<Squadron> getAssignedSquadrons(final Nation nation) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .collect(Collectors.toList());
    }

    /**
     * Add a squadron to the patrol.
     *
     * @param squadron The squadron added to the patrol.
     */
    @Override
    public void addSquadron(final Squadron squadron) {
        if (canAdd(squadron)) {   //Make sure the squadron is actuall deployed at the airbase.
            squadrons.add(squadron);
            squadron.setState(SquadronAction.ASSIGN_TO_PATROL);
            maxRadius = RADIUS;
        } else {
            log.error("Unable to add squadron: '{}' to patrol. Squadron not deployed to airbase: '{}' or unable to perform CAP", squadron, airbase);
        }
    }

    /**
     * Remove a squadron from the patrol.
     *
     * @param squadron The squadron removed from the patrol.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        squadrons.remove(squadron);
        squadron.setState(SquadronAction.REMOVE_FROM_PATROL);

        if (squadrons.isEmpty()) {
            maxRadius = 0;
        }
    }

    /**
     * Determine which squadrons on patrol can reach the given target radius.
     *
     * @param targetRadius A patrol radius for which each squadron on patrol is
     *                     checked to determine if the squadron can reach the
     *                     radius.
     * @return A list of squadrons on patrol that can reach the given target radius.
     */
    @Override
    public List<Squadron> getAssignedSquadrons(final int targetRadius) {
        return (targetRadius <= RADIUS) ?  squadrons : Collections.emptyList();
    }

    /**
     * Determine the squadrons available to perform this patrol for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of squadrons available for this patrol for the given nation.
     */
    @Override
    public List<Squadron> getAvailableSquadrons(final Nation nation) {
        return airbase
                .getSquadrons(nation)
                .stream()
                .filter(squadron -> squadron.isAtState(SquadronState.READY))
                .filter(squadron -> squadron.canDoPatrol(PatrolType.CAP))
                .collect(Collectors.toList());    }

    /**
     * Get the search success rate of the patrol given the distance to the target.
     *
     * @param distance The distance to the target.
     * @return The success rate.
     */
    @Override
    public int getSuccessRate(final int distance) {
        List<Squadron> inRange = getAssignedSquadrons(distance);
        return capRules.getBaseSearchSuccess(distance, inRange);
    }

    /**
     * Get the patrol data.
     *
     * @return A map of data for this patrol.
     */
    @Override
    public PatrolStats getPatrolStats() {
        Map<String, String> toolTips = new HashMap<>();
        toolTips.put("Intercept", "Intercept effectiveness with present weather affects");
        toolTips.put("No Weather", "Intercept effectiveness without any weather affects");

        if (squadrons.isEmpty()) {
            return new PatrolStats();
        }

        Map<Integer, Map<String, PatrolStat>> stats = IntStream
                .range(0, maxRadius + 1)
                .boxed()
                .collect(Collectors.toMap(radius -> radius, this::getPatrolStat));

        PatrolStats patrolStats = new PatrolStats();
        patrolStats.setData(stats);
        patrolStats.setMetaData(toolTips);
        return patrolStats;
    }

    /**
     * Clear all of the squadrons from this patrol.
     */
    @Override
    public void clearSquadrons() {
        squadrons.forEach(squadron -> squadron.setState(SquadronAction.REMOVE_FROM_PATROL));

        squadrons.clear();
        maxRadius = 0;
    }

    /**
     * Clear the squadrons of the given nation from this patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    @Override
    public void clearSquadrons(final Nation nation) {
        List<Squadron> toRemove = squadrons.stream()
                .filter(squadron -> squadron.ofNation(nation))
                .peek(squadron -> squadron.setState(SquadronAction.REMOVE_FROM_PATROL))
                .collect(Collectors.toList());

        squadrons.removeAll(toRemove);
        maxRadius = squadrons.isEmpty() ? 0 : RADIUS;
    }

    /**
     * Get the patrol data that corresponds to the given radius. This is the
     * data for a patrol that takes place at the given radius.
     *
     * @param radius The patrol radius.
     * @return A map of data for this patrol that corresponds to the given radius.
     */
    private Map<String, PatrolStat> getPatrolStat(final int radius) {
        List<Squadron> inRange = getAssignedSquadrons(radius);

        Map<String, PatrolStat> data = new LinkedHashMap<>();
        data.put("Squadrons", new PatrolStat(inRange.size(), getPatrolSquadrons(radius)));
        data.put("Steps", new PatrolStat(inRange.stream().map(Squadron::getSteps).reduce(BigDecimal.ZERO, BigDecimal::add)));
        data.put("Intercept", new PatrolStat(getSuccessRate(radius) + " %"));
        data.put("No Weather", new PatrolStat(capRules.getBaseSearchSuccessNoWeather(radius, inRange) + "%"));

        return data;
    }

    /**
     * Get the squadron titles that are affective at the given radius.
     *
     * @param radius Patrol radius.
     * @return A list of squadron titles that are affective at the given radius.
     */
    private String getPatrolSquadrons(final int radius) {
        return getAssignedSquadrons(radius)
                .stream()
                .map(Squadron::getTitle)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Get the patrol's true maximum squadron radius. This is the maximum radius
     * at which the patrol has a greater than 0 % chance to be successful.
     *
     * @return The patrol's true maximum radius.
     */
    @Override
    public int getTrueMaxRadius() {
        return IntStream.range(0, maxRadius + 1)
                .boxed()
                .sorted(Collections.reverseOrder())
                .filter(radius -> getSuccessRate(radius) > 0)
                .findFirst().orElse(0);    }

    /**
     * Get the best allowed squadron configuration for this patrol.
     *
     * @return The best allowed squadron configuration for this patrols.
     */
    @Override
    public SquadronConfig getBestSquadronConfig() {
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO().setPatrolType(PatrolType.CAP);

        Set<SquadronConfig> allowed = gameRules.getAllowedSquadronConfig(dto);

        // Get the first config for the given patrol type that is allowed.
        // This should return the most desired patrol squadron configuration.
        return VALID_SQUADRON_CONFIGS
                .stream()
                .filter(allowed::contains)
                .findFirst()
                .orElse(SquadronConfig.NONE);
    }

    /**
     * Determine if the patrol is adversely affected by the current weather conditions.
     *
     * @return True if the patrol is affected by the current weather conditions. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return capRules.isAffectedByWeather();
    }

    /**
     * Determine if the squadron may be added to the patrol.
     *
     * @param squadron The squadron that is potentially added to the patrol.
     * @return True if the given squadron may be added to this patrol. False otherwise.
     */
    private boolean canAdd(final Squadron squadron) {
        return squadron.canDoPatrol(PatrolType.CAP) && airbase.getSquadrons().contains(squadron);
    }
}
