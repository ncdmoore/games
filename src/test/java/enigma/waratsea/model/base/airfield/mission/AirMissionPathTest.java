package enigma.waratsea.model.base.airfield.mission;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.base.airfield.mission.AirMissionPath;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.enemy.views.airfield.AirfieldView;
import engima.waratsea.model.enemy.views.airfield.AirfieldViewFactory;
import engima.waratsea.model.enemy.views.airfield.data.AirfieldViewData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetFactory;
import engima.waratsea.model.target.TargetType;
import engima.waratsea.model.target.data.TargetData;
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

    private static Injector injector;
    private static GameMap gameMap;
    private static AirfieldFactory airfieldFactory;
    private static TargetFactory targetFactory;
    private static AirfieldViewFactory airfieldViewFactory;

    @BeforeClass
    public static void setup() {
        injector = Guice.createInjector(new TestModule());
        gameMap = injector.getInstance(GameMap.class);
        airfieldFactory = injector.getInstance(AirfieldFactory.class);
        targetFactory = injector.getInstance(TargetFactory.class);
        airfieldViewFactory = injector.getInstance(AirfieldViewFactory.class);
    }

    @Test
    public void testBuild() {
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

        Airfield airfield = buildAlexandriaAirfield();
        Airfield enemyAirfield = buildDernaAirfield();
        Target enemyAirfieldTarget = buildEnemyAirfield(enemyAirfield);

        AirfieldView enemyAirfieldView = buildAirfieldView(enemyAirfield);

        Deencapsulation.setField(enemyAirfieldTarget, "airfieldView", enemyAirfieldView);

        path.build(airfield, enemyAirfieldTarget);

        int distanceToTarget = enemyAirfieldTarget.getDistance(airfield);

        List<GameGrid> gridPath = Deencapsulation.getField(path, "gridPath");

        // The grid path size is equal to the distance to the target, minus the starting airbase grid.
        Assert.assertEquals(distanceToTarget, gridPath.size() - 1);  // The minus 1 accounts for the starting grid which is the airbase itself.

        Assert.assertTrue(verifyPath(gridPath));

    }

    @Test
    public void testAddInBound() {
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

        List<GameGrid> outBound = new ArrayList<>(Arrays.asList(
                new GameGrid(0,0),
                new GameGrid(0,1),
                new GameGrid(0, 2)));

        Deencapsulation.setField(path, "gridPath", outBound);

        path.addInBound();

        List<GameGrid> fullPath = Deencapsulation.getField(path, "gridPath");

        Assert.assertEquals(fullPath.get(0), fullPath.get(fullPath.size() - 1));
    }

    @Test
    public void testProgress() {
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

        List<GameGrid> fullPath = new ArrayList<>(Arrays.asList(
                new GameGrid(0,0),
                new GameGrid(0,1),
                new GameGrid(0,2),
                new GameGrid(0,3),     // In this test the first progress moves the index here.
                new GameGrid(0,4),
                new GameGrid(0, 5)));  // In this test the second progress moves the index here.

        Deencapsulation.setField(path, "gridPath", fullPath);

        int distanceTraversed = 3;
        path.start();

        // Simulate a single turn.
        path.progress(distanceTraversed);

        int currentGridIndex = Deencapsulation.getField(path, "currentGridIndex");

        Assert.assertEquals(distanceTraversed, currentGridIndex);

        // Simulate a second turn.
        path.progress(distanceTraversed);

        currentGridIndex = Deencapsulation.getField(path, "currentGridIndex");

        // The mission was capable of moving further than it needed this turn. Thus, the path should be at the last index.
        Assert.assertEquals(fullPath.size() - 1, currentGridIndex);
    }

    @Test
    public void testRecallOutBound() {
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

        List<GameGrid> fullPath = new ArrayList<>(Arrays.asList(
                new GameGrid(0,0),
                new GameGrid(0,1),
                new GameGrid(0,2),     // In this test the mission is here when it is recalled.
                new GameGrid(0,1),
                new GameGrid(0, 0)));

        Deencapsulation.setField(path, "gridPath", fullPath);

        int distanceTraversed = 2;
        path.start();

        // Simulate a single turn.
        path.progress(distanceTraversed);

        int currentGridIndex = Deencapsulation.getField(path, "currentGridIndex");

        GameGrid homeGrid = fullPath.get(0);
        GameGrid recallGrid = fullPath.get(currentGridIndex);

        // Recall the mission.
        path.recall(AirMissionState.OUT_BOUND);

        List<GameGrid> newPath = Deencapsulation.getField(path, "gridPath");
        currentGridIndex = Deencapsulation.getField(path, "currentGridIndex");

        GameGrid newStartingGrid = newPath.get(currentGridIndex);

        // The new recall path's starting grid should equal the original grid's grid at which the recall occurred.
        Assert.assertEquals(recallGrid, newStartingGrid);

        GameGrid newEndGrid = newPath.get(newPath.size() - 1);

        // The new recall path's ending grid should equal the original grid's starting grid.
        Assert.assertEquals(homeGrid, newEndGrid);
    }

    @Test
    public void testRecallInBound() {
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

        List<GameGrid> fullPath = new ArrayList<>(Arrays.asList(
                new GameGrid(0, 0),
                new GameGrid(0, 1),
                new GameGrid(0, 2),
                new GameGrid(0, 1),   // In this test the mission is here when it is recalled.
                new GameGrid(0, 0)));

        Deencapsulation.setField(path, "gridPath", fullPath);

        int distanceTraversed = 3;
        path.start();

        // Simulate a single turn.
        path.progress(distanceTraversed);

        int currentGridIndex = Deencapsulation.getField(path, "currentGridIndex");

        GameGrid homeGrid = fullPath.get(0);
        GameGrid recallGrid = fullPath.get(currentGridIndex);

        path.recall(AirMissionState.IN_BOUND);    // This really doesn't change anything as the mission is already in bound.

        currentGridIndex = Deencapsulation.getField(path, "currentGridIndex");
        List<GameGrid> newPath = Deencapsulation.getField(path, "gridPath");

        GameGrid newStartingGrid = newPath.get(currentGridIndex);

        // The new recall path's starting grid should equal the original grid's grid at which the recall occurred.
        Assert.assertEquals(recallGrid, newStartingGrid);

        GameGrid newEndGrid = newPath.get(newPath.size() - 1);

        // The new recall path's ending grid should equal the original grid's starting grid.
        Assert.assertEquals(homeGrid, newEndGrid);
    }

    @Test
    public void testGetStartingRow() {
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

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
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

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
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

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
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

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
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

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
        AirMissionPath path = injector.getInstance(AirMissionPath.class);

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

    private Airfield buildAlexandriaAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Alexandria");
        data.setSide(Side.ALLIES);
        data.setLandingType(List.of(LandingType.LAND, LandingType.SEAPLANE, LandingType.CARRIER));
        data.setMaxCapacity(20);
        data.setAntiAir(8);
        data.setLocation("BG32");

        return airfieldFactory.create(data);
    }

    private Airfield buildDernaAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Derna");
        data.setSide(Side.AXIS);
        data.setLandingType(List.of(LandingType.LAND, LandingType.CARRIER));
        data.setMaxCapacity(8);
        data.setAntiAir(8);
        data.setLocation("AV29");

        return airfieldFactory.create(data);
    }

    private Target buildEnemyAirfield(final Airfield airfield) {
        TargetData data = new TargetData();
        data.setType(TargetType.ENEMY_AIRFIELD);
        data.setName(airfield.getName());
        data.setSide(Side.AXIS);

        return targetFactory.createEnemyAirfieldTarget(data);
    }

    private AirfieldView buildAirfieldView(final Airfield airfield) {
        AirfieldViewData data = new AirfieldViewData();
        data.setName(airfield.getName());
        data.setAirfield(airfield);
        return airfieldViewFactory.create(data);
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
