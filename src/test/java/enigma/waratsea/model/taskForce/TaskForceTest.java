package enigma.waratsea.model.taskForce;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.Asset;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.game.event.turn.TurnEvent;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.turn.TurnEventMatcher;
import engima.waratsea.model.game.event.turn.data.TurnMatchData;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.ships.ShipId;
import engima.waratsea.model.ships.Shipyard;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.data.TaskForceData;
import engima.waratsea.model.taskForce.TaskForceFactory;
import engima.waratsea.model.taskForce.TaskForceState;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TaskForceTest {

    private static TaskForceFactory factory;
    private static Shipyard shipyard;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

        shipyard = injector.getInstance(Shipyard.class);

        factory = injector.getInstance(TaskForceFactory.class);
    }

    @Test
    public void testTaskForceCarrierGrouping() {
        TaskForceData data = new TaskForceData();

        List<String> shipNames = new ArrayList<>(Arrays.asList("BC01 Renown", "CV04 Ark Royal", "CL17 Despatch","CL36 Sheffield", "DD53 Faulknor"));

        data.setShips(shipNames);
        data.setLocation("Alexandria");

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        Assert.assertEquals(5, taskForce.getShips().size());
        Assert.assertEquals(1, taskForce.getAircraftCarriers().size());
    }

    @Test
    public void testCargoShips() {
        TaskForceData data = new TaskForceData();

        List<String> shipNames = new ArrayList<>(Arrays.asList("CL36 Sheffield", "DD53 Faulknor"));
        List<String> cargoShipNames = new ArrayList<>(Collections.singletonList("DD53 Faulknor"));

        data.setShips(shipNames);
        data.setCargoShips(cargoShipNames);
        data.setLocation("Alexandria");

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        Assert.assertEquals(2, taskForce.getShips().size());
        Assert.assertEquals(1, taskForce.getCargoShips().size());
    }

    @Test
    public void testNoCargoShips() {
        TaskForceData data = new TaskForceData();

        List<String> shipNames = new ArrayList<>(Arrays.asList("CL36 Sheffield", "DD53 Faulknor"));

        data.setShips(shipNames);
        data.setLocation("Alexandria");

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        Assert.assertEquals(2, taskForce.getShips().size());
    }

    @Test
    public void testTaskForceActivateShipEvent() throws Exception {

        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        ShipMatchData releaseEvent = new ShipMatchData();
        releaseEvent.setSide(Side.ALLIES);
        releaseEvent.setAction(ShipEventAction.SPOTTED.toString());

        List<ShipMatchData> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        String shipName = "CVL04 Eagle";
        ShipId shipId = new ShipId(shipName, Side.ALLIES);
        Ship ship = shipyard.build(shipId);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.SPOTTED);
        event.setBy(Asset.AIRCRAFT);

        data.setReleaseShipEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        ship.setTaskForce(taskForce);

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }

    @Test
    public void testTaskForceNonActivationShipEvent() throws Exception {
        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        ShipMatchData releaseEvent = new ShipMatchData();
        releaseEvent.setSide(Side.ALLIES);
        releaseEvent.setAction(ShipEventAction.SPOTTED.toString());
        releaseEvent.setShipType("BATTLESHIP");

        List<ShipMatchData> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        String shipName = "CVL04 Eagle";
        ShipId shipId = new ShipId(shipName, Side.ALLIES);
        Ship ship = shipyard.build(shipId);

        ShipEvent event = new ShipEvent();
        event.setShip(ship);
        event.setAction(ShipEventAction.SPOTTED);
        event.setBy(Asset.SUB);

        data.setReleaseShipEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        ship.setTaskForce(taskForce);

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.RESERVE);

        shipName = "BB02 Warspite";
        shipId = new ShipId(shipName, Side.ALLIES);
        ship = shipyard.build(shipId);
        ship.setTaskForce(taskForce);

        event.setShip(ship);

        event.fire();

        Assert.assertEquals(taskForce.getState(), TaskForceState.ACTIVE);
    }


    @Test
    public void testTaskForceActivateTurnEvent() {
        TaskForceData data = new TaskForceData();
        data.setLocation("Malta");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        int turnNumber = 10;

        TurnMatchData releaseEvent = new TurnMatchData();
        releaseEvent.setTurn(turnNumber);

        List<TurnMatchData> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        TurnEvent event = new TurnEvent();
        event.setTurn(turnNumber);

        data.setReleaseTurnEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }

    @Test
    public void testTaskForceNonActivationTurnEvent() {
        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        int turnNumber = 10;

        TurnMatchData releaseEvent = new TurnMatchData();
        releaseEvent.setTurn(turnNumber);

        List<TurnMatchData> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        TurnEvent event = new TurnEvent();
        event.setTurn(5);

        data.setReleaseTurnEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.RESERVE);
    }

}
