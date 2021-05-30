package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.GridView;
import engima.waratsea.model.map.Point;
import engima.waratsea.view.ViewProps;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * This class IS NOT NEEDED. Keep just in case we end up needing it later. I don't think we will ever need it.
 * But just in case.
 *
 * This is a utility class that calculates the grid through which a patrol passes.
 */
public class PatrolPath {
    private static final double HALF_GRID = 0.5;
    private static final int FULL_CIRCLE = 360;
    private final GameMap gameMap;
    private final ViewProps props;

    private final Map<Integer, List<GameGrid>> gridPath = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game map.
     * @param props The view properties.
     */
    @Inject
    public PatrolPath(final GameMap gameMap,
                      final ViewProps props) {
        this.gameMap = gameMap;
        this.props = props;
    }

    /**
     * Get the given patrol's grid path. This includes all grids within the patrol's circle.
     *
     * @param patrol The patrol whose grid path is determined.
     */
    public void buildGrids(final Patrol patrol) {
        int radius = patrol.getTrueMaxRadius();

        Airbase airbase = patrol.getAirbase();

        String airbaseReference = airbase.getReference();
        GameGrid airbaseGrid = gameMap.getGrid(airbaseReference).orElseThrow();

        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        GridView airbaseGridView = new GridView(gridSize, airbaseGrid);
        Point airbaseCenterPoint = airbaseGridView.getCenter();

        if (!patrol.getAssignedSquadrons().isEmpty()) {
            gridPath.put(0, Collections.singletonList(getGrid(airbaseCenterPoint, gridSize)));
        }

        gridPath.put(0, Collections.singletonList(getGrid(airbaseCenterPoint, gridSize)));

        for (int distance = 1; distance <= radius; distance++) {
            gridPath.put(distance, getGridsAtRadius(airbaseCenterPoint, radius, gridSize));
        }
    }

    /**
     * Determine if this patrol covers or contains the given game grid.
     *
     * @param targetGrid The grid checked to determine if covered by this patrol.
     * @return The distance from the airbase
     */
    public int contains(final GameGrid targetGrid) {
        return gridPath
                .entrySet()
                .stream()
                .filter(e -> e.getValue().contains(targetGrid))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    private List<GameGrid> getGridsAtRadius(final Point point, final int radius, final int gridSize) {
        Set<GameGrid> grids = new HashSet<>();

        // Using 1/2 half the grid size, find a reasonable angle interval
        // to evenly divide the patrol's circle into pie shape wedges.
        double angleInRadians = Math.asin(HALF_GRID * (1.0 / radius));
        int angleInDegrees = (int) Math.toDegrees(angleInRadians);

        // Find the next smallest whole interval of a circle.
        // We want the interval to evenly divide a full circle.
        while (FULL_CIRCLE % angleInDegrees != 0) {
            angleInDegrees--;
        }

        int intervals =  FULL_CIRCLE / angleInDegrees;   // This is always a whole number.

        // Calculate the x,y coordinates of the top of each pie shaped wedge upper corner.
        // Then find the containing game grid.
        for (int i = 0; i < intervals; i++) {
            double angle = i * angleInDegrees;
            int x = point.getX() + (int) Math.ceil(radius * gridSize * Math.cos(Math.toRadians(angle)));
            int y = point.getY() + (int) Math.ceil(radius * gridSize * Math.sin(Math.toRadians(angle)));

            GameGrid grid = getGrid(new Point(x, y), gridSize);
            grids.add(grid);
        }

        return List.copyOf(grids);
    }

    private GameGrid getGrid(final Point point, final int gridSize) {
        GridView gridView = new GridView(gridSize, point);
        return gameMap.getGrid(gridView.getRow(), gridView.getColumn());
    }
}
