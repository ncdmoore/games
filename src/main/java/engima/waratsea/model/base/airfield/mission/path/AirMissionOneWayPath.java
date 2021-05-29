package engima.waratsea.model.base.airfield.mission.path;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.paths.MapPaths;
import engima.waratsea.model.target.Target;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a utility class that calculates the grid through which a mission passes.
 */
@Slf4j
public class AirMissionOneWayPath implements AirMissionPath {
    private final MapPaths mapPaths;

    private List<GameGrid> gridPath = Collections.emptyList();  // Initialize to empty path.
    private int currentGridIndex = -1;                          // Empty path. So initialize index to invalid.
    private List<GameGrid> traversedThisTurn;                   // The grids traversed this game turn.

    /**
     * Constructor called by guice.
     *
     * @param mapPaths The game map utility - used to get the air mission path.
     */
    @Inject
    public AirMissionOneWayPath(final MapPaths mapPaths) {
        this.mapPaths = mapPaths;
    }

    /**
     * Get the game map grids that the mission's path passes through.
     *
     * @param airbase The starting airbase of the mission.
     * @param target The mission's target.
     */
    public void start(final Airbase airbase, final Target target) {
        String startingReference = airbase.getReference();
        String endingReference = target.getReference();

        gridPath = mapPaths.getStraightPath(startingReference, endingReference);
        currentGridIndex = 0;
    }

    /**
     * Progress the mission along its path by the given distance.
     *
     * @param distance how far the mission has progressed along its path. How far it has moved.
     */
    public void progress(final int distance) {
        int startingGrid = currentGridIndex;
        currentGridIndex += distance;

        if (currentGridIndex >= gridPath.size()) {      // The mission has reached it's end grid.
            currentGridIndex = gridPath.size() - 1;
        }

        traversedThisTurn = gridPath.subList(startingGrid, currentGridIndex + 1);
    }

    /**
     * Recall the mission. Adjust the mission paths to indicate it has been recalled.
     *
     * @param state The current state of the mission.
     */
    public void recall(final AirMissionState state) {
        // A mission may be recalled only if it is in the out bound leg.
        // All other states are either invalid or for the in bound case a non event.
        if (state == AirMissionState.OUT_BOUND) {
            // Set the grid path to be the grids already traversed, but in reverse order.
            // The squadrons are flying back to their original starting airbase.
            //
            // Grid path for one way missions is of the form:
            //
            // outBound-0 ... outBound-N, outBound-N+1 ... Target
            gridPath = new ArrayList<>(gridPath.subList(0, currentGridIndex + 1));
            Collections.reverse(gridPath);
            currentGridIndex = 0;
        }
    }

    /**
     * Mark the mission as ended. Set the mission path to indicate it has reached the end.
     */
    public void end() {
        currentGridIndex = gridPath.size() - 1;   // The last grid index.
    }

    /**
     * Get the distance to the end of the path.
     *
     * @return The distance in game grids to the path's end grid. This is a measure of how far the mission has
     * left to go until it reaches its end grid.
     */
    public int getDistanceToEnd() {
        int lastGridIndex = gridPath.size() - 1;
        return lastGridIndex - currentGridIndex;
    }
}
