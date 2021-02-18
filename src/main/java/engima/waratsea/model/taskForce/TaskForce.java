package engima.waratsea.model.taskForce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.AirbaseGroup;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.game.event.turn.TurnEvent;
import engima.waratsea.model.game.event.turn.TurnEventMatcher;
import engima.waratsea.model.game.event.turn.data.TurnMatchData;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.ship.AmmunitionType;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.ship.Shipyard;
import engima.waratsea.model.ship.ShipyardException;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.taskForce.data.TaskForceData;
import engima.waratsea.model.taskForce.mission.MissionDAO;
import engima.waratsea.model.taskForce.mission.SeaMission;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a task force, which is a collection of ships.
 *
 * Ships that may station squadrons implement the airbase interface; therefore this class implements the
 * airbase group interface.
 */
@Slf4j
public class TaskForce implements AirbaseGroup, Comparable<TaskForce>, Asset, PersistentData<TaskForceData> {
    private final Provider<PatrolGroups> provider;

    @Getter private final Side side;
    @Getter @Setter private String name;
    @Getter @Setter private String title;
    @Getter @Setter private SeaMission mission;
    @Getter private String reference;                                //This is always a map reference and never a name.
    @Getter private final List<String> possibleStartingLocations;    //Some task forces may start at multiple locations.
    @Getter private boolean locationKnown;
    @Getter @Setter private TaskForceState state;
    @Getter @Setter private List<ShipEventMatcher> releaseShipEvents;
    @Getter @Setter private List<TurnEventMatcher> releaseTurnEvents;
    @Getter private List<Ship> ships;
    @Getter private List<Airbase> airbases;
    @Getter private List<Ship> cargoShips;
    @Getter private Map<String, Ship> shipMap;
    @Getter private Map<ShipType, List<Ship>> shipTypeMap;

    private final Shipyard shipyard;
    private final ShipEventMatcherFactory shipEventMatcherFactory;
    private final GameMap gameMap;

    /**
     * Constructor of Task Force called by guice.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data The task force data read from a JSON file.
     * @param provider Provides patrol groups.
     * @param shipyard builds ships from ship names and side.
     * @param shipEventMatcherFactory Factory for creating ship event matchers.
     * @param missionDAO mission data access object, loads missions.
     * @param gameMap The game's map.
     */
    @Inject
    public TaskForce(@Assisted final Side side,
                     @Assisted final TaskForceData data,
                               final Provider<PatrolGroups> provider,
                               final Shipyard shipyard,
                               final ShipEventMatcherFactory shipEventMatcherFactory,
                               final MissionDAO missionDAO,
                               final GameMap gameMap) {
        this.provider = provider;

        this.shipEventMatcherFactory = shipEventMatcherFactory;
        this.gameMap = gameMap;

        this.side = side;
        name = data.getName();
        title = data.getTitle();

        data.getMission().setSide(side);

        mission = missionDAO.load(data.getMission());
        state = data.getState();

        this.shipyard = shipyard;

        Optional
                .ofNullable(data.getLocation())
                .ifPresent(this::setReference);

        possibleStartingLocations = Optional                          // If the location is fixed
                .ofNullable(data.getPossibleStartingLocations())      // set the possible starting locations to
                .orElseGet(() -> List.of(data.getLocation()));        // the fixed starting location.

        buildShips(data.getShips());
        getCargoShips(data.getCargoShips());
        setAirbases();

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
        ships.forEach(shipyard::save);
    }

    /**
     * Initialize a task force. This is only done for new games.
     *
     * @return This task force.
     */
    public TaskForce init() {
        cargoShips.forEach(Ship::loadCargo);

        if (mission.getType() == SeaMissionType.BOMBARDMENT) {
            ships.forEach(ship -> ship.setAmmunitionType(AmmunitionType.BOMBARDMENT));
        }

        return this;
    }

