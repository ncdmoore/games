package enigma.waratsea.model.ships;

import engima.waratsea.event.ship.ShipEvent;
import engima.waratsea.event.ship.ShipEventAction;
import engima.waratsea.event.ship.ShipEventType;
import engima.waratsea.event.turn.RandomTurnEvent;
import engima.waratsea.event.turn.TurnEvent;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.TaskForce;
import engima.waratsea.model.ships.TaskForceState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskForceTest {

    @Test
    public void testTaskForceActivateShipEvent() {

        TaskForce taskForce = new TaskForce();
        taskForce.setState(TaskForceState.RESERVE);

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

        taskForce.setReleaseShipEvents(releaseEvents);
        taskForce.registerEvents();

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }

    @Test
    public void testTaskForceNonActivationShipEvent() {
        TaskForce taskForce = new TaskForce();
        taskForce.setState(TaskForceState.RESERVE);

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

        taskForce.setReleaseShipEvents(releaseEvents);
        taskForce.registerEvents();

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.RESERVE);
    }


    @Test
    public void testTaskForceActivateTurnEvent() {
        TaskForce taskForce = new TaskForce();
        taskForce.setState(TaskForceState.RESERVE);

        int turnNumber = 10;

        TurnEvent releaseEvent = new TurnEvent();
        releaseEvent.setTurn(turnNumber);

        List<TurnEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        TurnEvent event = new TurnEvent();
        event.setTurn(turnNumber);

        taskForce.setReleaseTurnEvents(releaseEvents);
        taskForce.registerEvents();

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }

    @Test
    public void testTaskForceNonActivationTurnEvent() {
        TaskForce taskForce = new TaskForce();
        taskForce.setState(TaskForceState.RESERVE);

        int turnNumber = 10;

        TurnEvent releaseEvent = new TurnEvent();
        releaseEvent.setTurn(turnNumber);

        List<TurnEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        TurnEvent event = new TurnEvent();
        event.setTurn(5);

        taskForce.setReleaseTurnEvents(releaseEvents);
        taskForce.registerEvents();

        assert (taskForce.getState() == TaskForceState.RESERVE);

        event.fire();

        assert (taskForce.getState() == TaskForceState.RESERVE);
    }

    @Test
    public void testTaskForceActivateRandomTurnEventExactTurn() {
        TaskForce taskForce = new TaskForce();
        taskForce.setState(TaskForceState.RESERVE);

        Set<Integer> releaseValues = Stream.of(4, 5, 6).collect(Collectors.toSet());

        RandomTurnEvent releaseEvent = new RandomTurnEvent();
        releaseEvent.setTurn(1);
        releaseEvent.setValues(releaseValues);

        List<RandomTurnEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        taskForce.setReleaseRandomTurnEvents(releaseEvents);
        taskForce.registerEvents();

        RandomTurnEvent firedEvent = new RandomTurnEvent();
        firedEvent.setTurn(1);
        firedEvent.setValues(Stream.of(5).collect(Collectors.toSet()));


        assert (taskForce.getState() == TaskForceState.RESERVE);

        firedEvent.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }

    @Test
    public void testTaskForceActivateRandomTurnEventGreaterThan() {
        TaskForce taskForce = new TaskForce();
        taskForce.setState(TaskForceState.RESERVE);

        Set<Integer> releaseValues = Stream.of(1).collect(Collectors.toSet());

        RandomTurnEvent releaseEvent = new RandomTurnEvent();
        releaseEvent.setTurnGreaterThan(16);
        releaseEvent.setValues(releaseValues);

        List<RandomTurnEvent> releaseEvents = new ArrayList<>();
        releaseEvents.add(releaseEvent);

        taskForce.setReleaseRandomTurnEvents(releaseEvents);
        taskForce.registerEvents();

        RandomTurnEvent firedEvent = new RandomTurnEvent();
        firedEvent.setTurn(16);
        firedEvent.setValues(Stream.of(1).collect(Collectors.toSet()));


        assert (taskForce.getState() == TaskForceState.RESERVE);

        firedEvent.fire();

        assert (taskForce.getState() == TaskForceState.ACTIVE);
    }
}
