package engima.waratsea.model.game.event.squadron;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.squadron.data.SquadronMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.Squadron;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is used to match squadron events. An entity that is looking for a particular squadron event can use this
 * class to detect if the wanted event has occurred.
 */
@Slf4j
public class SquadronEventMatcher implements PersistentData<SquadronMatchData> {
    @Getter
    private final String action;  // The action to match.

    @Getter
    private final List<String> names; // A list of squadron names that match.

    @Getter
    private final Side side; // The side to match.

    @Getter
    private final List<String> aircraftModels; // aircraft name to match.

    @Getter
    private final List<AircraftType> aircraftTypes; // A list of aircraft types to match.

    // A list of starting locations to match. This is the location where a squadron took off.
    // The only event that uses this is the ARRIVAL event. The starting location is the location
    // where the squadron took off. This may be an aircraft carrier or a transport ship if
    // the squadron is launched from catapult.
    @Getter
    private final List<String> airfieldOrigins;  // The airfield names should be stored in the airfield origins. Not airfield references.

    private final List<String> locations; // A list of locations to match. This is the location where the event occurred.

    @Getter
    private final AssetType by;    // The game asset Ship, aircraft or sub that caused the event to fire. The asset that did the event.

    private GameMap gameMap;

    /**
     * Constructor called by guice.
     *
     * @param data squadron event matcher data read in from JSON.
     * @param gameMap The game map.
     */
    @Inject
    public SquadronEventMatcher(@Assisted final SquadronMatchData data,
                                final GameMap gameMap) {
        this.gameMap = gameMap;

        action = data.getAction();
        names = parseNames(data.getName());
        side = data.getSide();
        aircraftModels = parseNames(data.getAircraftModel());
        aircraftTypes = parseAircraftType(data.getAircraftType());
        locations = parseLocation(data.getLocation());
        airfieldOrigins = parseNames(data.getStartingLocation());
        by = data.getBy();
    }

    /**
     * Get the squadron match data. This is the corresponding data that is read and written.
     *
     * @return The corresponding squadron match data.
     */
    @Override
    public SquadronMatchData getData() {
        SquadronMatchData data = new SquadronMatchData();
        data.setAction(action);
        data.setName(Optional.ofNullable(names).map(n -> String.join(",", n)).orElse(null));
        data.setSide(side);
        data.setAircraftModel(Optional.ofNullable(aircraftModels).map(n -> String.join(",", n)).orElse(null));
        data.setAircraftType(Optional.ofNullable(aircraftTypes).map(this::getAircraftTypes).orElse(null));
        data.setLocation(Optional.ofNullable(locations).map(l -> String.join(",", l)).orElse(null));
        data.setStartingLocation(Optional.ofNullable(airfieldOrigins).map(p -> String.join(",", p)).orElse(null));
        return data;
    }

    /**
     * Determines if fired Squadron event matches.
     *
     * @param firedEvent The squadron event to test for matching.
     * @return True if the squadron event matches. False otherwise.
     */
    public boolean match(final SquadronEvent firedEvent) {
        return side == firedEvent.getSquadron().getSide()
                && isActionEqual(firedEvent.getAction())
                && isSquadronTypeEqual(firedEvent.getSquadron())
                && isSquadronNameEqual(firedEvent.getSquadron())
                && isAircraftModelEqual(firedEvent.getSquadron())
                && isLocationEqual(firedEvent.getSquadron())
                && isAirfieldOriginEqual(firedEvent.getSquadron())
                && isByEqual(firedEvent.getBy());
    }

    /**
     * Log the ship event match criteria.
     */
    public void log() {
        log.debug("Match action {}", logValue(action));
        log.debug("Match squadron name {}", logName(names));
        log.debug("Match side {}", logValue(side));
        log.debug("Match aircraft model {}", logName(aircraftModels));
        log.debug("Match aircraft type {}", logAircraft(aircraftTypes));
        log.debug("Match starting location {}", logLocation(airfieldOrigins));
        log.debug("Match location {}", logLocation(locations));
        log.debug("Match by {}", logValue(by));
    }

