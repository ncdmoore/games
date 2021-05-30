package engima.waratsea.model.map.paths;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.GridView;
import engima.waratsea.model.map.Point;
import engima.waratsea.view.ViewProps;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for finding paths on the game map.
 *
 * Air mission paths use this to find the path between the airbase and the target.
 */
@Slf4j
@Singleton
public class MapPaths {
    private final GameMap gameMap;
    private final ViewProps props;

    /**
     * The constructor called by guice.
     *
     * @param gameMap The game map.
     * @param props The view properties.
     */
    @Inject
    public MapPaths(final GameMap gameMap,
                    final ViewProps props) {
        this.gameMap = gameMap;
        this.props = props;

    }

    /**
     * Get a straight path between two map reference points.
     *
     * @param startingReference The starting map reference point.
     * @param endingReference The ending map reference point.
     * @return A list of game grids that connect the given starting and ending map reference points.
     */
    public List<GameGrid> getStraightPath(final String startingReference, final String endingReference) {
        GameGrid startingGrid = gameMap.getGrid(startingReference).orElseThrow();
        GameGrid endingGrid = gameMap.getGrid(endingReference).orElseThrow();

        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        GridView startingGridView = new GridView(gridSize, startingGrid);
        GridView endingGridView = new GridView(gridSize, endingGrid);

        Point startingPoint = startingGridView.getCenter();
        Point endingPoint = endingGridView.getCenter();

        return buildGrids(startingPoint, endingPoint, gridSize);
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
                GameGrid grid = gameMap.getGrid(row, endColumn);
                allGrids.add(grid);
            }

            allGrids.add(endGrid);
        }

        return List.copyOf(allGrids);
    }

    private GameGrid getGrid(final Point point, final int gridSize) {
        GridView gridView = new GridView(gridSize, point);
        return gameMap.getGrid(gridView.getRow(), gridView.getColumn());
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
