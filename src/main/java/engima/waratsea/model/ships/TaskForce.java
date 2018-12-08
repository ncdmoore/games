package engima.waratsea.model.ships;

import engima.waratsea.event.ship.ShipEvent;
import engima.waratsea.event.ship.ShipEventHandler;
import engima.waratsea.event.turn.TurnEvent;
import engima.waratsea.event.turn.TurnEventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * This class represents a task force, which is a collection of ships.
 */
@Slf4j
public class TaskForce implements ShipEventHandler, TurnEventHandler {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String location;

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
            ShipEvent.register(this);
        }

        if (state == TaskForceState.RESERVE && releaseTurnEvents != null) {
            TurnEvent.register(this);
        }
    }

    /**
     * This method is called to notify the event handler that an event has fired.
     *
     * @param event The fired event.
     */
    @Override
    public void notify(final ShipEvent event) {
        log.info("{} {} notify ship event {} {} {}", new Object[] {name, title, event.getAction(), event.getSide(), event.getShipType()});

        boolean release = releaseShipEvents.stream().anyMatch(shipEvent -> shipEvent.equals(event));

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
    @Override
    public void notify(final TurnEvent event) {
        log.info("{} {} notify turn event {}", new Object[] {name, title, event.getTurn()});

        boolean release = releaseTurnEvents.stream().anyMatch(turnEvent -> turnEvent.equals(event));

        if (release) {
            state = TaskForceState.ACTIVE;
            log.info("{} state {}", name, state);
            TurnEvent.unregister(this);
        }
    }
}
