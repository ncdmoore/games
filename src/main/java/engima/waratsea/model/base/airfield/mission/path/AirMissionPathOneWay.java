package engima.waratsea.model.base.airfield.mission.path;

import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.path.data.AirMissionPathData;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.paths.MapPaths;
import engima.waratsea.model.target.Target;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is a utility class that calculates the grid through which a mission passes.
 */
@Slf4j
public class AirMissionPathOneWay implements AirMissionPath {
    private final MapPaths mapPaths;

    private List<GameGrid> gridPath;
    private int currentGridIndex;
    private List<GameGrid> traversedThisTurn;                   // The grids traversed this game turn.

    /**
     * Constructor called by guice.
     *
     * @param data The air mission path data read in from a JSON file.
     * @param mapPaths The game map utility - used to get the air mission path.
     */
    @Inject
    public AirMissionPathOneWay(@Assisted final AirMissionPathData data,
                                          final MapPaths mapPaths) {
        this.mapPaths = mapPaths;

        this.currentGridIndex = Optional
                .ofNullable(data)
                .map(AirMissionPathData::getCurrentGridIndex)
                .orElse(-1);

        this.gridPath = Optional
                .ofNullable(data)
                .map(AirMissionPathData::getGridPath)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(mapRef -> mapPaths.getGrid(mapRef).orElseThrow())
                .collect(Collectors.toList());
    }

    /**
     * Get the persistent air mission path data.
     *
     * @return The persistent air mission path data.
     */
    public AirMissionPathData getData() {
        AirMissionPathData data = new AirMissionPathData();
        data.setGridPath(gridPath.stream().map(GameGrid::getMapReference).collect(Collectors.toList()));
        data.setCurrentGridIndex(currentGridIndex);
        return data;
    }

    /**
     * Get the game map grids that the mission's path passes through.
     *
     * @param airbase The starting airbase of the mission.
     * @param target The mission's target.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void end() {
        currentGridIndex = gridPath.size() - 1;   // The last grid index.
    }

    /**
     * Get the distance to the end of the path.
     *
     * @return The distance in game grids to the path's end grid. This is a measure of how far the mission has
     * left to go until it reaches its end grid.
     */
    @Override
    public int getDistanceToEnd() {
        int lastGridIndex = gridPath.size() - 1;
        return lastGridIndex - currentGridIndex;
    }
}
