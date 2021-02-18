package engima.waratsea.model.taskForce.patrol;

import engima.waratsea.model.base.AirbaseGroup;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.stats.PatrolStats;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;

import java.util.List;

public interface PatrolGroup {
    /**
     * Get the patrol group's title.
     *
     * @return The patrol group's title.
     */
    String getTitle();

    /**
     * Get the type of patrols contained within this patrol group.
     *
     * @return The type of patrol.
     */
    PatrolType getType();

    /**
     * Get the airbase group for which this patrol group is associated.
     *
     * @return The airbase group of this patrol group.
     */
    AirbaseGroup getAirbaseGroup();

    /**
     * Get the home airbase group. The home airbase group is only really meaningful when several task forces
     * are co-located at the same game grid. Together these task forces form a task force group which is an
     * airbase group. This method returns one of those task forces which is itself an airbase group.
     *
     * This is needed for relating the patrol group to a task force marker on the game map.
     * Task force groups set one of the task forces as the home group. Note, a task force group
     * has no meaning on the game map.
     *
     * @return The home airbase group of this patrol group.
     */
    AirbaseGroup getHomeGroup();

    /**
     * Get the squadrons in this patrol group.
     *
     * @return The patrol group squadrons.
     */
    List<Squadron> getSquadrons();

    /**
     * Determine if this patrol group contains any squadrons for the given nation.
     *
     * @param nation A nation: BRITISH, ITALIAN, et...
     * @return True if the given nation contains squadrons within this patrol group. False otherwise.
     */
    boolean areSquadronsPresent(Nation nation);

    /**
     * Get the patrol's groups true maximum squadron radius. This is the maximum radius
     * at which the patrol group has a greater than 0 % chance to be successful.
     *
     * @return The patrol's true maximum radius.
     */
    int getTrueMaxRadius();

    /**
     * Get the patrol stats for this group.
     *
     * @return The patrol stats for this patrol group.
     */
    PatrolStats getPatrolStats();
}
