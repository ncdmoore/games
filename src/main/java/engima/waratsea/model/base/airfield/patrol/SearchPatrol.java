package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.base.airfield.patrol.rules.AirRules;
import engima.waratsea.model.base.airfield.patrol.rules.AirRulesFactory;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class SearchPatrol implements Patrol {

    private List<Squadron> squadrons;

    @Getter
    private final Airbase airbase;

    @Getter
    private int maxRadius;

    private AirRules rules;

    /**
     * The constructor.
     *
     * @param data The search patrol data read in from a JSON file.
     * @param airRulesFactory The air search rules factory.
     */
    @Inject
    public SearchPatrol(@Assisted final PatrolData data,
                                  final AirRulesFactory airRulesFactory) {

        this.rules = airRulesFactory.createSearch();

        airbase = data.getAirbase();

        Map<String, Squadron> squadronMap = getSquadronMap(data.getAirbase().getSquadrons());

        squadrons = Optional.ofNullable(data.getSquadrons())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronMap::get)
                .collect(Collectors.toList());

        updateMaxRadius();
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
     * Get the squadrons on patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on the patrol.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation nation) {
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
            SquadronState state = squadron.getSquadronState().transition(SquadronAction.ASSIGN_TO_PATROL);
            squadron.setSquadronState(state);
            updateMaxRadius();
        } else {
            log.error("Unable to add squadron: '{}' to patrol. Squadron not deployed to airbase: '{}'", squadron, airbase);
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
        SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_PATROL);
        squadron.setSquadronState(state);
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
    public List<Squadron> getSquadrons(final int targetRadius) {
        return squadrons
                .stream()
                .filter(squadron -> squadron
                        .getRadius()
                        .stream()
                        .anyMatch(radius -> radius >= targetRadius))
                .collect(Collectors.toList());
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
     * Determine if the patrol is adversely affected by the current weather conditions.
     *
     * @return True if the patrol is affected by the current weather conditions. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return rules.isAffectedByWeather();
    }

    /**
     * Get the rate of success for this patrol's air search.
     *
     * @param radius The distance the target is from the patrol's base.
     * @return An integer representing the percentage success rate of finding a task force.
     */
    @Override
    public int getSuccessRate(final int radius) {
        List<Squadron> inRange = getSquadrons(radius);
        return rules.getBaseSearchSuccess(radius, inRange);
    }

    /**
     * Get the patrol data.
     *
     * @return A map of data for this patrol.
     */
    @Override
    public Map<Integer, Map<String, String>> getPatrolStats() {
        return IntStream
                .range(1, maxRadius + 1)
                .boxed()
                .collect(Collectors.toMap(radius -> radius, this::getPatrolStat));
    }

    /**
     * Get the patrol data that corresponds to the given radius. This is the
     * data for a patrol that takes place at the given radius.
     *
     * @param radius The patrol radius.
     * @return A map of data for this patrol that corresponds to the given radius.
     */
    private Map<String, String> getPatrolStat(final int radius) {
        List<Squadron> inRange = getSquadrons(radius);

        Map<String, String> data = new LinkedHashMap<>();
        data.put("Squadrons", inRange.size() + "");
        data.put("Steps", inRange.stream().map(Squadron::getSteps).reduce(BigDecimal.ZERO, BigDecimal::add) + "");
        data.put("Search", getSuccessRate(radius) + " %");
        data.put("No Weather", rules.getBaseSearchSuccessNoWeather(radius, inRange) + "%");

        return data;
    }

    /**
     * Get a squadron map to aid in determine which squadrons of the airfield
     * are on search patrol.
     *
     * @param squadronList The airfield squadrons.
     * @return A map of squadron name to squadron.
     */
    private Map<String, Squadron> getSquadronMap(final List<Squadron> squadronList) {
        return squadronList
                .stream()
                .collect(Collectors.toMap(Squadron::getName, squadron -> squadron));
    }

    /**
     * Determine if the squadron may be added to the patrol.
     *
     * @param squadron The squadron that is potentially added to the patrol.
     * @return True if the given squadron may be added to this patrol. False otherwise.
     */
    private boolean canAdd(final Squadron squadron) {
        return squadron.canDoPatrol(PatrolType.SEARCH) && airbase.getSquadrons().contains(squadron);
    }

    /**
     * Update this search's maximum search radius. If the newly added squadron has a greater
     * readius then the current maximum search radius, then this squadron's search radius
     * is the new maximum search radius.
     **/
    private void updateMaxRadius() {
        maxRadius = squadrons
                .stream()
                .flatMap(s -> s.getRadius().stream())
                .max(Integer::compare)
                .orElse(0);

    }
}
