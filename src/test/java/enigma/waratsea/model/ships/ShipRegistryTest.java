package enigma.waratsea.model.ships;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.ShipRegistry;
import enigma.waratsea.TestModule;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShipRegistryTest {

    private static ShipRegistry registry;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

        registry = injector.getInstance(ShipRegistry.class);
    }

    @Test
    public void TestAlliedShipLookup() {


        String shipName = "BB01 Queen Elizabeth";
        String shipClassName = "Queen Elizabeth";

        String result = registry.getClass(Side.ALLIES, shipName);

        assert (shipClassName.equals(result));
    }

    @Test
    public void TestAxisShipLookup() {


        String shipName = "BB01 Conte di Cavour";
        String shipClassName = "Conte di Cavour";

        String result = registry.getClass(Side.AXIS, shipName);

        assert (shipClassName.equals(result));
    }
}
