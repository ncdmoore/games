package engima.waratsea.model.base.airfield.mission.path;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.path.data.AirMissionPathData;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.target.Target;

public interface AirMissionPath {

    /**
     * Get the persistent air mission path data.
     *
     * @return The persistent air mission path data.
     */
    AirMissionPathData getData();

    /**
     * Get the game map grids that the mission's path passes through.
     *
     * @param airbase The starting airbase of the mission.
     * @param target The mission's target.
     */
    void start(Airbase airbase, Target target);

    /**
     * Progress the mission along its path by the given distance.
     *
     * @param distance how far the mission has progressed along its path. How far it has moved.
     */
    void progress(int distance);

    /**
     * Recall the mission. Adjust the mission paths to indicate it has been recalled.
     *
     * @param state The current state of the mission.
     */
    void recall(AirMissionState state);

    /**
     * Mark the mission as ended. Set the mission path to indicate it has reached the end.
     */
    void end();

    /**
     * Get the distance to the end of the path.
     *
     * @return The distance in game grids to the path's end grid. This is a measure of how far the mission has
     * left to go until it reaches its end grid.
     */
    int getDistanceToEnd();
}
