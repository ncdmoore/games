package enigma.waratsea.model.ships;

import engima.waratsea.event.ship.ShipEvent;
import engima.waratsea.event.ship.ShipEventAction;
import engima.waratsea.event.ship.ShipEventType;
import engima.waratsea.event.turn.TurnEvent;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.TaskForce;
import engima.waratsea.model.ships.TaskForceState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
}
