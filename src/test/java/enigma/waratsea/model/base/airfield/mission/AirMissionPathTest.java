package enigma.waratsea.model.base.airfield.mission;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.base.airfield.mission.AirMissionPath;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import enigma.waratsea.TestModule;
import lombok.extern.slf4j.Slf4j;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AirMissionPathTest {

    private static AirMissionPath path;
    private static GameMap gameMap;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());
        path = injector.getInstance(AirMissionPath.class);
        gameMap = injector.getInstance(GameMap.class);
    }

    @Test
    public void testAddInBound() {
        List<GameGrid> outBound = new ArrayList<>(Arrays.asList(new GameGrid(0,0), new GameGrid(0,1), new GameGrid(0, 2)));

        Deencapsulation.setField(path, "gridPath", outBound);

        path.addInBound();

        List<GameGrid> fullPath = Deencapsulation.getField(path, "gridPath");

        Assert.assertEquals(fullPath.get(0), fullPath.get(fullPath.size() - 1));
    }

    @Test
    public void testGetStartingRow() {
        // Coordinates are of the form (row, column)

        // Even start row.
        // Test end row greater than start row.
        // Path is a straight line from 0,0 to 10,n. Ending column does not matter.
        int startingRow = Deencapsulation.invoke(path, "getStartingRow", 0, 10, 0);

        Assert.assertEquals(0, startingRow);

        // Test end row less than start row.
        // Path is a straight line from 10,0 to 0,n. Ending column does not matter.
        startingRow = Deencapsulation.invoke(path, "getStartingRow", 10, 0, 0);

        Assert.assertEquals(9, startingRow);

        // Odd start row.
        // Test end row greater than start row.
        // Path is a straight line from 0,1 to 10,n. Ending column does not matter.
        startingRow = Deencapsulation.invoke(path, "getStartingRow", 0, 10, 1);

        Assert.assertEquals(1, startingRow);

        // Test end row less than start row.
        // Path is a straight line from 1,1 to 0,n. Ending column does not matter.
        startingRow = Deencapsulation.invoke(path, "getStartingRow", 1, 0, 1);

        Assert.assertEquals(1, startingRow);
    }

    @Test
    public void testAddRowsLineSlopesToTheUpperRight() {
        // The path slopes upward to the right.
        String startGrid = "BG32";
        String endGrid = "BM25";

        List<GameGrid> grids = Stream
                .of(startGrid, "BH30", "BI30", "BJ28", "BK27", "BL26", endGrid)
                .map(mapReference -> gameMap.getGrid(mapReference).orElseThrow())
                .collect(Collectors.toList());

        List<GameGrid> fullPath = Deencapsulation.invoke(path, "addGrids", grids);

        int distance = gameMap.determineDistance(startGrid, endGrid);

        // The starting grid is in the list, so the number of grids is the distance plus 1.
        Assert.assertEquals(distance + 1, fullPath.size());

        Assert.assertTrue(verifyPath(fullPath));
    }

    @Test
    public void testAddRowsLineSlopesToTheUpperLeft() {
        // The path slopes upward to the left.
        String startGrid = "BG32";
        String endGrid = "BE23";

        List<GameGrid> grids = Stream
                .of(startGrid, "BF27", endGrid)
                .map(mapReference -> gameMap.getGrid(mapReference).orElseThrow())
                .collect(Collectors.toList());

        List<GameGrid> fullPath = Deencapsulation.invoke(path, "addGrids", grids);

        int distance = gameMap.determineDistance(startGrid, endGrid);

        Assert.assertEquals(distance + 1, fullPath.size());

        Assert.assertTrue(verifyPath(fullPath));
    }

    @Test
    public void testAddRowsHorizontalLine() {
        // The path is a horizontal line.
        String startGrid = "BG32";
        String endGrid = "BI32";

        List<GameGrid> grids = Stream
                .of(startGrid, "BH32", endGrid)
                .map(mapReference -> gameMap.getGrid(mapReference).orElseThrow())
                .collect(Collectors.toList());

        List<GameGrid> fullPath = Deencapsulation.invoke(path, "addGrids", grids);

        int distance = gameMap.determineDistance(startGrid, endGrid);

        Assert.assertEquals(distance + 1, fullPath.size());

        Assert.assertTrue(verifyPath(fullPath));
    }

    @Test
    public void testAddRowsLineSlopesToLeft() {
        // The path slopes to the left. The rows are much closer than the columns.
        String startGrid = "BG32";
        String endGrid = "AV29";

        List<GameGrid> grids = Stream
                .of(startGrid, "BF31", "BE32", "BD31", "BC31", "BB30", "BA31", "AZ30", "AY30", "AX29", "AW30", endGrid)
                .map(mapReference -> gameMap.getGrid(mapReference).orElseThrow())
                .collect(Collectors.toList());

        List<GameGrid> fullPath = Deencapsulation.invoke(path, "addGrids", grids);

        int distance = gameMap.determineDistance(startGrid, endGrid);

        Assert.assertEquals(distance + 1, fullPath.size());

        Assert.assertTrue(verifyPath(fullPath));
    }

    @Test
    public void testAddRowsVerticalLine() {
        // The path is a vertical line.
        String startGrid = "AK22";
        String endGrid = "AK24";

        List<GameGrid> grids = Stream
                .of(startGrid, endGrid)
                .map(mapReference -> gameMap.getGrid(mapReference).orElseThrow())
                .collect(Collectors.toList());

        List<GameGrid> fullPath = Deencapsulation.invoke(path, "addGrids", grids);

        int distance = gameMap.determineDistance(startGrid, endGrid);

        Assert.assertEquals(distance + 1, fullPath.size());

        Assert.assertTrue(verifyPath(fullPath));
    }

    /**
     * Verify that the grids in the given path are contiguous. Each grid in the
     * path must be adjacent to its neighbors (the previous grid and the subsequent
     * grid in the list).
     *
     * @param path the given path from one grid to another grid.
     * @return True if the path is contiguous.
     */
    private boolean verifyPath(List<GameGrid> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            if (!areGridsAdjacent(path.get(i), path.get(i + 1))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determine if the given two grids are next to each other.
     *
     * @param gridOne a game map grid.
     * @param gridTwo a game map grid.
     * @return True if the given grids are next to each other. Meaning that starting
     * from grid one, grid two may be reached in one move.
     */
    private boolean areGridsAdjacent(final GameGrid gridOne, final GameGrid gridTwo) {
        return Math.abs(gridOne.getRow() - gridTwo.getRow()) <= 1
                && Math.abs(gridOne.getColumn() - gridTwo.getColumn()) <= 1;
    }
}
