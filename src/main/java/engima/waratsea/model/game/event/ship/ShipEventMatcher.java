package engima.waratsea.model.game.event.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.Asset;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is used to match ship events. An entity that is looking for a particular ship event can use this
 * class to detect if the wanted event has occurred.
 */
@Slf4j
public class ShipEventMatcher implements PersistentData<ShipMatchData> {
    @Getter
    private final String action;  // The action to match.

    @Getter
    private final List<String> names; // A list of ship names that match.

    @Getter
    private final Side side; // The side to match.

    @Getter
    private final String taskForceName; // The task force name to match.

    @Getter
    private final List<ShipType> shipTypes; // A list of ship types to match.

    // A list of starting locations to match. This is the location where a ship started.
    // The only event that uses this is the CARGO_UNLOADED event. The starting location is the location
    // where the cargo was loaded.
    @Getter
    private final List<String> portOrigins;

    private final List<String> locations; // A list of locations to match. This is the location where the event occurred.

    @Getter
    private final Asset by;    // The game asset Ship, aircraft or sub that caused the event to fire. The asset that did the event.

    private final GameMap gameMap;

    /**
     * Constructor called by guice.
     *
     * @param data Ship event matcher data read in from a JSON file.
     * @param gameMap The game map.
     */
    @Inject
    public ShipEventMatcher(@Assisted final ShipMatchData data,
                                      final GameMap gameMap) {
        this.gameMap = gameMap;

        action = data.getAction();
        names = parseNames(data.getName());
        side = data.getSide();
        taskForceName = data.getTaskForceName();
        shipTypes = parseShipType(data.getShipType());
        locations = parseLocation(data.getLocation());
        portOrigins = parseLocation(data.getStartingLocation());
        by = data.getBy();
    }

    /**
     * Get the ship match data. This is the corresponding data that is read and written.
     *
     * @return The persistent ship match data.
     */
    @Override
    public ShipMatchData getData() {
        ShipMatchData data = new ShipMatchData();
        data.setAction(action);
        data.setName(Optional.ofNullable(names).map(n -> String.join(",", n)).orElse(null));
        data.setSide(side);
        data.setTaskForceName(taskForceName);
        data.setShipType(Optional.ofNullable(shipTypes).map(this::getShipTypes).orElse(null));
        data.setLocation(Optional.ofNullable(locations).map(l -> String.join(",", l)).orElse(null));
        data.setStartingLocation(Optional.ofNullable(portOrigins).map(p -> String.join(",", p)).orElse(null));
        return data;
    }

    /**
     * Determines if the fired event matches the desired event.
     *
     * @param firedEvent The ship event to test for matching.
     * @return True if the ship event matches. False otherwise.
     */
    public boolean match(final ShipEvent firedEvent) {
        return side == firedEvent.getShip().getShipId().getSide()
                && isActionEqual(firedEvent.getAction())
                && isShipTypeEqual(firedEvent.getShip())
                && isNameEqual(firedEvent.getShip())
                && isTaskForceNameEqual(firedEvent.getShip())
                && isLocationEqual(firedEvent.getShip())
                && isPortOriginEqual(firedEvent.getShip())
                && isByEqual(firedEvent.getBy());
    }

    /**
     * Returns a text string indicating under what conditions the ship event is matched.
     *
     * @return The text explanation string.
     */
    public String getExplanation() {
        String taskForce = Optional.ofNullable(taskForceName)
                .map(tfn -> " in " + tfn)
                .orElse("");

        String ship = Optional.ofNullable(shipTypes)
                .map(this::getShipTypes)
                .orElse("ship");

        String place = Optional.ofNullable(locations)
                .map(this::getLocations)
                .map(l -> "at " + l)
                .orElse("");

        return "if an " + side.getPossesive() + " " + ship + taskForce + " " + action.toLowerCase() + place + ".";
    }

