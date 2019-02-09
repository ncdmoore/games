package enigma.waratsea.model.map;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.MapProps;
import engima.waratsea.model.scenario.Scenario;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GameMapTest {
    private static GameTitle gameTitle;
    private static MapProps props;
    private static GameMap gameMap;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());
        gameTitle = injector.getInstance(GameTitle.class);                                                              //The game instance must be injected first!
        final String gameName = "bombAlley";
        gameTitle.setValue(gameName);

        props = injector.getInstance(MapProps.class);
        gameMap = injector.getInstance(GameMap.class);


    }

    @Test
    public void testConversionFromMapReference() {
        String mapReference = "F20";

        GameGrid grid = gameMap.getGrid(mapReference);

        assert (grid.getColumn() == 5);
        assert (grid.getRow() == 19);

        mapReference = "AA1";

        grid = gameMap.getGrid(mapReference);

        assert (grid.getRow() == 0);
        assert (grid.getColumn() == 26);

        mapReference = "BE31";

        grid = gameMap.getGrid(mapReference);

        assert (grid.getRow() == 30);
        assert (grid.getColumn() == 56);

        mapReference = "A01";

        grid = gameMap.getGrid(mapReference);

        assert (grid.getRow() == 0);
        assert (grid.getColumn() == 0);

        mapReference = "A1";

        grid = gameMap.getGrid(mapReference);

        assert (grid.getRow() == 0);
        assert (grid.getColumn() == 0);
    }

    @Test
    public void testMapPropConversionNeeded() {
        String name = "Gibraltar";
        String mapReference = gameMap.convertNameToReference(name);
        assert ("H22".equals(mapReference));
    }

    @Test
    public void testMapPropNoConversionNeeded() {
        String name = "BF31";
        String mapReference = gameMap.convertNameToReference(name);
        assert (name.equals(mapReference));
    }

    @Test
    public void testMapPropReverseLookup() {
        String mapReference = "AK24";
        String name = gameMap.convertReferenceToName(mapReference);
        Assert.assertEquals ("Malta", name);
    }

    @Test
    public void testMapLocation() throws Exception{
        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        gameMap.load(scenario);

        Assert.assertTrue(gameMap.isLocationBase(Side.ALLIES, "BG32"));

    }
}
