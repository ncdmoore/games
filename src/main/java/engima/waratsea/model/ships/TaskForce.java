package engima.waratsea.model.ships;

import engima.waratsea.event.ship.ShipEvent;
import engima.waratsea.event.turn.RandomTurnEvent;
import engima.waratsea.event.turn.TurnEvent;
import engima.waratsea.model.target.Target;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * This class represents a task force, which is a collection of ships.
 */
@Slf4j
public class TaskForce  {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private TaskForceMission mission;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private List<Target> target;

    @Getter
    @Setter
    private TaskForceState state;

    @Getter
    @Setter
    private List<ShipEvent> releaseShipEvents;

    @Getter
    @Setter
    private List<TurnEvent> releaseTurnEvents;

    @Getter
    @Setter
    private List<RandomTurnEvent> releaseRandomTurnEvents;

    @Getter
    @Setter
    private List<String> ships;

    /**
     * The string representation of this object.
     *
     * @return The task force name and title.
     */
    @Override
    public String toString() {
        return name + "-" + title;
    }

    /**
     * Register the task force for game events.
     */
    public void registerEvents() {
        if (state == TaskForceState.RESERVE && releaseShipEvents != null) {
            ShipEvent.register(this, this::handleShipEvent);
        }

        if (state == TaskForceState.RESERVE && releaseTurnEvents != null) {
            TurnEvent.register(this, this::handleTurnEvent);
        }

        if (state == TaskForceState.RESERVE && releaseRandomTurnEvents != null) {
            RandomTurnEvent.register(this, this::handleRandomTurnEvent);
        }
    }

    /**
     * This method is called to notify the event handler that an event has fired.
     *
     * @param event The fired event.
     */
    private void handleShipEvent(final ShipEvent event) {
        log.info("{} {} notify ship event {} {} {}", new Object[] {name, title, event.getAction(), event.getSide(), event.getShipType()});

        boolean release = releaseShipEvents.stream().anyMatch(shipEvent -> shipEvent.match(event));

        if (release) {
            state = TaskForceState.ACTIVE;
            log.info("{} state {}", name, state);
            ShipEvent.unregister(this);
        }
    }

    /**
     * This method is called to notify the event.
     *
     * @param event the fired event.
     */
    private void handleTurnEvent(final TurnEvent event) {
        log.info("{} {} notify turn event {}", new Object[] {name, title, event.getTurn()});

        boolean release = releaseTurnEvents.stream().anyMatch(turnEvent -> turnEvent.match(event));

        if (release) {
            state = TaskForceState.ACTIVE;
            log.info("{} state {}", name, state);
            TurnEvent.unregister(this);
        }
    }

    /**
     * A random turn event has fired.
     *
     * @param event the fired event.
     */
    private void handleRandomTurnEvent(final RandomTurnEvent event) {
        log.info("{} {} notify random turn event {}", new Object[] {name, title, event.getTurn()});

        boolean release = releaseRandomTurnEvents.stream().anyMatch(randomTurnEvent -> randomTurnEvent.match(event));

        if (release) {
            state = TaskForceState.ACTIVE;
            log.info("{} state {}", name, state);
            RandomTurnEvent.unregister(this);
        }
    }
}
