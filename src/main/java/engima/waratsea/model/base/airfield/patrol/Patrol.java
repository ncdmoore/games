package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;

import java.util.List;

public interface Patrol {
    /**
     * Get the Patrol data.
     *
     * @return The Patrol data.
     */
    PatrolData getData();

    /**
     * Get the air base of this patrol.
     *
     * @return This patrol's air base.
     */
    Airbase getAirbase();

    /**
     * Get the Patrol type.
     *
     * @return The patrol type.
     */
    PatrolType getType();

    /**
     * Get the squadrons on patrol.
     *
     * @return A list of squadrons on the patrol.
     */
    List<Squadron> getAssignedSquadrons();

    /**
     * Get the squadrons on patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on the patrol.
     */
    List<Squadron> getAssignedSquadrons(Nation nation);

    /**
     * Determine which squadrons on patrol can reach the given target radius.
     *
     * @param targetRadius A patrol radius for which each squadron on patrol is
     *                     checked to determine if the squadron can reach the
     *                     radius.
     * @return A list of squadrons on patrol that can reach the given target radius.
     */
    List<Squadron> getAssignedSquadrons(int targetRadius);

    /**
     * Add a squadron to the patrol.
     *
     * @param squadron The squadron added to the patrol.
     */
    void addSquadron(Squadron squadron);

    /**
     * Remove a squadron from the patrol.
     *
     * @param squadron The squadron removed from the patrol.
     */
    void removeSquadron(Squadron squadron);

    /**
     * Get the search success rate of the patrol given the distance to the target.
     *
     * @param distance The distance to the target.
     * @return The success rate an integer percentage.
     */
    int getSuccessRate(int distance);

    /**
     * Get the Patrol's maximum squadron radius. This is the radius of the squadron
     * on this patrol that has the largest radius.
     *
     * @return The Patrol's maximum squadron radius.
     */
    int getMaxRadius();

    /**
     * Get the patrol's true maximum squadron radius. This is the maximum radius
     * at which the patrol has a greater than 0 % chance to be successful.
     *
     * @return The patrol's true or effective maximum radius.
     */
    int getTrueMaxRadius();

    /**
     * Get the best allowed squadron configuration for this patrol.
     *
     * @return The best allowed squadron configuration for this patrols.
     */
    SquadronConfig getBestSquadronConfig();

    /**
     * Determine if the patrol is adversely affected by the current weather conditions.
     *
     * @return True if the patrol is affected by the current weather conditions. False otherwise.
     */
    boolean isAffectedByWeather();

    /**
     * Clear all of the squadrons from this patrol.
     */
    void clearSquadrons();

    /**
     * Clear the squadrons of the given nation from this patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    void clearSquadrons(Nation nation);
}
