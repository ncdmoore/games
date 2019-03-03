package engima.waratsea.model.taskForce;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.game.event.turn.TurnEvent;
import engima.waratsea.model.aircraft.Airbase;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.turn.TurnEventMatcher;
import engima.waratsea.model.game.event.turn.data.TurnMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.ships.ShipId;
import engima.waratsea.model.ships.ShipType;
import engima.waratsea.model.ships.Shipyard;
import engima.waratsea.model.ships.ShipyardException;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetFactory;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.taskForce.data.TaskForceData;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
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
public class TaskForce implements PersistentData<TaskForceData> {
    private final Side side;

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
    private List<Ship> cargoShips;

    @Getter
    private Map<String, Ship> shipMap;

    @Getter
    private Map<ShipType, List<Ship>> shipTypeMap;

    private GameMap gameMap;
    private Shipyard shipyard;
    private ShipEventMatcherFactory shipEventMatcherFactory;
    private TargetFactory targetFactory;

    /**
     * Constructor of Task Force called by guice.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data The task force data read from a JSON file.
     * @param gameMap The game map.
     * @param shipyard builds ships from ship names and side.
     * @param shipEventMatcherFactory Factory for creating ship event matchers.
     * @param targetFactory Factory for creating ship targets.
     */
    @Inject
    public TaskForce(@Assisted final Side side,
                     @Assisted final TaskForceData data,
                               final GameMap gameMap,
                               final Shipyard shipyard,
                               final ShipEventMatcherFactory shipEventMatcherFactory,
                               final TargetFactory targetFactory) {

        this.shipEventMatcherFactory = shipEventMatcherFactory;
        this.targetFactory = targetFactory;

        this.side = side;
        name = data.getName();
        title = data.getTitle();
        mission = data.getMission();
        targets = createTargets(data.getTargets());
        state = data.getState();

        this.gameMap = gameMap;
        this.shipyard = shipyard;

        setLocation(data.getLocation());
        buildShips(data.getShips());
        loadCargo(data.getCargoShips());
        getCarriers();

        buildShipEvents(data.getReleaseShipEvents());
        buildTurnEvents(data.getReleaseTurnEvents());

        finish();
    }

    /**
     * Get the persistent task force data.
     *
     * @return The data of the task force that is persisted.
     */
    @Override
    public TaskForceData getData() {
        TaskForceData data = new TaskForceData();
        data.setName(name);
        data.setTitle(title);
        data.setMission(mission);
        data.setTargets(PersistentUtility.getData(targets));
        data.setState(state);
        data.setLocation(location);
        data.setShips(getShipNames(ships));
        data.setCargoShips(getShipNames(cargoShips));
        data.setReleaseShipEvents(PersistentUtility.getData(releaseShipEvents));
        data.setReleaseTurnEvents(PersistentUtility.getData(releaseTurnEvents));

        return data;
    }

    /**
     * Save the task force ships.
     */
    public void saveShips() {
        ships.forEach(ship -> shipyard.save(ship));
    }

