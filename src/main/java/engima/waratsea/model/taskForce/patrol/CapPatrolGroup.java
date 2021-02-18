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
import engima.waratsea.model.taskForce.patrol.data.PatrolGroupData;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CapPatrolGroup implements PatrolGroup {
    private static final int RADIUS = 2;
    @Getter private final PatrolType type = PatrolType.CAP;
    @Getter private final List<Squadron> squadrons;
    @Getter private int maxRadius;

    private final PatrolGroups patrolGroups; // The parent patrols groups.

    private final PatrolAirRules capRules;

    @Inject
    public CapPatrolGroup(@Assisted final PatrolGroupData data,
                                    final @Named("cap") PatrolAirRules capRules) {
        this.capRules = capRules;

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
     * Determine if squadrons are present in this patrol.
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
        return IntStream.range(0, maxRadius + 1)
                .boxed()
                .sorted(Collections.reverseOrder())
                .filter(radius -> getSuccessRate(radius) > 0)
                .findFirst().orElse(0);
    }

    /**
     * Get the patrol stats for this group.
     *
     * @return The patrol stats for this patrol group.
     */
    @Override
    public PatrolStats getPatrolStats() {
        if (squadrons.isEmpty()) {
            return new PatrolStats();
        }

        Map<Integer, Map<String, PatrolStat>> stats = IntStream
                .range(0, maxRadius + 1)
                .boxed()
                .collect(Collectors.toMap(radius -> radius, this::getPatrolStat));

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
        return getSquadronsInRange(radius)
                .stream()
                .map(Squadron::getTitle)
                .collect(Collectors.joining("\n"));
    }

    private void updateMaxRadius() {
        if (!squadrons.isEmpty()) {
            maxRadius = RADIUS;
        }
    }

    /**
     * Get the search success rate of the patrol given the distance to the target.
     *
     * @param distance The distance to the target.
     * @return The success rate.
     */
    private int getSuccessRate(final int distance) {
        List<Squadron> inRange = getSquadronsInRange(distance);
        return capRules.getBaseSearchSuccess(distance, inRange);
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
        return (targetRadius <= RADIUS) ?  squadrons : Collections.emptyList();
    }

    private Map<String, String> getMetaData() {
        return Map.of("Intercept", "Intercept effectiveness with present weather affects",
                      "No Weather", "Intercept effectiveness without any weather affects");
    }
}
