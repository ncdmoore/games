package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;

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
     * Get the Patrol's maximum squadron radius.
     *
     * @return The Patrol's maximum squadron radius.
     */
    int getMaxRadius();
}
