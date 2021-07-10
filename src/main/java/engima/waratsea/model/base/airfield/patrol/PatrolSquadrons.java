package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A utility class for handling patrol squadrons.
 */
public class PatrolSquadrons {
    private List<Squadron> squadrons;   //The squadrons that are on this patrol.

    /**
     * Set the squadrons for this patrol.
     *
     * @param squadronNames The squadron names on this patrol.
     * @param airbase The airbase that launches this patrol.
     * @return The maximum patrol radius.
     */
    public int setSquadrons(final List<String> squadronNames, final Airbase airbase) {
        this.squadrons = Optional.ofNullable(squadronNames)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(airbase::getSquadron)
                .collect(Collectors.toList());

        return getMaxRadius();
    }

    /**
     * Get the persistent data for this patrol.
     *
     * @return The names of the squadrons on patrol.
     */
    public List<String> getData() {
        return Optional
                .ofNullable(squadrons)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get the squadrons on this patrol.
     *
     * @return The squadrons on this patrol.
     */
    public List<Squadron> get() {
        return squadrons;
    }

    /**
     * Get the squadrons on this patrol for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The given nations squadrons on this patrol.
     */
    public List<Squadron> get(final Nation nation) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .collect(Collectors.toList());
    }

    /**
     * Determine which squadrons on patrol can reach the given target radius.
     *
     * @param targetRadius A patrol radius for which each squadron on patrol is
     *                     checked to determine if the squadron can reach the
     *                     radius.
     * @return A list of squadrons on patrol that can reach the given target radius.
     */
    public List<Squadron> get(final int targetRadius) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.getRadius() >= targetRadius)
                .collect(Collectors.toList());
    }

    /**
     * Indicates if no squadrons are on this patrol.
     *
     * @return True if there are no squadrons on this patrol. False otherwise, there are squadrons on patrol.
     */
    public boolean isEmpty() {
        return squadrons.isEmpty();
    }

    /**
     * Indicates if any squadrons are on this patrol.
     *
     * @return True if there are squadrons on this patrol. False otherwise.
     */
    public boolean isNotEmpty() {
        return !squadrons.isEmpty();
    }

    /**
     * Add the given squadron to the patrol.
     *
     * @param squadron The added squadron.
     * @return The new maximum radius of this patrol.
     */
    public int add(final Squadron squadron) {
        squadrons.add(squadron);
        return getMaxRadius();
    }

    /**
     * Remove a squadron from this patrol.
     *
     * @param squadron The squadron removed from this patrol.
     * @return The new maximum radius of this patrol.
     */
    public int remove(final Squadron squadron) {
        squadrons.remove(squadron);
        return getMaxRadius();
    }

    /**
     * Update the squadrons state.
     *
     * @param action A squadron action.
     */
    public void doAction(final SquadronAction action) {
        squadrons.forEach(squadron -> squadron.setState(action));
    }

    /**
     * Update the squadrons state.
     *
     * @param nation The nation.
     * @param action A squadron action.
     */
    public void doAction(final Nation nation, final SquadronAction action) {
        squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .forEach(squadron -> squadron.setState(action));
    }

    /**
     * Clear all of the squadrons from this patrol.
     *
     * @return The new maximum radius of this patrol: 0.
     */
    public int clear() {
        squadrons.clear();
        return 0;
    }

    /**
     * Clear the squadrons of the given nation from this patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The new maximum radius of this patrol.
     */
    public int clear(final Nation nation) {
        List<Squadron> toRemove = get(nation);
        squadrons.removeAll(toRemove);
        return getMaxRadius();
    }

    /**
     * Get the maximum radius from the squadron on this patrol.
     *
     * @return The maximum radius of all the squadrons on this patrol.
     */
    private int getMaxRadius() {
        return squadrons
                .stream()
                .map(Squadron::getRadius)
                .max(Integer::compare)
                .orElse(0);
    }
}
