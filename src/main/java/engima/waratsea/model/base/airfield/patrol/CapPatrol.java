package engima.waratsea.model.base.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.base.airfield.patrol.rules.PatrolAirRules;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class CapPatrol implements Patrol {
    private static final int RADIUS = 2;

    private final PatrolAirRules rules;

    private List<Squadron> squadrons;

    @Getter
    private final Airbase airbase;

    @Getter
    private int maxRadius;

    /**
     * The constructor.
     *
     * @param data The CAP patrol data read in from a JSON file.
     * @param rules The CAP air rules.
     */
    @Inject
    public CapPatrol(@Assisted final PatrolData data,
                               final @Named("cap") PatrolAirRules rules) {
        this.rules = rules;

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
        SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_PATROL);
        squadron.setSquadronState(state);

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
    public List<Squadron> getSquadrons(final int targetRadius) {
        return (targetRadius <= RADIUS) ?  squadrons : Collections.emptyList();
    }

    /**
     * Get the search success rate of the patrol given the distance to the target.
     *
     * @param distance The distance to the target.
     * @return The success rate.
     */
    @Override
    public int getSuccessRate(final int distance) {
        List<Squadron> inRange = getSquadrons(distance);
        return rules.getBaseSearchSuccess(distance, inRange);
    }

    /**
     * Get the patrol data.
     *
     * @return A map of data for this patrol.
     */
    @Override
    public Map<Integer, Map<String, String>> getPatrolStats() {

        if (squadrons.isEmpty()) {
            return new HashMap<>();
        }

        return IntStream
                .range(0, maxRadius + 1)
                .boxed()
                .collect(Collectors.toMap(radius -> radius, this::getPatrolStat));
    }

    /**
     * Clear all of the squadrons from this patrol.
     */
    @Override
    public void clearSquadrons() {
        squadrons.forEach(squadron -> {
            SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_PATROL);
            squadron.setSquadronState(state);
        });

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
                .peek(squadron -> {
                    SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_PATROL);
                    squadron.setSquadronState(state);
                }).collect(Collectors.toList());

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
    private Map<String, String> getPatrolStat(final int radius) {
        List<Squadron> inRange = getSquadrons(radius);

        Map<String, String> data = new LinkedHashMap<>();
        data.put("Squadrons", inRange.size() + "");
        data.put("Steps", inRange.stream().map(Squadron::getSteps).reduce(BigDecimal.ZERO, BigDecimal::add) + "");
        data.put("Intercept", getSuccessRate(radius) + " %");
        data.put("No Weather", rules.getBaseSearchSuccessNoWeather(radius, inRange) + "%");

        return data;
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
     * Determine if the patrol is adversely affected by the current weather conditions.
     *
     * @return True if the patrol is affected by the current weather conditions. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return rules.isAffectedByWeather();
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
