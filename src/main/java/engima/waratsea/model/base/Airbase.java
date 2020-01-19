package engima.waratsea.model.base;

import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.base.airfield.mission.Mission;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronState;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents air bases.
 */
public interface Airbase extends Base {

    /**
     * Get the type of airbase.
     *
     * @return The type of airbase.
     */
    AirfieldType getAirfieldType();

    /**
     * Get the landing types supported by this airbase.
     *
     * @return A list of supported landing types.
     */
    List<LandingType> getLandingType();

    /**
     * The name of the air base.
     *
     * @return The name of the air base.
     */
    String getName();

    /**
     * The title of the air base.
     *
     * @return The title of the air base.
     */
    String getTitle();

    /**
     * The side of the air base.
     *
     * @return The air base side: ALLIES or AXIS.
     */
    Side getSide();

    /**
     * Get the air base's nations.
     *
     * @return The air base's nations.
     */
    Set<Nation> getNations();

    /**
     * Get the given nations region.
     *
     * @param nation The nation.
     * @return The region that corresponds to the given nation.
     */
    Region getRegion(Nation nation);

    /**
     * The maximum capacity of the air base.
     *
     * @return The maximum capacity of the air base.
     */
    int getMaxCapacity();

    /**
     * The current capacity of the air base.
     *
     * @return The current capacity of the air base in steps.
     */
    int getCapacity();

    /**
     * Get the current number of steps deployed at this air base.
     *
     * @return The current number of steps deployed at this air base.
     */
    BigDecimal getCurrentSteps();

    /**
     * Indicates if this airbase has any squadrons.
     *
     * @return True if any squadron is based at this airbase. False otherwise.
     */
    boolean areSquadronsPresent();

    /**
     * Get the squadron given its name.
     *
     * @param squadronName The squadron name.
     * @return The squadron that corresponds to the given squadron name.
     */
    Squadron getSquadron(String squadronName);

    /**
     * Get all the squadrons stationed at this airbase. This includes all nations.
     *
     * @return The list of all the squadrons stationed at this airbase.
     */
    List<Squadron> getSquadrons();

    /**
     * Get the list of squadrons for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The squadron list for the given nation.
     */
    List<Squadron> getSquadrons(Nation nation);

    /**
     * Get the list of squadrons for the given nation and given state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return A list of squadron for the given nation and given state.
     */
    List<Squadron> getSquadrons(Nation nation, SquadronState state);

    /**
     * Get a list of squadrons for the given nation that can perform the given patrol type.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param patrolType The type of patrol.
     * @return A list of squadrons for the given nation that can perform the given patrol.
     */
    List<Squadron> getReadySquadrons(Nation nation, PatrolType patrolType);

    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    Map<AircraftType, List<Squadron>> getSquadronMap(Nation nation, SquadronState state);

    /**
     * Add a squadron to this air base.
     *
     * @param squadron The squadron that is now based at this airbase.
     * @return True if the squadron was added. False otherwise.
     */
    AirfieldOperation addSquadron(Squadron squadron);

    /**
     * Remove a squadron from this air base.
     *
     * @param squadron The squadron that is removed from this airbase.
     */
    void removeSquadron(Squadron squadron);

    /**
     * Get the current missions of this air base.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the current missions.
     */
    List<Mission> getMissions(Nation nation);

    /**
     * Add a mission to this air base.
     *
     * @param mission The mission that is added to this airbase.
     */
    void addMission(Mission mission);

    /**
     * Remove a misson from this air base.
     *
     * @param mission The mission that is removed from this airbase.
     */
    void removeMission(Mission mission);

    /**
     * Get the Patrol specified by the given patrol type.
     *
     * @param patrolType The type of patrol.
     * @return The Patrol that corresponds to the given patrol type.
     */
    Patrol getPatrol(PatrolType patrolType);

    /**
     * This is a utility function to aid in determining patrol stats for squadrons that are
     * selected for a given patrol type but not necessarily committed to the patrol yet.
     *
     * @param patrolType The type of patrol.
     * @param squadronOnPatrol A list of potential squadrons on patrol.
     * @return A patrol consisting of the given squadrons.
     */
    Patrol getTemporaryPatrol(PatrolType patrolType, List<Squadron> squadronOnPatrol);

    /**
     * Clear all of the patrols and missions on this airbase.
     */
    void clearPatrolsAndMissions();

    /**
     * Get the air base's anti aircraft rating.
     *
     * @return The air base's anti aircraft rating.
     */
    int getAntiAirRating();
}
