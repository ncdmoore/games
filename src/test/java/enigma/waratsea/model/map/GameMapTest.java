package enigma.waratsea.model.map;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GameMapTest {
    private static GameMap gameMap;

    @BeforeClass
    public static void setup() throws Exception{
        Injector injector = Guice.createInjector(new TestModule());
        GameTitle gameTitle = injector.getInstance(GameTitle.class);                                                              //The game instance must be injected first!
        gameTitle.setName(GameName.BOMB_ALLEY);

        gameMap = injector.getInstance(GameMap.class);

        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        GameMap gameMap = injector.getInstance(GameMap.class);

        gameMap.load(scenario);
    }

    @Test
    public void testConversionFromMapReference() throws Exception {
        String mapReference = "F20";

        GameGrid grid = gameMap.getGrid(mapReference).orElseThrow(Exception::new);

        assert (grid.getColumn() == 5);
        assert (grid.getRow() == 19);

        mapReference = "AA1";

        grid = gameMap.getGrid(mapReference).orElseThrow(Exception::new);

        assert (grid.getRow() == 0);
        assert (grid.getColumn() == 26);

        mapReference = "BE31";

        grid = gameMap.getGrid(mapReference).orElseThrow(Exception::new);

        assert (grid.getRow() == 30);
        assert (grid.getColumn() == 56);

        mapReference = "A10";

        grid = gameMap.getGrid(mapReference).orElseThrow(Exception::new);

        assert (grid.getRow() == 9);
        assert (grid.getColumn() == 0);

        mapReference = "A1";

        grid = gameMap.getGrid(mapReference).orElseThrow(Exception::new);

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
        String name = gameMap.convertPortReferenceToName(mapReference);
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
