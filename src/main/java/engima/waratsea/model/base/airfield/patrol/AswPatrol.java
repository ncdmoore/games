package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirRules;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.rules.GameRules;
import engima.waratsea.model.game.rules.SquadronConfigRulesDTO;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Represents an airbase's ASW patrol.
 *
 * An airbase may be an airfield or an aircraft/seaplane carrier.
 *
 */

@Slf4j
public class AswPatrol implements Patrol {
    private static final LinkedHashSet<SquadronConfig> VALID_SQUADRON_CONFIGS = new LinkedHashSet<>(Collections.singletonList(SquadronConfig.NONE));

    private final PatrolSquadrons squadrons;  //The squadrons that are on this patrol.
    private final PatrolPath patrolPath;
    @Getter private final Airbase airbase;    //The airbase from which the patrol is flown.
    @Getter private int maxRadius;            //The maximum patrol radius of this patrol.
    private final PatrolAirRules rules;       //The set of rules that govern this patrol.
    private final GameRules gameRules;

    private Map<Integer, List<GameGrid>> gridPath;

    /**
     * The constructor.
     *
     * @param data The ASW patrol data read in from a JSON file.
     * @param aswRules The ASW rules.
     * @param gameRules The game rules.
     * @param squadrons The patrol's squadrons.
     * @param patrolPath The patrol's path.
     */
    @Inject
    public AswPatrol(@Assisted final PatrolData data,
                               final @Named("asw") PatrolAirRules aswRules,
                               final GameRules gameRules,
                               final PatrolSquadrons squadrons,
                               final PatrolPath patrolPath) {
        airbase = data.getAirbase();

        this.rules = aswRules;
        this.gameRules = gameRules;
        this.squadrons = squadrons;
        this.patrolPath = patrolPath;

        maxRadius = squadrons.setSquadrons(data.getSquadrons(), airbase);
        calculatePath();
    }

    /**
     * Get the ASW patrol data that is written to a JSON file.
     *
     * @return The persistent ASW patrol data.
     */
    @Override
    public PatrolData getData() {
        PatrolData data = new PatrolData();
        data.setSquadrons(squadrons.getData());
        return  data;
    }

    /**
     * Get the Patrol type.
     *
     * @return The patrol type.
     */
    @Override
    public PatrolType getType() {
        return PatrolType.ASW;
    }

    /**
     * Get the squadrons on patrol.
     *
     * @return A list of squadrons on the patrol.
     */
    @Override
    public List<Squadron> getAssignedSquadrons() {
        return squadrons.get();
    }

    /**
     * Get the list of squadrons on ASW patrol for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on ASW patrol for the given nation.
     */
    @Override
    public List<Squadron> getAssignedSquadrons(final Nation nation) {
        return squadrons.get(nation);
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
        return squadrons.get(targetRadius);
    }

    /**
     * Add a squadron to the ASW patrol.
     *
     * @param squadron The squadron that is added to the patrol.
     */
    @Override
    public void addSquadron(final Squadron squadron) {
        if (canAdd(squadron)) {   //Make sure the squadron is actually deployed at the airbase.
            int newMaxRadius = squadrons.add(squadron, this);
            updateMaxRadius(newMaxRadius);
        } else {
            log.error("Unable to add squadron: '{}' to patrol. Squadron not deployed to airbase: '{}' or unable to perform ASW", squadron, airbase);
        }
    }

    /**
     * Remove a squadron from the ASW patrol.
     *
     * @param squadron The squadron removed from the patrol.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        int newMaxRadius = squadrons.remove(squadron);
        updateMaxRadius(newMaxRadius);
    }

    /**
     * Get the search success rate of the patrol given the distance to the target.
     *
     * @param distance The distance to the target.
     * @return The success rate of finding a submarine flotilla. An integer percentage.
     */
    @Override
    public int getSuccessRate(final int distance) {
        List<Squadron> inRange = getAssignedSquadrons(distance);
        return rules.getBaseSearchSuccess(distance, inRange);
    }

    /**
     * Get the patrol's true maximum squadron radius. This is the maximum radius
     * at which the patrol has a greater than 0 % chance to be successful.
     *
     * @return The patrol's true maximum radius.
     */
    @Override
    public int getTrueMaxRadius() {
        return IntStream.range(1, maxRadius + 1)
                .boxed()
                .sorted(Collections.reverseOrder())
                .filter(radius -> getSuccessRate(radius) > 0)
                .findFirst().orElse(0);
    }

    /**
     * Get the best allowed squadron configuration for this patrol.
     *
     * @return The best allowed squadron configuration for this patrols.
     */
    @Override
    public SquadronConfig getBestSquadronConfig() {
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO().setPatrolType(PatrolType.ASW);

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
        return rules.isAffectedByWeather();
    }

    /**
     * Clear all of the squadrons from this patrol.
     */
    @Override
    public void clearSquadrons() {
        int newMaxRadius = squadrons.clear();
        updateMaxRadius(newMaxRadius);
    }

    /**
     * Clear the squadrons of the given nation from this patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    @Override
    public void clearSquadrons(final Nation nation) {
        int newMaxRadius = squadrons.clear(nation);
        updateMaxRadius(newMaxRadius);
    }

    /**
     * Determine if the squadron may be added to the patrol.
     *
     * @param squadron The squadron that is potentially added to the patrol.
     * @return True if the given squadron may be added to this patrol. False otherwise.
     */
    private boolean canAdd(final Squadron squadron) {
        return squadron.canDoPatrol(PatrolType.ASW) && airbase.getSquadrons().contains(squadron);
    }

    /**
     * Calculate the patrol's grid path.
     */
    private void calculatePath() {
        gridPath = patrolPath.getGrids(this);
    }

    /**
     * Update the patrols maximum radius if it has changed.
     * If the maximum radius has changed then re-calculate the patrol's grid path.
     *
     * @param newMaxRadius The patrol's new maximum radius.
     */
    private void updateMaxRadius(final int newMaxRadius) {
        if (maxRadius != newMaxRadius) {
            maxRadius = newMaxRadius;
            calculatePath();
        }
    }
}
