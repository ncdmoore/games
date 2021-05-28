package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.MissionSquadrons;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirRules;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.rules.GameRules;
import engima.waratsea.model.game.rules.SquadronConfigRulesDTO;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Slf4j
public class CapPatrol implements Patrol {
    private static final LinkedHashSet<SquadronConfig> VALID_SQUADRON_CONFIGS = new LinkedHashSet<>(Collections.singletonList(SquadronConfig.NONE));

    private static final int RADIUS = 2;

    private final PatrolAirRules capRules;
    private final GameRules gameRules;
    private final PatrolSquadrons squadrons;
    private final PatrolPath patrolPath;

    @Getter private final Airbase airbase;
    @Getter private int maxRadius;

    /**
     * The constructor.
     *
     * @param data The CAP patrol data read in from a JSON file.
     * @param capRules The CAP air rules.
     * @param gameRules The game rules.
     * @param squadrons The squadrons on patrol.
     * @param patrolPath The patrol's grid path.
     */
    @Inject
    public CapPatrol(@Assisted final PatrolData data,
                               final @Named("cap") PatrolAirRules capRules,
                               final GameRules gameRules,
                               final PatrolSquadrons squadrons,
                               final PatrolPath patrolPath) {
        this.capRules = capRules;
        this.gameRules = gameRules;
        this.squadrons = squadrons;
        this.patrolPath = patrolPath;

        airbase = data.getAirbase();

        squadrons.setSquadrons(data.getSquadrons(), airbase);

        setMaxRadius();
    }

    /**
     * Get the Patrol data.
     *
     * @return The Patrol data.
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
        return PatrolType.CAP;
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
     * Get the squadrons on patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on the patrol.
     */
    @Override
    public List<Squadron> getAssignedSquadrons(final Nation nation) {
        return squadrons.get(nation);
    }

    /**
     * Add a squadron to the patrol.
     *
     * @param squadron The squadron added to the patrol.
     */
    @Override
    public void addSquadron(final Squadron squadron) {
        if (canAdd(squadron)) {   //Make sure the squadron is actually deployed at the airbase.
            squadrons.add(squadron, this);
            updateMaxRadius();
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
        updateMaxRadius();
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
        return (targetRadius <= RADIUS) ?  squadrons.get() : Collections.emptyList();
    }

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
     * Clear all of the squadrons from this patrol.
     */
    @Override
    public void clearSquadrons() {
        squadrons.clear();
        updateMaxRadius();
    }

    /**
     * Clear the squadrons of the given nation from this patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    @Override
    public void clearSquadrons(final Nation nation) {
        squadrons.clear(nation);
        updateMaxRadius();
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
                .findFirst().orElse(0);
    }

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

    public void intercept(final MissionSquadrons enemySquadrons) {

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

    /**
     * Set the patrol's max radius.
     */
    private void setMaxRadius() {
        maxRadius = squadrons.isNotEmpty() ? RADIUS : 0;
        calculatePath();
    }

    /**
     * Calculate the patrol's grid path.
     */
    private void calculatePath() {
        patrolPath.buildGrids(this);
    }

    /**
     * Update the patrols maximum radius if it has changed.
     * If the maximum radius has changed then re-calculate the patrol's grid path.
     */
    private void updateMaxRadius() {
        int newMaxRadius = squadrons.isNotEmpty() ? RADIUS : 0;

        if (maxRadius != newMaxRadius) {
            maxRadius = newMaxRadius;
            calculatePath();
        }
    }
}
