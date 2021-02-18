package engima.waratsea.model.taskForce.patrol;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.base.AirbaseGroup;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirRules;
import engima.waratsea.model.base.airfield.patrol.stats.PatrolStat;
import engima.waratsea.model.base.airfield.patrol.stats.PatrolStats;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.taskForce.patrol.data.PatrolGroupData;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchPatrolGroup implements PatrolGroup {
    @Getter private final PatrolType type = PatrolType.SEARCH;
    @Getter private final List<Squadron> squadrons;
    @Getter private int maxRadius;

    private final PatrolGroups patrolGroups;  //The parent patrol groups.

    private final PatrolAirRules searchRules; //The set of rules that govern this patrol.

    /**
     * Constructor called by guice. Get the airbases from the given task force. From the airbases
     * get the search patrols. From the search patrols get the assigned squadrons.
     *
     * @param data The data needed to construct a search patrol group. The required data consists
     *             of the task force.
     * @param searchRules The search patrol rules.
     */
    @Inject
    public SearchPatrolGroup(@Assisted final PatrolGroupData data,
                                       final @Named("search") PatrolAirRules searchRules) {
        this.searchRules = searchRules;

        patrolGroups = data.getGroups();
        squadrons = data.getSquadrons();

        updateMaxRadius();
    }

    /**
     * Get the patrol group's title.
     *
     * @return The patrol group's title.
     */
    @Override
    public String getTitle() {
        return type.getValue();
    }

    /**
     * Get the airbase group for which this patrol group is associated.
     *
     * @return The airbase group of this patrol group.
     */
    @Override
    public AirbaseGroup getAirbaseGroup() {
        return patrolGroups.getAirbaseGroup();
    }

    /**
     * Get the home airbase group.
     *
     * @return The home airbase group of this patrol group.
     */
    @Override
    public AirbaseGroup getHomeGroup() {
        return patrolGroups.getHomeGroup();
    }

    /**
     * Determine if squadrons are present for the given nation.
     *
     * @param nation A nation: BRITISH, ITALIAN, et...
     * @return True if squadrons are present. False otherwise.
     */
    @Override
    public boolean areSquadronsPresent(final Nation nation) {
        return squadrons
                .stream()
                .anyMatch(squadron -> squadron.ofNation(nation));
    }

    /**
     * Get the patrol's groups true maximum squadron radius. This is the maximum radius
     * at which the patrol group has a greater than 0 % chance to be successful.
     *
     * @return The patrol's true maximum radius.
     */
    @Override
    public int getTrueMaxRadius() {
        return IntStream.range(1, maxRadius + 1)
                .boxed()
                .sorted(Collections.reverseOrder())
                .filter(radius -> getSuccessRate(radius) > 0)
                .findFirst()
                .orElse(0);
    }

    /**
     * Get the patrol stats for this group.
     *
     * @return The patrol stats for this patrol group.
     */
    @Override
    public PatrolStats getPatrolStats() {
        int trueMaxRadius = getTrueMaxRadius();

        Map<Integer, Map<String, PatrolStat>> stats = IntStream
                .range(1, trueMaxRadius + 1)
                .boxed()
                .collect(Collectors.toMap(radius -> radius,
                                          this::getPatrolStat));

        PatrolStats patrolStats = new PatrolStats();
        patrolStats.setData(stats);
        patrolStats.setMetaData(getMetaData());

        return patrolStats;
    }

    /**
     * Get the patrol data that corresponds to the given radius. This is the
     * data for a patrol that takes place at the given radius.
     *
     * @param radius The patrol radius.
     * @return A map of data for this patrol that corresponds to the given radius.
     */
    private Map<String, PatrolStat> getPatrolStat(final int radius) {
        List<Squadron> inRange = getSquadronsInRange(radius);

        Map<String, PatrolStat> data = new LinkedHashMap<>();
        data.put("Squadrons", new PatrolStat(inRange.size(), getPatrolSquadrons(radius)));
        data.put("Steps", new PatrolStat(inRange.stream().map(Squadron::getSteps).reduce(BigDecimal.ZERO, BigDecimal::add)));
        data.put("Search", new PatrolStat(getSuccessRate(radius) + " %", getPatrolSearchFactors(radius)));
        data.put("No Weather", new PatrolStat(searchRules.getBaseSearchSuccessNoWeather(radius, inRange) + "%"));

        return data;
    }

    /**
     * Get the squadron titles that are affective at the given radius.
     *
     * @param radius Patrol radius.
     * @return A list of squadron titles that are affective at the given radius.
     */
    private String getPatrolSquadrons(final int radius) {
        return getSquadronsInRange(radius)
                .stream()
                .map(this::getSquadronTitleAndHome)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Get the patrol factors at the given radius.
     *
     * @param radius Patrol radius.
     * @return A string that specifies the factors that determine the patrols success.
     */
    private String getPatrolSearchFactors(final int radius) {
        return getSearchFactors(radius)
                .entrySet()
                .stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Get the patrol success factors.
     *
     * @param radius The distance to the target.
     * @return A map of factor name to factor value.
     */
    private Map<String, String> getSearchFactors(final int radius) {
        List<Squadron> inRange = getSquadronsInRange(radius);
        return searchRules.getBaseSearchFactors(radius, inRange);
    }

    /**
     * Update this search's maximum search radius. If the newly added squadron has a greater
     * radius then the current maximum search radius, then this squadron's search radius
     * is the new maximum search radius.
     **/
    private void updateMaxRadius() {
        maxRadius = squadrons
                .stream()
                .map(squadron -> squadron.getRadius(SquadronConfig.SEARCH))
                .max(Integer::compare)
                .orElse(0);
    }

    /**
     * Get the rate of success for this patrol's air search.
     *
     * @param radius The distance the target is from the patrol's base.
     * @return An integer representing the percentage success rate of finding a task force.
     */
    private int getSuccessRate(final int radius) {
        List<Squadron> inRange = getSquadronsInRange(radius);
        return searchRules.getBaseSearchSuccess(radius, inRange);
    }

    /**
     * Determine which squadrons on patrol can reach the given target radius.
     *
     * @param targetRadius A patrol radius for which each squadron on patrol is
     *                     checked to determine if the squadron can reach the
     *                     radius.
     * @return A list of squadrons on patrol that can reach the given target radius.
     */
    private List<Squadron> getSquadronsInRange(final int targetRadius) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.getRadius(SquadronConfig.SEARCH) >= targetRadius)
                .collect(Collectors.toList());
    }

    private Map<String, String> getMetaData() {
        return Map.of("Search", "Search effectiveness with present weather affects",
                      "No Weather", "Search effectiveness without any weather affects");
    }

    private String getSquadronTitleAndHome(final Squadron squadron) {
        return  squadron.getTitle() + " : " + squadron.getHome().getTitle();
    }
}
