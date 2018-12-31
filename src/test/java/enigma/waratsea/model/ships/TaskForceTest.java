package enigma.waratsea.model.ships;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.event.ship.ShipEvent;
import engima.waratsea.event.ship.ShipEventAction;
import engima.waratsea.event.ship.ShipEventType;
import engima.waratsea.event.turn.RandomTurnEvent;
import engima.waratsea.event.turn.TurnEvent;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceData;
import engima.waratsea.model.taskForce.TaskForceFactory;
import engima.waratsea.model.taskForce.TaskForceState;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        ShipEvent releaseEvent = new ShipEvent();
        releaseEvent.setSide(Side.ALLIES);
        releaseEvent.setAction(ShipEventAction.SPOTTED);
        releaseEvent.setShipType(ShipEventType.ANY);

        List<ShipEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);


        ShipEvent event = new ShipEvent();
        event.setSide(Side.ALLIES);
        event.setAction(ShipEventAction.SPOTTED);
        event.setShipType(ShipEventType.AIRCRAFT_CARRIER);

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

        ShipEvent releaseEvent = new ShipEvent();
        releaseEvent.setSide(Side.ALLIES);
        releaseEvent.setAction(ShipEventAction.SPOTTED);
        releaseEvent.setShipType(ShipEventType.BATTLESHIP);

        List<ShipEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        ShipEvent event = new ShipEvent();
        event.setSide(Side.ALLIES);
        event.setAction(ShipEventAction.SPOTTED);
        event.setShipType(ShipEventType.AIRCRAFT_CARRIER);

        data.setReleaseShipEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.RESERVE);
    }


    @Test
    public void testTaskForceActivateTurnEvent() {
        TaskForceData data = new TaskForceData();
        data.setLocation("Malta");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        int turnNumber = 10;

        TurnEvent releaseEvent = new TurnEvent();
        releaseEvent.setTurn(turnNumber);

        List<TurnEvent> releaseEvents = new ArrayList<>();
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

        TurnEvent releaseEvent = new TurnEvent();
        releaseEvent.setTurn(turnNumber);

        List<TurnEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        TurnEvent event = new TurnEvent();
        event.setTurn(5);

        data.setReleaseTurnEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.RESERVE);
    }

    @Test
    public void testTaskForceActivateRandomTurnEventExactTurn() {
        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        Set<Integer> releaseValues = Stream.of(4, 5, 6).collect(Collectors.toSet());

        RandomTurnEvent releaseEvent = new RandomTurnEvent();
        releaseEvent.setTurn(1);
        releaseEvent.setValues(releaseValues);

        List<RandomTurnEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        data.setReleaseRandomTurnEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        RandomTurnEvent firedEvent = new RandomTurnEvent();
        firedEvent.setTurn(1);
        firedEvent.setValues(Stream.of(5).collect(Collectors.toSet()));


        assert (taskForce.getState() == TaskForceState.RESERVE);

        firedEvent.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }

    @Test
    public void testTaskForceActivateRandomTurnEventGreaterThan() {
        TaskForceData data = new TaskForceData();
        data.setLocation("Alexandria");
        data.setState(TaskForceState.RESERVE);
        data.setShips(new ArrayList<>());

        Set<Integer> releaseValues = Stream.of(1).collect(Collectors.toSet());

        RandomTurnEvent releaseEvent = new RandomTurnEvent();
        releaseEvent.setTurnGreaterThan(16);
        releaseEvent.setValues(releaseValues);

        List<RandomTurnEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        data.setReleaseRandomTurnEvents(releaseEvents);

        TaskForce taskForce = factory.create(Side.ALLIES, data);

        RandomTurnEvent firedEvent = new RandomTurnEvent();
        firedEvent.setTurn(16);
        firedEvent.setValues(Stream.of(1).collect(Collectors.toSet()));


        assert (taskForce.getState() == TaskForceState.RESERVE);

        firedEvent.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }
}
