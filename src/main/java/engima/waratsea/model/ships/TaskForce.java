package engima.waratsea.model.ships;

import engima.waratsea.event.ShipEvent;
import engima.waratsea.event.ShipEventHandler;
import engima.waratsea.event.TurnEvent;
import engima.waratsea.event.TurnEventHandler;
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
        if (state == TaskForceState.RESERVE) {
            ShipEvent.register(this);
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
        log.info("notify ship event {} {}", event.getAction(), event.getName());
    }

    /**
     * This method is called to notify the event.
     * @param event the fired event.
     */
    @Override
    public void notify(final TurnEvent event) {
        log.info("notify turn event {}", event.getTurn());
    }
}
