package engima.waratsea.model.base.airfield.mission;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.GridView;
import engima.waratsea.model.map.Point;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.ViewProps;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a utility class that calculates the grid through which a mission passes.
 */
@Slf4j
public class AirMissionPath {
    private final GameMap gameMap;
    private final ViewProps props;

    private List<GameGrid> gridPath = Collections.emptyList();  // Initialize to empty path.
    private int currentGridIndex = -1;                          // Empty path. So initialize index to invalid.
    private List<GameGrid> traversedThisTurn;                   // The grids traversed this game turn.

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game map.
     * @param props The view properties.
     */
    @Inject
    public AirMissionPath(final GameMap gameMap,
                          final ViewProps props) {
        this.gameMap = gameMap;
        this.props = props;
    }

    /**
     * Get the game map grids that the mission's path passes through.
     *
     * @param airbase The starting airbase of the mission.
     * @param target The mission's target.
     */
    public void build(final Airbase airbase, final Target target) {
        String startingReference = airbase.getReference();
        String endingReference = target.getReference();

        GameGrid startingGrid = gameMap.getGrid(startingReference).orElseThrow();
        GameGrid endingGrid = gameMap.getGrid(endingReference).orElseThrow();

        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        GridView startingGridView = new GridView(gridSize, startingGrid);
        GridView endingGridView = new GridView(gridSize, endingGrid);

        Point startingPoint = startingGridView.getCenter();
        Point endingPoint = endingGridView.getCenter();

        gridPath = buildGrids(startingPoint, endingPoint, gridSize);
    }

    /**
     * For mission that are round trips. This method adds the in-bound grids.
     * Which are just the out-bound grids in reverse order minus the end grid.
     */
    public void addInBound() {
        List<GameGrid> outBound = new ArrayList<>(gridPath);
        List<GameGrid> inbound = new ArrayList<>(gridPath);
        inbound.remove(outBound.size() - 1);

        Collections.reverse(inbound);
        outBound.addAll(inbound);

        gridPath = outBound;
    }

    /**
     * Mark the path as starting. The mission has started.
     */
    public void start() {
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

        switch (state) {
            case OUT_BOUND:
                // Set the grid path to be the grids already traversed, but in reverse order.
                // The squadrons are flying back to their original starting airbase.
                //
                // Grid path is of the form:
                //
                //  outBound-0 ... outBound-N, outBound-N+1 ... Target ... inBound-N+1, inBound-N ... inBound-0
                //
                // where outBound-N and inBound-N are the same distance from the starting airbase
                // (ouBound-0 and inBound-0). In fact, outBound-N = inBound-N.
                //
                // Note, the grid path always contains an odd number of grids for round trip missions.
                gridPath = new ArrayList<>(gridPath.subList(0, currentGridIndex + 1));
                Collections.reverse(gridPath);
                currentGridIndex = 0;
                break;
            case IN_BOUND:
                // Set the grid path to be the current grid to the end grid.
                // There is no real change if the mission is already in bound.
                gridPath = new ArrayList<>(gridPath.subList(currentGridIndex, gridPath.size()));
                currentGridIndex = 0;
                break;
            default:
                log.error("Invalid air mission state: '{}'", state);
        }
    }

    /**
     * Mark the mission as ended. Set the mission path to indicate it has reached the end.
     */
    public void end() {
        currentGridIndex = gridPath.size() - 1;
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

    /**
     * Get the grid path between the given start and end points given the grid size.
     *
     * @param startingPoint Marks the starting grid location.
     * @param endingPoint Marks the ending grid location.
     * @param gridSize The map's grid size.
     * @return The mission's grid path.
     */
    private List<GameGrid> buildGrids(final Point startingPoint, final Point endingPoint, final int gridSize) {
        Set<GameGrid> grids = new LinkedHashSet<>();

        double slope = getSlope(startingPoint, endingPoint);
        double b = getB(slope, startingPoint);

        int intervals = (endingPoint.getX() - startingPoint.getX()) / gridSize;  // This should be a whole number.

        for (int i = 0; i <= Math.abs(intervals); i++) {
            int x = startingPoint.getX() + (gridSize * i * (intervals / Math.abs(intervals)));
            int y = (int) (slope * x + b);
            Point point = new Point(x, y);
            GameGrid grid = getGrid(point, gridSize);
            grids.add(grid);
        }

        return addGrids(List.copyOf(grids));
    }

    /**
     * This method adds the grids between any two initial grids that are needed to reach the n+1 grid
     * from the n grid. This occurs when any two grids are only a single column or less away from each
     * other but are more than a single row away from each other.
     *
     * @param initialGrids A list of grids that are only at most a single column away from each other.
     * @return A full grid path that may be traversed from the first grid to the last grid, where each
     * grid is only either at most a single column or single row away. So grid n is only one move away
     * from grid n + 1.
     */
    private List<GameGrid> addGrids(final List<GameGrid> initialGrids) {
        Set<GameGrid> allGrids = new LinkedHashSet<>();

        for (int i = 0; i < initialGrids.size() - 1; i++) {
            GameGrid startGrid = initialGrids.get(i);
            GameGrid endGrid = initialGrids.get(i + 1);

            int startRow = startGrid.getRow();
            int endRow = endGrid.getRow();

            int startColumn = startGrid.getColumn();
            int endColumn = endGrid.getColumn();

            int increment = endRow > startRow ? 1 : -1;
            int delta = Math.abs(endRow - startRow);

            int startingRow = getStartingRow(startRow, endRow, startColumn);

            allGrids.add(startGrid);

            for (int rowCount = 0; rowCount < delta; rowCount++) {
                int row = startingRow + rowCount * increment;
                GameGrid grid = new GameGrid(row, endColumn);
                allGrids.add(grid);
            }

            allGrids.add(endGrid);
        }


        return List.copyOf(allGrids);
    }

    private GameGrid getGrid(final Point point, final int gridSize) {
        GridView gridView = new GridView(gridSize, point);
        return new GameGrid(gridView.getRow(), gridView.getColumn());
    }

    private double getSlope(final Point startingPoint, final Point endingPoint) {
        double deltaX = endingPoint.getX() - startingPoint.getX();
        double deltaY = endingPoint.getY() - startingPoint.getY();
        return deltaY / deltaX;
    }

    private double getB(final double slope, final Point point) {
        return point.getY() - (slope * point.getX());
    }

    private int getStartingRow(final int startRow, final int endRow, final int startColumn) {
        int startingRow;
        if (startColumn % 2 == 0) {
            if (endRow < startRow) {
                startingRow = startRow - 1;
            } else {
                startingRow = startRow;
            }
        } else {
            if (endRow < startRow) {
                startingRow = startRow;
            } else {
                startingRow = startRow + 1;
            }
        }

        return startingRow;
    }
}
