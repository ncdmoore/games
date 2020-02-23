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
import engima.waratsea.model.target.Target;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents airbases. An airbase may be an airfield (land based airbase) or may be an aircraft carrier.
 */
public interface Airbase extends Base {

    /**
     * Get the type of airbase. The type of airbase indicates what types of squadrons (landing type) the airbase
     * supports.
     *
     * @return The type of airbase.
     */
    AirfieldType getAirfieldType();

    /**
     * Get the landing types supported by this airbase.
     *
     * @return The supported landing types of this airbase.
     */
    List<LandingType> getLandingType();

    /**
     * The name of the airbase. The name of the airbase uniquely identifies the airbase per side.
     *
     * @return The name of the airbase.
     */
    String getName();

    /**
     * The title of the airbase. The title of the airbase is used in the GUI to indicate the identity of
     * the airbase.
     *
     * @return The title of the airbase.
     */
    String getTitle();

    /**
     * The side of the airbase. This is the owning side of this airbase.
     *
     * @return The airbase side: ALLIES or AXIS.
     */
    Side getSide();

    /**
     * Get the airbase's nations. These are the nations that are permitted to station squadrons at this airbase.
     * Not all nations are allowed to station squadrons at all airbase's.
     *
     * @return The nations that allowed to station squadrons at this airbase.
     */
    Set<Nation> getNations();

    /**
     * Get the given nations region. This is the nation's region of which this airbase is included.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The region that corresponds to the given nation.
     */
    Region getRegion(Nation nation);

    /**
     * The maximum capacity of the airbase. This is the undamaged squadron step capacity of this airbase.
     *
     * @return The maximum squadron step capacity of this airbase.
     */
    int getMaxCapacity();

    /**
     * The current squadron step capacity of the airbase. A base's capacity to station
     * squadrons may be reduced via air attack or naval bombardment. This method returns the
     * current capacity taking into account any damage or repairs from air attacks or
     * bombardments.
     *
     * @return The current capacity of the airbase in steps.
     */
    int getCapacity();

    /**
     * Get the current number of squadron steps stationed at this airbase. This is
     * the total number of squadron steps of all squadrons currently stationed at
     * this airbase. This does not take into account any squadron steps that
     * are given a ferry mission to this airbase. The returned value includes
     * squadrons steps for all nations currently using this airbase.
     *
     * @return The current number of steps deployed at this airbase.
     */
    BigDecimal getCurrentSteps();

    /**
     * Determine if the airbase is at capacity, meaning the maximum
     * number of squadron steps that may be stationed at the airbase
     * are stationed at the airbase. This does not take into account
     * any squadron steps that are given a ferry mission to this airbase.
     * This does take into account all nations squadrons.
     *
     * @return True if this airbase contains its maximum number of squadron steps.
     */
    boolean isAtCapacity();

    /**
     * Determine if the given squadron can be stationed at this airbase.
     * This does not take into account any squadron steps that are given
     * a ferry mission to this airbase.
     *
     * @param squadron the squadron to station at this airbase.
     * @return The results of the squadron station operation. Success if
     * the squadron can be stationed. Otherwise an error code is returned.
     */
    AirfieldOperation canStation(Squadron squadron);

    /**
     * Indicates if this airbase has any squadrons currently stationed.
     * This includes all nations that may station squadrons at this airbase.
     * This does not take into account any squadrons given a ferry mission
     * to this airbase.
     *
     * @return True if any squadron is based at this airbase. False otherwise.
     */
    boolean areSquadronsPresent();

    /**
     * Get a squadron stationed at this airbase given its name.
     *
     * @param squadronName The squadron name.
     * @return The squadron that corresponds to the given squadron name.
     */
    Squadron getSquadron(String squadronName);

    /**
     * Get all the squadrons currently stationed at this airbase. This includes all nations.
     *
     * @return The all squadrons currently stationed at this airbase.
     */
    List<Squadron> getSquadrons();

    /**
     * Get the list of squadrons currently stationed at this airbase for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The squadrons currently stationed at this airbase for the given nation.
     */
    List<Squadron> getSquadrons(Nation nation);

    /**
     * Get the list of squadrons currently stationed at this airbase for the given nation and given state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state. Only squadrons in this state are included in the squadrons returned.
     * @return The squadrons for the given nation and in the given state.
     */
    List<Squadron> getSquadrons(Nation nation, SquadronState state);

    /**
     * Get a list of squadrons for the given nation that can perform the given patrol type.
     * Not all squadrons can perform all patrols. This method returns the squadrons stationed
     * at this airbase for the given nation that can perform the specified patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param patrolType The type of patrol.
     * @return The squadrons for the given nation that can perform the given patrol.
     */
    List<Squadron> getReadySquadrons(Nation nation, PatrolType patrolType);

    /**
     * Get the squadron map for the given nation and given squadron state. This is map of
     * aircraft type to list of this type of aircraft type squadrons.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    Map<AircraftType, List<Squadron>> getSquadronMap(Nation nation, SquadronState state);

    /**
     * Add a squadron to this airbase. Station the given squadron at this airbase.
     * Note, if the airbase does not support the squadron landing type or if the
     * airbase does not have capacity to station the squadron, then the squadron will
     * not be stationed.
     *
     * @param squadron The squadron that is now based at this airbase.
     * @return True if the squadron was added. False otherwise.
     */
    AirfieldOperation addSquadron(Squadron squadron);

    /**
     * Remove a squadron from this airbase.
     *
     * @param squadron The squadron that is removed from this airbase.
     */
    void removeSquadron(Squadron squadron);

    /**
     * Get the current missions of this airbase for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The current squadron missions of this airbase.
     */
    List<Mission> getMissions(Nation nation);

    /**
     * Add a mission to this airbase.
     *
     * @param mission The mission that is added to this airbase.
     */
    void addMission(Mission mission);

    /**
     * Get the total number of squadron steps on missions assigned to the given target.
     * This is the total number of squadron steps from all missions that have
     * the given target as their assigned target.
     *
     * @param target The mission target.
     * @return The total number of squadron steps on missions assigned to the given target.
     */
    int getTotalMissionSteps(Target target);

    /**
     * Get this airbase's patrol specified by the given patrol type.
     *
     * @param patrolType The type of patrol.
     * @return The patrol that corresponds to the given patrol type.
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
     * Clear all of the patrols and missions on this airbase. For patrols this removes all squadrons
     * from patrols. For missions this removes all missions and therefore removes all the squadrons
     * on the removed missions.
     */
    void clearPatrolsAndMissions();

    /**
     * Get the airbase's anti aircraft rating.
     *
     * @return The airbase's anti aircraft rating.
     */
    int getAntiAirRating();
}