    /**
     * Create the task force targets.
     *
     * @param targetData Task force target data read in from a JSON file.
     * @return A list of Task force targets.
     */
    private List<Target> createTargets(final List<TargetData> targetData) {
        return Optional.ofNullable(targetData)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(targetFactory::create)
                .collect(Collectors.toList());
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
     *
     * @return True if the task force is active. False otherwise.
     */
    public boolean isActive() {
        return state == TaskForceState.ACTIVE;
    }

    /**
     * Determine if the task force is at an enemey port.
     *
     * @return True if the task force is currently located at an enemy port. False otherwise.
     */
    public boolean atEnemyBase() {
        return gameMap.isLocationBase(side.opposite(), location);
    }

    /**
     * Determine if the task force is at a friendly port.
     *
     * @return True if the task force is currently located at a friendly port. False otherwise.
     */
    public boolean atFriendlyBase() {
        return gameMap.isLocationBase(side, location);
    }

    /**
     * Set the task force's locaton.
     *
     * @param newLocation The new location of the task force.
     */
    public void setLocation(final String newLocation) {
        location = gameMap.convertNameToReference(newLocation);
    }

    /**
     * Indicates the task force has left harbor: setting sail.
     */
    public void setSail() {
        ships.forEach(Ship::setSail);
    }

    /**
     * Get the reasons a task force is activated.
     *
     * @return A list of strings where each string is a separate reason the task force is activated.
     */
    public List<String> getActivatedByText() {
        Stream<String> turnReasons = Optional.ofNullable(releaseTurnEvents)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(turnEventMatcher -> "\u2022 " + turnEventMatcher.getExplanation());

        Stream<String> shipReasons = Optional.ofNullable(releaseShipEvents)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(shipEventMatcher -> "\u2022 " + shipEventMatcher.getExplanation());

        return Stream.concat(turnReasons, shipReasons).collect(Collectors.toList());
    }

    /**
     * Get the specified ship.
     *
     * @param shipName The name of the ship.
     * @return The ship identified by the given name.
     */
    public Ship getShip(final String shipName) {
        return shipMap.get(shipName);
    }

    /**
     * Build all the task force's ships.
     * @param shipNames list of ship names.
     */
    private void buildShips(final List<String> shipNames)  {
        ships = shipNames.stream()
                .map(shipName -> new ShipId(shipName, side))
                .map(this::buildShip)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        shipMap = ships.stream()
                .collect(Collectors.toMap(Ship::getName, ship -> ship));

        shipTypeMap = ships
                .stream()
                .collect(Collectors.groupingBy(Ship::getType));
    }

    /**
     * Set the cargoShips status of all cargoShips ships.
     *
     * @param cargoShipNames List of ships carrying cargoShips.
     */
    private void loadCargo(final List<String> cargoShipNames) {
        cargoShips = Optional.ofNullable(cargoShipNames)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(this::loadCargo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
     *
     * @param shipId Uniquely identifies a ship.
     * @return The constructed ship.
     */
    private Ship buildShip(final ShipId shipId) {
        try {
            Ship ship = shipyard.load(shipId);
            ship.setTaskForce(this);
            return ship;
        } catch (ShipyardException ex) {
            log.error("Unable to build ship '{}' for side {}", shipId.getName(), shipId.getSide());
            return null;
        }
    }

    /**
     * Get the ship names from the given list of ships.
     *
     * @param shipList A list of ships.
     * @return List of ship names.
     */
    private List<String> getShipNames(final List<Ship> shipList) {
        return shipList.stream()
                .map(Ship::getName)
                .collect(Collectors.toList());
    }

    /**
     * Register the task force for game events.
     */
    private void finish() {

        location = gameMap.convertNameToReference(location);

        Optional.ofNullable(targets)
                .orElseGet(Collections::emptyList)
                .forEach(this::convertTargetLocation);

        Optional.ofNullable(releaseShipEvents)
                .filter(matchers -> state == TaskForceState.RESERVE)
                .ifPresent(matchers -> ShipEvent.register(this, this::handleShipEvent));

        Optional.ofNullable(releaseTurnEvents)
                .filter(matchers -> state == TaskForceState.RESERVE)
                .ifPresent(matchers -> TurnEvent.register(this, this::handleTurnEvent));
    }

    /**
     * Build the ship release events.
     *
     * @param shipMatchData Release ship match data from the JSON file.
     */
    private void buildShipEvents(final List<ShipMatchData> shipMatchData) {
        releaseShipEvents = Optional.ofNullable(shipMatchData)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(data -> shipEventMatcherFactory.create(data))
                .collect(Collectors.toList());
    }

    /**
     * Build the turn release events.
     *
     * @param data Release turn match data from the JSON file.
     */
    private void buildTurnEvents(final List<TurnMatchData> data) {
        releaseTurnEvents = Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(TurnEventMatcher::new)
                .collect(Collectors.toList());
    }

    /**
     * If the target location is a base name, update the target's map reference.
     *
     * @param target One of the task force's targets.
     */
    private void convertTargetLocation(final Target target) {
        String mapReference =  gameMap.convertNameToReference(target.getLocation());
        target.setLocation(mapReference);
    }

    /**
     * This method is called to notify the event handler that an event has fired.
     *
     * @param event The fired event.
     */
    private void handleShipEvent(final ShipEvent event) {
        log.info("{} {} notify ship event {} {} {}.", new Object[] {name, title,
                event.getShip().getShipId().getSide().getPossesive(), event.getShip().getType(), event.getAction()});

        boolean release = releaseShipEvents.stream().anyMatch(eventMatcher -> eventMatcher.match(event));

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

        boolean release = releaseTurnEvents.stream().anyMatch(eventMatcher -> eventMatcher.match(event));

        if (release) {
            state = TaskForceState.ACTIVE;
            log.info("{} state {}", name, state);
            TurnEvent.unregister(this);
        }
    }

    /**
     * Load the specified ship with cargoShips.
     *
     * @param shipName The name of the ship that is loaded with cargoShips.
     * @return The cargo ship.
     */
    private Ship loadCargo(final String shipName) {
        if (shipMap.containsKey(shipName)) {
            Ship ship = shipMap.get(shipName);
            ship.loadCargo();
            return ship;
        } else {
            log.error("Invalid cargo ship name: '{}'", shipName);
            return null;
        }
    }
}
