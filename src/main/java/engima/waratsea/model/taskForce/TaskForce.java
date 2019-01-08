package engima.waratsea.model.taskForce;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.turn.TurnEvent;
import engima.waratsea.model.aircraft.Airbase;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.turn.TurnEventMatcher;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.ships.ShipType;
import engima.waratsea.model.ships.Shipyard;
import engima.waratsea.model.ships.ShipyardException;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.data.TaskForceData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private List<Target> targets;

    @Getter
    @Setter
    private TaskForceState state;

    @Getter
    @Setter
    private List<ShipEventMatcher> releaseShipEvents;

    @Getter
    @Setter
    private List<TurnEventMatcher> releaseTurnEvents;

    @Getter
    private List<Ship> ships;

    @Getter
    private List<Airbase> aircraftCarriers;

    @Getter
    private Map<ShipType, List<Ship>> shipMap;


    private GameMap gameMap;
    private Shipyard shipyard;

    /**
     * Constructor of Task Force called by guice.
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data The task force data read from a JSON file.
     * @param gameMap The game map.
     * @param shipyard builds ships from ship names and side.
     */
    @Inject
    public TaskForce(@Assisted final Side side,
                     @Assisted final TaskForceData data,
                               final GameMap gameMap,
                               final Shipyard shipyard) {
        name = data.getName();
        title = data.getTitle();
        mission = data.getMission();
        location = data.getLocation();
        targets = data.getTargets();
        state = data.getState();
        releaseShipEvents = data.getReleaseShipEvents();
        releaseTurnEvents = data.getReleaseTurnEvents();

        this.gameMap = gameMap;
        this.shipyard = shipyard;

        buildShips(side, data.getShips());
        getCarriers();

        finish();
    }

    /**
     * The string representation of this object.
     * @return The task force name and title.
     */
    @Override
    public String toString() {
        return name + "-" + title;
    }

    /**
     * Determines if the task force is active.
     * @return True if the task force is active. False otherwise.
     */
    public boolean isActive() {
        return state == TaskForceState.ACTIVE;
    }

    /**
     * Get the reasons a task force is activated.
     * @return A list of strings where each string is a separate reason the task force is activated.
     */
    public List<String> getActivatedByText() {
        Stream<String> turnReasons = Stream.empty();

        if (releaseTurnEvents != null) {
            turnReasons = releaseTurnEvents.stream()
                    .map(turnEventMatcher -> "Activated " + turnEventMatcher.getExplanation());
        }

        Stream<String> shipReasons = Stream.empty();

        if (releaseShipEvents != null) {
            shipReasons = releaseShipEvents.stream()
                    .map(shipEventMatcher -> "Activated " + shipEventMatcher.getExplanation());
        }

        return Stream.concat(turnReasons, shipReasons).collect(Collectors.toList());
    }

    /**
     * Build all the task force's ships.
     * @param side The task force side. ALLIES or AXIS.
     * @param shipNames list of ship names.
     */
    private void buildShips(final Side side, final List<String> shipNames)  {
        ships = shipNames.stream()
                .map(shipName -> buildShip(side, shipName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        shipMap = ships
                .stream()
                .collect(Collectors.groupingBy(Ship::getType));
    }

    /**
     * Separate out the aircraft carriers in this task force.
     */
    private void getCarriers() {
        aircraftCarriers = ships.stream()
                .filter(Ship::isCarrier)
                .map(ship -> (Airbase) ship)
                .collect(Collectors.toList());
    }

    /**
     * Build a given ship.
     * @param side The side of the ship. ALLIES or AXIS.
     * @param shipName The ship name.
     * @return The constructed ship.
     */
    private Ship buildShip(final Side side, final String shipName) {
        try {
            return shipyard.build(shipName, side);
        } catch (ShipyardException ex) {
            log.error("Unable to build ship '{}' for side {}", shipName, side);
            return null;
        }
    }

    /**
     * Register the task force for game events.
     */
    private void finish() {

        location = gameMap.convertNameToReference(location);

        Optional.ofNullable(targets)
                .ifPresent(targetList -> targetList.forEach(this::convertTargetLocation));

        if (state == TaskForceState.RESERVE && releaseShipEvents != null) {
            ShipEvent.register(this, this::handleShipEvent);
        }

        if (state == TaskForceState.RESERVE && releaseTurnEvents != null) {
            TurnEvent.register(this, this::handleTurnEvent);
        }
    }

    /**
     * If the target location is a base name, update the target's map reference.
     * @param target One of the task force's targets.
     */
    private void convertTargetLocation(final Target target) {
        String mapReference =  gameMap.convertNameToReference(target.getLocation());
        target.setLocation(mapReference);
    }

    /**
     * This method is called to notify the event handler that an event has fired.
     * @param event The fired event.
     */
    private void handleShipEvent(final ShipEvent event) {
        log.info("{} {} notify ship event {} {} {}", new Object[] {name, title, event.getAction(), event.getSide(), event.getShipType()});

        boolean release = releaseShipEvents.stream().anyMatch(eventMatcher -> eventMatcher.match(event));

        if (release) {
            state = TaskForceState.ACTIVE;
            log.info("{} state {}", name, state);
            ShipEvent.unregister(this);
        }
    }

    /**
     * This method is called to notify the event.
     * @param event the fired event.
     */
    private void handleTurnEvent(final TurnEvent event) {
        log.info("{} {} notify turn event {}", new Object[] {name, title, event.getTurn()});

        boolean release = releaseTurnEvents.stream().anyMatch(eventMatcher -> eventMatcher.match(event));

        if (release) {
            state = TaskForceState.ACTIVE;
            log.info("{} state {}", name, state);
            TurnEvent.unregister(this);
        }
    }
}
