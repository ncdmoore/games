package enigma.waratsea.model.ship;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipRegistry;
import enigma.waratsea.TestModule;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShipRegistryTest {

    private static ShipRegistry registry;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setName(GameName.BOMB_ALLEY);

        registry = injector.getInstance(ShipRegistry.class);
    }

    @Test
    public void TestAlliedShipLookup() {
        String shipName = "BB01 Queen Elizabeth";
        String shipClassName = "Queen Elizabeth";

        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        String result = registry.getClass(shipId);

        assert (shipClassName.equals(result));
    }

    @Test
    public void TestAxisShipLookup() {
        String shipName = "BB01 Conte di Cavour";
        String shipClassName = "Conte di Cavour";

        ShipId shipId = new ShipId(shipName, Side.AXIS);

        String result = registry.getClass(shipId);

        assert (shipClassName.equals(result));
    }
}