    /**
     * Log the ship event match criteria.
     */
    public void log() {
        log.debug("Match action {}", logValue(action));
        log.debug("Match name {}", logName(names));
        log.debug("Match side {}", logValue(side));
        log.debug("Match task force name {}", logValue(taskForceName));
        log.debug("Match ship type {}", logShip(shipTypes));
        log.debug("Match starting location {}", logLocation(portOrigins));
        log.debug("Match location {}", logLocation(locations));
        log.debug("Match by {}", logValue(by));
    }

    /**
     * Parse the string version of ship types into a list of ShipTypes.
     *
     * @param shipTypeString The ship types in string form. May be null.
     * @return A list of ship types.
     */
    private List<ShipType> parseShipType(final String shipTypeString) {
        return Optional.ofNullable(shipTypeString)
                .map(types -> Stream.of(types.split("\\s*,\\s*"))
                        .map(shipType -> ShipType.valueOf(shipType.toUpperCase().replace(' ', '_')))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Parse the string version of the locations into a list of locations.
     *
     * @param locationString A comma separated list of locations. May be null.
     * @return A list of locations.
     */
    private List<String> parseLocation(final String locationString) {
        return Optional.ofNullable(locationString)
                .map(shipLocations -> Stream.of(shipLocations.split("\\s*,\\s*"))
                        .map(gameMap::convertNameToReference)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Parse the string version of the ship names into a list of ship names.
     *
     * @param nameString A comma separated list of ship names. May be null.
     * @return A list of ship names.
     */
    private List<String> parseNames(final String nameString) {
        return Optional.ofNullable(nameString)
                .map(shipNames -> Stream.of(shipNames.split("\\s*,\\s*"))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Get the string version of the ship names.
     *
     * @param shipNames The list of ship names.
     * @return The string version of the ship names.
     */
    private String getNames(@NotNull final List<String> shipNames) {
        return String.join(", ", shipNames);
    }

    /**
     * Get the string version of the ship types.
     *
     * @param ships The list of ship types.
     * @return The string version of the ship types.
     */
    private String getShipTypes(@NotNull final List<ShipType> ships) {
        return ships.stream()
                .map(ShipType::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Get the string version of the locations.
     *
     * @param shipLocations The list of locations.
     * @return The string version of the locations.
     */
    private String getLocations(@NotNull final List<String> shipLocations) {
        return shipLocations.stream()
                .filter(Objects::nonNull)
                .map(gameMap::convertReferenceToName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Determine if the fired event action matches the desired action.
     *
     * @param shipAction The action of the fired event.
     * @return True if the action of the fired event matches. False otherwise.
     */
    private boolean isActionEqual(final ShipEventAction shipAction) {
        return action == null                                                                                           // Non specified action matches all.
                || matchActionWildcard(shipAction)
                || matchAction(shipAction);
    }

    /**
     * Determine if the fired event ship name matches the desired ship name.
     *
     * @param ship The ship of the fired event.
     * @return True if the ship name of the fired event matches. False otherwise.
     */
    private boolean isNameEqual(final Ship ship) {
        return names == null
                || names.contains(ship.getName());
    }

    /**
     * Determine if the fired event task force name matches the desired task force name.
     *
     * @param ship The fired event ship.
     * @return True if the task force name matches. False otherwise.
     */
    private boolean isTaskForceNameEqual(final Ship ship) {
        return taskForceName == null                                                                                    // Non specified task force name matches all.
                || taskForceName.equalsIgnoreCase(ship.getTaskForce().getName());
    }

    /**
     * Determine if the fired event ship type matches the desired ship type.
     *
     * @param ship The fired event ship.
     * @return True if the two ship event's ship types are equal. False otherwise.
     */
    private boolean isShipTypeEqual(final Ship ship) {
        return shipTypes == null                                                                                         // If wildcard then event ship type does not matter.
                || shipTypes.contains(ship.getType());
    }

    /**
     * Determine if the fired event location mathces the desired location.
     *
     * @param ship The fired event ship.
     * @return True if the ship's location matched. False otherwise.
     */
    private boolean isLocationEqual(final Ship ship) {
        TaskForce taskForce = ship.getTaskForce();

        return locations == null                                                                                         // If the location is not specified then it does not matter.
                || matchAnyEnemyBase(taskForce)
                || matchAnyFriendlyBase(taskForce)
                || locations.contains(taskForce.getLocation());
    }

    /**
     * Determine if the fired event port origin matches the desired port origin.
     *
     * @param ship The fired event ship.
     * @return True if the ship's port origin is matched. False otherwise.
     */
    private boolean isPortOriginEqual(final Ship ship) {
        String portOrigin = ship.getOriginPort();

        return portOrigins == null
                || portOrigin == null
                || portOrigins.contains(portOrigin);
    }
    /**
     * Determine if the fired event asset that caused the event to fire matches the desired asset.
     *
     * @param eventBy The asset that caused the event to fire.
     * @return True if the asset that caused the event to fire is matched. False otherwise.
     */
    private boolean isByEqual(final Asset eventBy) {
        return by == null                                                                                               // If the by asset is not specified then it does not matter.
                || by.equals(eventBy);
    }

    /**
     * Determine if the action matches.
     *
     * @param shipAction The event ship action.
     * @return True if the ship event action matches the desired action. False otherwise.
     */
    private boolean matchAction(final ShipEventAction shipAction) {
        boolean result;

        try {
            result = ShipEventAction.valueOf(action) == shipAction;
        } catch (IllegalArgumentException ex) {
            log.error("Unable to convert ship action: '{}'", action);
            result = false;
        }

        return result;
    }

    /**
     * Matches when a task force is located at an enemy base. Used to match ship
     * bombardment events.
     *
     * @param taskForce The task force that contains the ship experiencing the event.
     * @return True if the ship is at an enemy base and any base will match. False otherwise.
     */
    private boolean matchAnyEnemyBase(final TaskForce taskForce) {
        return locations.contains(GameMap.ANY_ENEMY_BASE)
                && taskForce.atEnemyBase();
    }

    /**
     * Matches the event location if the desired location is a friendly location and the event location occurs at a
     * friendly base.
     *
     * @param taskForce The task force of the ship that experienced the event.
     * @return True if the location matches. False otherwise.
     */
    private boolean matchAnyFriendlyBase(final TaskForce taskForce) {
        return locations.contains(GameMap.ANY_FRIENDLY_BASE)
                && taskForce.atFriendlyBase();
    }

    /**
     * Matches event action wildcard. For example if the event action is DAMAGED_HULL and the
     * desired event is simply DAMAGED, then there is a match.
     *
     * @param shipAction The ship event action.
     * @return True if the ship event action matches. Otherwise false.
     */
    private boolean matchActionWildcard(final ShipEventAction shipAction) {
        return shipAction.toString().toLowerCase().contains(action.trim().toLowerCase());
    }

    /**
     * If a value is not present output an "*".
     *
     * @param value The value to log.
     * @return The value that is actually logged.
     */
    private Object logValue(final Object value) {
        return Optional.ofNullable(value).orElse("*");
    }


    /**
     * The ship names are converted into a comma separated string if possible. If not ship names are specified then "*"
     * is returned.
     *
     * @param value The list of ship names.
     * @return A comma separated list of ship names.
     */
    private String logName(final List<String> value) {
        return Optional.ofNullable(value)
                .map(this::getNames)
                .orElse("*");
    }

    /**
     * The location is converted to a name if possible. If no location is specified then "*"
     * is returned.
     *
     * @param value The location value to log.
     * @return The value of the location. A name is returned if possible.
     */
    private String logLocation(final List<String> value) {
        return Optional.ofNullable(value)
                .map(this::getLocations)
                .orElse("*");
    }

    /**
     * The ship types are converted into a comma separated string if possible. If no ship types are specified then "*"
     * is returned.
     *
     * @param value A list of ship types.
     * @return The string value of the list of ship types.
     */
    private String logShip(final List<ShipType> value) {
        return Optional.ofNullable(value)
                .map(this::getShipTypes)
                .orElse("*");
    }
}
