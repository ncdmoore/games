package enigma.waratsea.model.game.event;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceFactory;
import engima.waratsea.model.taskForce.data.TaskForceData;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShipEventTest {
    private static ShipEventMatcherFactory shipEventMatcherFactory;

    private static TaskForce taskForce;

    private static String carrierName = "CVL04 Eagle-1";
    private static String destroyerName = "DD110 Tartar";
    private static String cruiserName = "CA05 Kent";
    private static String battleshipName = "BB02 Warspite";

    @BeforeClass
    public static void setup() throws Exception {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setName(GameName.BOMB_ALLEY);

        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        GameMap gameMap = injector.getInstance(GameMap.class);
        gameMap.load(scenario);

        shipEventMatcherFactory = injector.getInstance(ShipEventMatcherFactory.class);

        TaskForceFactory factory = injector.getInstance(TaskForceFactory.class);

        List<String> ships = new ArrayList<>(Arrays.asList(carrierName, destroyerName, cruiserName, battleshipName));

        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setShips(ships);

        taskForce = factory.create(Side.ALLIES, data);
        taskForce.setReference("Tobruk");
    }

    @Test
    public void testShipEventSpottedMatch() {

        Ship ship = taskForce.getShip(carrierName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.SPOTTED);
        event.setBy(AssetType.AIRCRAFT);

        ShipMatchData data = new ShipMatchData();
        data.setAction(ShipEventAction.SPOTTED.toString());
        data.setShipType("AIRCRAFT_CARRIER");
        data.setSide(Side.ALLIES);
        data.setLocation("Tobruk");

        ShipEventMatcher matcher = shipEventMatcherFactory.create(data);

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
        data.setLocation("Tobruk");

        ShipEventMatcher matcher = shipEventMatcherFactory.create(data);

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
        data.setLocation("Tobruk");

        ShipEventMatcher matcher = shipEventMatcherFactory.create(data);
        matcher.log();

        Assert.assertTrue(matcher.match(event));
    }

    @Test
    public void testShipEventArrival() {

        Ship ship = taskForce.getShip(cruiserName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.ARRIVAL);

        ShipMatchData data = new ShipMatchData();
        data.setAction(ShipEventAction.ARRIVAL.toString());
        data.setName(cruiserName);
        data.setShipType("CRUISER");
        data.setSide(Side.ALLIES);
        data.setLocation("Tobruk");

        ShipEventMatcher matcher = shipEventMatcherFactory.create(data);
        matcher.log();

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

        ShipEventMatcher matcher = shipEventMatcherFactory.create(data);

        Assert.assertTrue(matcher.match(event));
    }

    @Test
    public void testShipEventMultipleTypes() {
        Ship ship = taskForce.getShip(battleshipName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.BOMBARDMENT);

        ShipMatchData data = new ShipMatchData();
        data.setAction("BOMBARDMENT");
        data.setSide(Side.ALLIES);
        data.setShipType("BATTLESHIP, CRUISER");

        ShipEventMatcher matcher = shipEventMatcherFactory.create(data);

        Assert.assertTrue(matcher.match(event));
    }

    @Test
    public void testShipEventMulitpleLocations() {
        Ship ship = taskForce.getShip(battleshipName);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.CARGO_UNLOADED);

        ShipMatchData data = new ShipMatchData();
        data.setAction("CARGO_UNLOADED");
        data.setSide(Side.ALLIES);
        data.setShipType("BATTLESHIP");
        data.setLocation("Tobruk, Taranto");

        ShipEventMatcher matcher = shipEventMatcherFactory.create(data);

        Assert.assertTrue(matcher.match(event));

    }
}