    /**
     * Parse the string version of aircraft types into a list of AircraftTypes.
     *
     * @param aircraftTypeString The aircraft types in string form. May be null.
     * @return A list of aircraft types.
     */
    private List<AircraftType> parseAircraftType(final String aircraftTypeString) {
        return Optional.ofNullable(aircraftTypeString)
                .map(types -> Stream.of(types.split("\\s*,\\s*"))
                        .map(type -> AircraftType.valueOf(type.toUpperCase().replace(' ', '_')))
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
     * Parse the string version of the squadron names into a list of squadron names.
     *
     * @param nameString A comma separated list of squadron names. May be null.
     * @return A list of squadron names.
     */
    private List<String> parseNames(final String nameString) {
        return Optional.ofNullable(nameString)
                .map(squadronNames -> Stream.of(squadronNames.split("\\s*,\\s*"))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Get the string version of the squadron names.
     *
     * @param squadronNames The list of squadron names.
     * @return The string version of the squadron names.
     */
    private String getNames(@NotNull final List<String> squadronNames) {
        return String.join(", ", squadronNames);
    }

    /**
     * Get the string version of the aircraft types.
     *
     * @param aircraft The list of aircraft types.
     * @return The string version of the aircraft types.
     */
    private String getAircraftTypes(@NotNull final List<AircraftType> aircraft) {
        return aircraft.stream()
                .map(AircraftType::toString)
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
     * Determine if the event fired matches the desired action.
     *
     * @param squadronAction The action of the fired event.
     * @return True if the action of the fired event matches. False otherwise.
     */
    private boolean isActionEqual(final SquadronEventAction squadronAction) {
        return action == null                                                                                           // Non specified action matches all.
                || matchAction(squadronAction);
    }

    /**
     * Determine if the event fired matches the desired squadron name.
     *
     * @param squadron The squadron of the fired event.
     * @return True if the squadron name of the fired event matches. False otherwise.
     */
    private boolean isSquadronNameEqual(final Squadron squadron) {
        return names == null
                || names.contains(squadron.getName());
    }

    /**
     * Determine if the aircraft name matches the desired aircraft name.
     *
     * @param squadron The fired event squadron.
     * @return True if the aircraft name matches. False otherwise.
     */
    private boolean isAircraftModelEqual(final Squadron squadron) {
        return aircraftModels == null                                                                                    // Non specified aircraft name matches all.
                || aircraftModels.contains(squadron.getModel());
    }

    /**
     * Determine if the aircraft type is equal to the fired event aircraft type.
     *
     * @param squadron The fired event aircraft type.
     * @return True if the aircraft types are equal. False otherwise.
     */
    private boolean isSquadronTypeEqual(final Squadron squadron) {
        return aircraftTypes == null                                                                                    // If wildcard then event aircraft type does not matter.
                || aircraftTypes.contains(squadron.getAircraft().getType());
    }

    /**
     * Determine if the location of the event is matched.
     *
     * @param squadron The squadron that experienced the event.
     * @return True if the squadron's location matched. False otherwise.
     */
    private boolean isLocationEqual(final Squadron squadron) {
        return locations == null                                                                                         // If the location is not specified then it does not matter.
                || matchAnyEnemyBase(squadron)
                || matchAnyFriendlyBase(squadron)
                || locations.contains(squadron.getLocation());
    }

    /**
     * Determine if the airfield origin of the event is matched.
     *
     * @param squadron The fired event squadron.
     * @return True if the squadron's airfield origin is matched. False otherwise.
     */
    private boolean isAirfieldOriginEqual(final Squadron squadron) {
        return airfieldOrigins == null
                || airfieldOrigins.contains(squadron.getAirfield().getName());
    }
    /**
     * Determine if the asset that caused the event to fire matches.
     *
     * @param eventBy The asset that caused the event to fire.
     * @return True if the asset that caused the event to fire is matched. False otherwise.
     */
    private boolean isByEqual(final AssetType eventBy) {
        return by == null                                                                                               // If the by asset is not specified then it does not matter.
                || by.equals(eventBy);
    }

    /**
     * Determine if the action matches.
     *
     * @param squadronAction The event squadron action.
     * @return True if the squadron event action matches the desired action. False otherwise.
     */
    private boolean matchAction(final SquadronEventAction squadronAction) {
        boolean result;

        try {
            result = SquadronEventAction.valueOf(action) == squadronAction;
        } catch (IllegalArgumentException ex) {
            log.error("Unable to convert squadron action: '{}'", action);
            result = false;
        }

        return result;
    }

    /**
     * Matches when the event location occurs at an enemy base.
     *
     * @param squadron The squadron experiencing the event.
     * @return True if the squadron is at an enemy base. False otherwise.
     */
    private boolean matchAnyEnemyBase(final Squadron squadron) {
        return locations.contains("ANY_ENEMY_BASE")
                && squadron.atEnemyBase();
    }

    /**
     * Matches when the event location occurs at a friendly base.
     *
     * @param squadron The squadron that experienced the event.
     * @return True if the squadron is at a friendly base. False otherwise.
     */
    private boolean matchAnyFriendlyBase(final Squadron squadron) {
        return locations.contains("ANY_FRIENDLY_BASE")
                && squadron.atFriendlyBase();
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
     * The squadron names are converted into a comma separated string if possible. If no squadron names are specified then "*"
     * is returned.
     *
     * @param value The list of squadron names.
     * @return A comma separated list of squadron names.
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
     * The aircraft types are converted into a comma separated string if possible. If no aircraft types are specified then "*"
     * is returned.
     *
     * @param value A list of ship types.
     * @return The string value of the list of ship types.
     */
    private String logAircraft(final List<AircraftType> value) {
        return Optional.ofNullable(value)
                .map(this::getAircraftTypes)
                .orElse("*");
    }
}
