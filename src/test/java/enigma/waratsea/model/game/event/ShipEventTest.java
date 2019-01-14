package enigma.waratsea.model.game.event;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.Asset;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceFactory;
import engima.waratsea.model.taskForce.data.TaskForceData;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ShipEventTest {
    private static TaskForce taskForce;

    private static String carrierName = "CVL04 Eagle";
    private static String destroyerName = "DD110 Tartar";
    private static String cruiserName = "CA05 Kent";

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

        TaskForceFactory factory = injector.getInstance(TaskForceFactory.class);

        List<String> ships = new ArrayList<>();
        ships.add(carrierName);
        ships.add(destroyerName);
        ships.add(cruiserName);

        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setShips(ships);

        taskForce = factory.create(Side.ALLIES, data);
        taskForce.setLocation("F20");
    }

    @Test
    public void testShipEventSpottedMatch() {

        Ship ship = taskForce.getShip(carrierName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.SPOTTED);
        event.setBy(Asset.AIRCRAFT);

        ShipMatchData data = new ShipMatchData();
        data.setAction(ShipEventAction.SPOTTED.toString());
        data.setShipType("AIRCRAFT_CARRIER");
        data.setSide(Side.ALLIES);
        data.setLocation("F20");

        ShipEventMatcher matcher = new ShipEventMatcher(data);

        Assert.assertTrue(matcher.match(event));
    }

    @Test
    public void testShipEventCargo() {

        Ship ship = taskForce.getShip(destroyerName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.CARGO_UNLOADED);

        ShipMatchData data = new ShipMatchData();
        data.setAction(ShipEventAction.CARGO_UNLOADED.toString());
        data.setShipType("DESTROYER");
        data.setSide(Side.ALLIES);
        data.setLocation("F20");

        ShipEventMatcher matcher = new ShipEventMatcher(data);

        Assert.assertTrue(matcher.match(event));
    }

    @Test
    public void testShipEventBombardment() {

        Ship ship = taskForce.getShip(cruiserName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.BOMBARDMENT);

        ShipMatchData data = new ShipMatchData();
        data.setAction(ShipEventAction.BOMBARDMENT.toString());
        data.setShipType("CRUISER");
        data.setSide(Side.ALLIES);
        data.setLocation("F20");

        ShipEventMatcher matcher = new ShipEventMatcher(data);

        Assert.assertTrue(matcher.match(event));
    }

    @Test
    public void testShipEventArival() {

        Ship ship = taskForce.getShip(cruiserName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.ARIVAL);

        ShipMatchData data = new ShipMatchData();
        data.setAction(ShipEventAction.ARIVAL.toString());
        data.setName(cruiserName);
        data.setShipType("CRUISER");
        data.setSide(Side.ALLIES);
        data.setLocation("F20");

        ShipEventMatcher matcher = new ShipEventMatcher(data);

        Assert.assertTrue(matcher.match(event));
    }

    @Test
    public void testShipEventDamaged() {
        Ship ship = taskForce.getShip(destroyerName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.DAMAGED_PRIMARY);

        ShipMatchData data = new ShipMatchData();
        data.setAction("DAMAGED");
        data.setSide(Side.ALLIES);

        ShipEventMatcher matcher = new ShipEventMatcher(data);

        Assert.assertTrue(matcher.match(event));
    }

}
