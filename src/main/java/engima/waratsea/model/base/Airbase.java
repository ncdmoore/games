package engima.waratsea.model.base;

import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronHome;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents airbases. An airbase may be an airfield (land based airbase) or may be an aircraft carrier.
 */
public interface Airbase extends Base, SquadronHome {

    /**
     * Get the airbase's game grid.
     *
     * @return The airbase's game grid.
     */
    Optional<GameGrid> getGrid();

    /**
     * Get the landing types supported by this airbase.
     *
     * @return The supported landing types of this airbase.
     */
    List<LandingType> getLandingType();

    /**
     * Get the airbase's nations. These are the nations that are permitted to station squadrons at this airbase.
     * Not all nations are allowed to station squadrons at all airbase's.
     *
     * @return The nations that allowed to station squadrons at this airbase.
     */
    Set<Nation> getNations();

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
     * Get a map of nation to list of squadrons for that nation.
     *
     * @return A map of nation to list of squadrons.
     */
    Map<Nation, List<Squadron>> getSquadronMap();

    /**
     * Get the squadron map for the given nation. This is map of
     * aircraft type to a list of squadrons of the type of aircraft.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A squadron map keyed by aircraft type to list of squadrons of that type.
     */
    Map<AircraftType, List<Squadron>> getSquadronMap(Nation nation);

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
     * Get the current missions of this airbase for all nations.
     *
     * @return The current squadron missions of this airbase.
     */
    List<AirMission> getMissions();

    /**
     * Get the current missions of this airbase for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The current squadron missions of this airbase for the given nation.
     */
    List<AirMission> getMissions(Nation nation);

    /**
     * Add a mission to this airbase.
     *
     * @param mission The mission that is added to this airbase.
     */
    void addMission(AirMission mission);

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
     * Clear all missions for this airbase. This removes all the squadrons from all missions.
     */
    void clearMissions();

    /**
     * Clear all of the patrols for this airbase. This removes all the squadrons from the patrols.
     */
    void clearPatrols();

    /**
     * Upddate the patrol.
     *
     * @param patrolType The type of patrol.
     * @param squadrons The squadrons on patrol.
     */
    void updatePatrol(PatrolType patrolType, List<Squadron> squadrons);
}
