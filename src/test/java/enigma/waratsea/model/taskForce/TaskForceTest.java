package enigma.waratsea.model.taskForce;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.turn.TurnEvent;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.turn.TurnEventMatcher;
import engima.waratsea.model.ships.ShipType;
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
import java.util.List;

public class TaskForceTest {

    private static TaskForceFactory factory;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

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
    public void testTaskForceActivateShipEvent() {

        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        ShipEventMatcher releaseEvent = new ShipEventMatcher();
        releaseEvent.setSide(Side.ALLIES);
        releaseEvent.setAction(ShipEventAction.SPOTTED);
        releaseEvent.setShipType("*");

        List<ShipEventMatcher> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        ShipEvent event = new ShipEvent();
        event.setSide(Side.ALLIES);
        event.setAction(ShipEventAction.SPOTTED);
        event.setShipType(ShipType.AIRCRAFT_CARRIER);

        data.setReleaseShipEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }

    @Test
    public void testTaskForceNonActivationShipEvent() {
        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        ShipEventMatcher releaseEvent = new ShipEventMatcher();
        releaseEvent.setSide(Side.ALLIES);
        releaseEvent.setAction(ShipEventAction.SPOTTED);
        releaseEvent.setShipType("BATTLESHIP");

        List<ShipEventMatcher> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        ShipEvent event = new ShipEvent();
        event.setSide(Side.ALLIES);
        event.setAction(ShipEventAction.SPOTTED);
        event.setShipType(ShipType.AIRCRAFT_CARRIER);

        data.setReleaseShipEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.setShipType(ShipType.BATTLESHIP);

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

        TurnEventMatcher releaseEvent = new TurnEventMatcher();
        releaseEvent.setTurn(turnNumber);

        List<TurnEventMatcher> releaseEvents = new ArrayList<>();
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

        TurnEventMatcher releaseEvent = new TurnEventMatcher();
        releaseEvent.setTurn(turnNumber);

        List<TurnEventMatcher> releaseEvents = new ArrayList<>();
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