    /**
     * Get the task force airbases nations. All the nations that have squadrons based within this task force.
     *
     * @return The task force airbases nations.
     */
    public Set<Nation> getNations() {
        return airbases
                .stream()
                .flatMap(airbase -> airbase.getNations().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Get the task force's game grid.
     *
     * @return The task force's game grid.
     */
    public Optional<GameGrid> getGrid() {
        return gameMap.getGrid(reference);
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
     * Determine if the task force is at an enemy port.
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
     * Determine if the task force has shore bombardment ammunition.
     *
     * @return True if the task force is equipped with shore bombardment ammunition.
     */
    public boolean hasBombardmentAmmo() {
        return ships
                .stream()
                .anyMatch(ship -> ship.getAmmunitionType() == AmmunitionType.BOMBARDMENT);
    }

    /**
     * Set the task force's new reference. Since during the initial task force setup
     * a task force may be removed from the map, it is possible for the new location
     * to be null. This just indicates that the task force is temporarily not assigned
     * to the game. This occurs in the game Arctic Convoy.
     *
     * @param newLocation The task force's new reference.
     */
    public void setReference(final String newLocation) {
        reference = Optional
                .ofNullable(newLocation)
                .map(gameMap::convertNameToReference)
                .orElse(null);

        gameMap.updateTaskForce(this);

        locationKnown = StringUtils.isNotBlank(reference);
    }

    /**
     * Get the task force's reference. Return a port if the task force is in a port.
     * Otherwise, a game map reference is returned.
     *
     * @return The task force's reference. Mapped to a port name if the task force is in a port.
     */
    public String getLocation() {
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
        return airbases
                .stream()
                .flatMap(carrier -> carrier.getLandingType().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get the squadron's stationed within this task force.
     *
     * @return A the squadrons stationed within this task force.
     */
    public List<Squadron> getSquadrons() {
        return airbases
                .stream()
                .flatMap(ship -> ship
                        .getSquadrons()
                        .stream())
                .collect(Collectors.toList());
    }

    /**
     * Indicates if this task force has any squadrons currently stationed.
     * This does not take into account any squadrons given a ferry mission
     * to this airbase.
     *
     * @return True if any squadron is based at this task force. False otherwise.
     */
    public boolean areSquadronsPresent() {
        return airbases
                .stream()
                .anyMatch(Airbase::areSquadronsPresent);
    }

    /**
     * Get this task forces patrol groups.
     *
     * @return This task force's patrol groups.
     */
    @Override
    public PatrolGroups getPatrolGroups() {
        return provider
                .get()
                .build(this);
    }

    /**
     * Build all the task force's ships.
     * @param shipNames list of ship names.
     */
    private void buildShips(final List<String> shipNames)  {
        ships = shipNames.stream()
                .map(shipName -> new ShipId(shipName, side))
                .map(this::buildShip)
                .filter(Optional::isPresent)
                .map(Optional::get)
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
    private void getCargoShips(final List<String> cargoShipNames) {
        cargoShips = Optional.ofNullable(cargoShipNames)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(shipName -> shipMap.get(shipName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Separate out the ships that can conduct air operations, airbases, in this task force.
     */
    private void setAirbases() {
        airbases = ships.stream()
                .filter(Ship::isAirbase)
                .map(ship -> (Airbase) ship)
                .collect(Collectors.toList());
    }

    /**
     * Build a given ship.
     *
     * @param shipId Uniquely identifies a ship.
     * @return The constructed ship.
     */
    private Optional<Ship> buildShip(final ShipId shipId) {
        try {
            return Optional.of(shipyard.load(shipId, this));
        } catch (ShipyardException ex) {
            log.error("Unable to build ship '{}' for side {}", shipId.getName(), shipId.getSide());
            return Optional.empty();
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
                .map(shipEventMatcherFactory::create)
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
                event.getShip().getShipId().getSide().getPossessive(), event.getShip().getType(), event.getAction()});

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
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(final @NotNull TaskForce o) {
        return name.compareTo(o.getName());
    }
}
