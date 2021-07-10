package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Not all patrol types are virtual. For a patrol type that is not virtual the Patrols container class
 * will return one of these: an empty patrol. This is used to keep the classes that call methods on
 * patrols simple and not needing to check for null all over the place.
 */
public class EmptyPatrol implements Patrol {
    @Getter private final PatrolType type;
    @Getter private final Airbase airbase;               //The airbase from which the patrol is flown.
    @Getter private final int maxRadius = 0;

    EmptyPatrol(final PatrolType type, final Airbase airbase) {
        this.type = type;
        this.airbase = airbase;
    }

    /**
     * Get the Patrol data.
     *
     * @return The Patrol data.
     */
    @Override
    public PatrolData getData() {
        PatrolData data = new PatrolData();
        data.setSquadrons(Collections.emptyList());
        return  data;
    }

    /**
     * Get the squadrons on patrol.
     *
     * @return A list of squadrons on the patrol.
     */
    @Override
    public List<Squadron> getAssignedSquadrons() {
        return Collections.emptyList();
    }

    /**
     * Get the squadrons on patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on the patrol.
     */
    @Override
    public List<Squadron> getAssignedSquadrons(final Nation nation) {
        return Collections.emptyList();
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
        return Collections.emptyList();
    }

    /**
     * Add a squadron to the patrol.
     *
     * @param squadron The squadron added to the patrol.
     */
    @Override
    public void addSquadron(final Squadron squadron) {
    }

    /**
     * Remove a squadron from the patrol.
     *
     * @param squadron The squadron removed from the patrol.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
    }

    /**
     * Get the search success rate of the patrol given the distance to the target.
     *
     * @param distance The distance to the target.
     * @return The success rate an integer percentage.
     */
    @Override
    public int getSuccessRate(final int distance) {
        return 0;
    }

    /**
     * Get the patrol's true maximum squadron radius. This is the maximum radius
     * at which the patrol has a greater than 0 % chance to be successful.
     *
     * @return The patrol's true or effective maximum radius.
     */
    @Override
    public int getTrueMaxRadius() {
        return 0;
    }

    /**
     * Get the best allowed squadron configuration for this patrol.
     *
     * @return The best allowed squadron configuration for this patrols.
     */
    @Override
    public SquadronConfig getBestSquadronConfig() {
        return SquadronConfig.NONE;
    }

    /**
     * Determine if the patrol is adversely affected by the current weather conditions.
     *
     * @return True if the patrol is affected by the current weather conditions. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return false;
    }

    /**
     * Clear all of the squadrons from this patrol.
     */
    @Override
    public void clearSquadrons() {
    }

    /**
     * Clear the squadrons of the given nation from this patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    @Override
    public void clearSquadrons(final Nation nation) {
    }
}
