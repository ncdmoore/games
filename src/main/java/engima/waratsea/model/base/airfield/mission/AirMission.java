package engima.waratsea.model.base.airfield.mission;

import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.state.AirMissionAction;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;

import java.util.List;

/**
 * This class represents an air mission. For example, an air strike on an airbase.
 */
public interface AirMission extends PersistentData<MissionData> {
    /**
     * Get the persistent mission data.
     *
     * @return The persistent mission data.
     */
    MissionData getData();

    /**
     * Get the air mission id. The mission id uniquely identifies a mission.
     *
     * @return The air mission's id.
     */
    int getId();

    /**
     * Get the air mission's current state.
     *
     * @return The air mission state.
     */
    AirMissionState getState();

    /**
     * Instruct the mission to carry out the given action.
     *
     * @param action The action that is done.
     */
    void doAction(AirMissionAction action);

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
     * Get the squadrons on this mission.
     *
     * @return The squadrons on this mission.
     */
    MissionSquadrons getSquadrons();

    /**
     * Set all of the squadrons to the correct state.
     */
    void addSquadrons();

    /**
     * Remove all the squadrons from the mission.
     */
    void removeSquadrons();

    /**
     * Get the probability of success for this mission.
     *
     * @return The probability that this mission will be successful.
     */
    List<ProbabilityStats> getMissionProbability();

    /**
     * Determine if the mission is adversely affected by the current weather conditions.
     *
     * @return True if the mission is affected by the current weather conditions. False otherwise.
     */
    boolean isAffectedByWeather();

    /**
     * Get the number of elapsed turns. This is the number of turns for which this mission
     * has been in flight.
     *
     * @return The mission's elapsed turns.
     */
    int getElapsedTurns();
}
