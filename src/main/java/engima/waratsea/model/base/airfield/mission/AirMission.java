package engima.waratsea.model.base.airfield.mission;

import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;

import java.util.List;

public interface AirMission extends PersistentData<MissionData> {
    /**
     * Get the persistent mission data.
     *
     * @return The persistent mission data.
     */
    MissionData getData();

    /**
     * Get the airbase from which the mission was launched.
     *
     * @return The airbase from which the mission was launched.
     */
    Airbase getAirbase();

    /**
     * Get the mission's type.
     *
     * @return The type of mission.
     */
    AirMissionType getType();

    /**
     * Get the mission's target.
     *
     * @return The mission's target.
     */
    Target getTarget();

    /**
     * Get the nation that is performing this mission.
     *
     * @return The nation: BRITISH, ITALIAN, etc...
     */
    Nation getNation();

    /**
     * Get the squadrons on the mission that are assigned the given role.
     *
     * @param role The squadron's mission role.
     * @return A list of squadrons on the mission.
     */
    List<Squadron> getSquadrons(MissionRole role);

    /**
     * Get all of the squadrons, all roles on the mission.
     *
     * @return All of the squadrons involved with this mission.
     */
    List<Squadron> getSquadronsAllRoles();

    /**
     * Get the number of steps assigned to this mission.
     *
     * @return the total number of steps assigned to this mission.
     */
    int getSteps();

    /**
     * Set all of the squadrons to the correct state.
     */
    void addSquadrons();

    /**
     * Remove all the squadrons from the mission.
     */
    void removeSquadrons();

    /**
     * Get the number of squadron in the mission.
     *
     * @return The number of squadrons in the mission.
     */
    int getNumber();

    /**
     * Get the probability of success for this mission.
     *
     * @return The probability that this mission will be successful.
     */
    List<ProbabilityStats> getMissionProbability();
}
