package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;

import java.util.List;
import java.util.Map;

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
     * Get the squadrons on patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on the patrol.
     */
    List<Squadron> getSquadrons(Nation nation);

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
     * Determine which squadrons on patrol can reach the given target radius.
     *
     * @param targetRadius A patrol radius for which each squadron on patrol is
     *                     checked to determine if the squadron can reach the
     *                     radius.
     * @return A list of squadrons on patrol that can reach the given target radius.
     */
    List<Squadron> getSquadrons(int targetRadius);

    /**
     * Get the search success rate of the patrol given the distance to the target.
     *
     * @param distance The distance to the target.
     * @return The success rate an integer percentage.
     */
    int getSuccessRate(int distance);

    /**
     * Get the patrol data.
     *
     * @return A map of data for this patrol.
     */
    Map<Integer, Map<String, String>> getPatrolStats();

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
}
