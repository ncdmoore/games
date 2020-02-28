package engima.waratsea.model.taskForce;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.game.event.turn.TurnEvent;
import engima.waratsea.model.game.event.turn.TurnEventMatcher;
import engima.waratsea.model.game.event.turn.data.TurnMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.ship.Shipyard;
import engima.waratsea.model.ship.ShipyardException;
import engima.waratsea.model.taskForce.data.TaskForceData;
import engima.waratsea.model.taskForce.mission.SeaMission;
import engima.waratsea.model.taskForce.mission.MissionDAO;
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
public class TaskForce implements Asset, PersistentData<TaskForceData> {
    @Getter
    private final Side side;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private SeaMission mission;

    @Getter
    private String reference; //This is always a map reference and never a name.

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

    private Shipyard shipyard;
    private ShipEventMatcherFactory shipEventMatcherFactory;
    private GameMap gameMap;

    /**
     * Constructor of Task Force called by guice.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data The task force data read from a JSON file.
     * @param shipyard builds ships from ship names and side.
     * @param shipEventMatcherFactory Factory for creating ship event matchers.
     * @param missionDAO mission data access object, loads missions.
     * @param gameMap The game's map.
     */
    @Inject
    public TaskForce(@Assisted final Side side,
                     @Assisted final TaskForceData data,
                               final Shipyard shipyard,
                               final ShipEventMatcherFactory shipEventMatcherFactory,
                               final MissionDAO missionDAO,
                               final GameMap gameMap) {

        this.shipEventMatcherFactory = shipEventMatcherFactory;
        this.gameMap = gameMap;

        this.side = side;
        name = data.getName();
        title = data.getTitle();

        data.getMission().setSide(side);

        mission = missionDAO.load(data.getMission());
        state = data.getState();

        this.shipyard = shipyard;

        setReference(data.getLocation());
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
        data.setMission(mission.getData());
        data.setState(state);
        data.setLocation(reference);
        data.setShips(getShipNames(ships));
        data.setCargoShips(getShipNames(cargoShips));
        data.setReleaseShipEvents(PersistentUtility.getData(releaseShipEvents));
        data.setReleaseTurnEvents(PersistentUtility.getData(releaseTurnEvents));

        return data;
    }

    /**
     * Save the task force ships.
     */
    @Override
    public void saveChildrenData() {
        ships.forEach(ship -> shipyard.save(ship));
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
        return gameMap.isLocationBase(side.opposite(), reference);
    }

    /**
     * Determine if the task force is at a friendly port.
     *
     * @return True if the task force is currently located at a friendly port. False otherwise.
     */
    public boolean atFriendlyBase() {
        return gameMap.isLocationBase(side, reference);
    }

    /**
     * Get the task force's new reference.
     *
     * @param newLocation The task force's new reference.
     */
    public void setReference(final String newLocation) {
        reference = gameMap.convertNameToReference(newLocation);

        if (atFriendlyBase()) {
            gameMap.getPort(side, reference)
                    .ifPresent(port -> port.addTaskForce(this));
        }
    }

    /**
     * Get the task force's reference. Return a port if the task force is in a port.
     *
     * @return The task force's reference. Mapped to a port name if the task force is in a port.
     */
    public String getMappedLocation() {
        return gameMap.convertPortReferenceToName(reference);
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

        List<String> reasons = Stream.concat(turnReasons, shipReasons).collect(Collectors.toList());

        if (!reasons.isEmpty()) {
            reasons.add(0, "Activated:");
        }

        return reasons;
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
     * Get the task force landing types supported. The list should be empty except for task forces that
     * contain aircraft carriers, including seaplane carriers.
     *
     * @return A list of the landing type's supported by this task force.
     */
    public List<LandingType> getLandingType() {
        return aircraftCarriers
                .stream()
                .flatMap(carrier -> carrier.getLandingType().stream())
                .distinct()
                .collect(Collectors.toList());
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
