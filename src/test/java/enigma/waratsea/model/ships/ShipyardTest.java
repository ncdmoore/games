package enigma.waratsea.model.ships;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.aircraft.Airbase;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.ships.data.ShipData;
import engima.waratsea.model.ships.ShipType;
import engima.waratsea.model.ships.Shipyard;
import enigma.waratsea.TestModule;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShipyardTest {

    private static Shipyard shipyard;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

        shipyard = injector.getInstance(Shipyard.class);
    }

    @Test
    public void testShipClassLoadForAircraftCarrier() throws Exception {
        String shipClass = "Eagle";
        ShipData shipData = Deencapsulation.invoke(shipyard, "loadShipData", shipClass, Side.ALLIES);
        assert (shipData.getType().equals(ShipType.AIRCRAFT_CARRIER));
    }

    @Test
    public void testShipClassLoadForDestroyer() throws Exception {
        String shipClass = "A Class";
        ShipData shipData = Deencapsulation.invoke(shipyard, "loadShipData", shipClass, Side.ALLIES);
        assert (shipData.getType().equals(ShipType.DESTROYER));
    }

    @Test
    public void testBuildAircraftCarrier() throws Exception {
        String shipName = "CVL04 Eagle";
        Ship ship = shipyard.build(shipName, Side.ALLIES);

        Assert.assertNotNull(ship);
        Assert.assertEquals(Nation.BRITISH, ship.getNationality());

        ship = shipyard.build(shipName, Side.ALLIES);

        Airbase carrier = (Airbase) ship;

        Assert.assertNotNull(ship);
        Assert.assertEquals(Nation.BRITISH, ship.getNationality());
        Assert.assertEquals(4, carrier.getCapacity());
    }

    @Test
    public void testBuildDestroyer() throws Exception {
        String shipName = "DD18 Arrow";
        Ship ship = shipyard.build(shipName, Side.ALLIES);

        Assert.assertNotNull(ship);
        Assert.assertEquals(Nation.BRITISH, ship.getNationality());
    }
}
