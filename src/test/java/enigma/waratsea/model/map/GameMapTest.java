package enigma.waratsea.model.map;

import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.Grid;
import org.junit.Test;

public class GameMapTest {


    @Test
    public void testConversionFromMapReference() {
        String mapReference = "F20";

        GameMap gameMap = new GameMap();
        gameMap.init(12);

        Grid grid = gameMap.getGrid(mapReference);

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
    }
}
