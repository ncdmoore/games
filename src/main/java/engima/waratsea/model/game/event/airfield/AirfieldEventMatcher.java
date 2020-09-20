package engima.waratsea.model.game.event.airfield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.airfield.data.AirfieldMatchData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is used to match airfield events. An entity that is looking for a particular airfield event can use this
 * class to detect if the wanted event has occurred.
 */
@Slf4j
public class AirfieldEventMatcher implements PersistentData<AirfieldMatchData> {
    @Getter
    private final String action; // The event action to match.

    @Getter
    private final List<String> names;  // The name to match of the airfield that experienced the event.

    @Getter
    private final Side side; // The side to match of the airfield that experienced the event.

    @Getter
    private final int value;

    @Getter
    private final AssetType by;  // The asset Ship, Aircraft, etc to match that performed the event.


    /**
     * Constructor called by guice.
     *
     * @param data airfield event matcher data read in from JSON.
     */
    @Inject
    public AirfieldEventMatcher(@Assisted final AirfieldMatchData data) {
        action = data.getAction();
        names = parseNames(data.getName());
        side = data.getSide();
        value = data.getValue();
        by = data.getBy();
    }

    /**
     * Get the airfield match data. This is the corresponding data that is read and written.
     *
     * @return The corresponding airfield match data.
     */
    @Override
    public AirfieldMatchData getData() {
        AirfieldMatchData data = new AirfieldMatchData();
        data.setAction(action);
        data.setName(Optional.ofNullable(names).map(n -> String.join(",", n)).orElse(null));
        data.setSide(side);
        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
    }

    /**
     * Determines if two ship events are equal.
     *
     * @param firedEvent The airfield event to test for matching.
     * @return True if the airfield event matches. False otherwise.
     */
    public boolean match(final AirfieldEvent firedEvent) {
        return side == firedEvent.getAirfield().getSide()
                && isActionEqual(firedEvent.getAction())
                && isNameEqual(firedEvent.getAirfield().getName())
                && isByEqual(firedEvent.getBy());
    }

    /**
     * Get the airfield action event String representation.
     *
     * @return The airfield action event String representation.
     */
    public String getActionString() {
        try {
            return AirfieldEventAction.valueOf(action).toString();
        } catch (IllegalArgumentException ex) {
            return action;
        }
    }

    /**
     * Get the airfield names String representation.
     *
     * @return The airfield names String representation.
     */
    public String getAirfieldNamesString() {
        return logName(names);
    }

    /**
     * Log the ship event match criteria.
     */
    public void log() {
        log.debug("Match side {}", logValue(side));
        log.debug("Match action {}", logValue(action));
        log.debug("Match name {}", logName(names));
        log.debug("Match by {}", logValue(by));
    }

    /**
     * Determine if the event fired matches the desired action.
     *
     * @param airfieldAction The action of the fired event.
     * @return True if the action of the fired event matches. False otherwise.
     */
    private boolean isActionEqual(final AirfieldEventAction airfieldAction) {
        return action == null                                                                                           // Non specified action matches all.
                || matchAction(airfieldAction);
    }

    /**
     * Determine if the event fired matches the desired airfield name.
     *
     * @param airfieldName The airfield name of the fired event.
     * @return True if the airfield name of the fired event matches. False otherwise.
     */
    private boolean isNameEqual(final String airfieldName) {
        return names == null
                || names.contains(airfieldName);
    }

    /**
     * Determine if the asset that caused the event to fire matches.
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
     * @param airfieldAction The event airfield action.
     * @return True if the airfield event action matches the desired action. False otherwise.
     */
    private boolean matchAction(final AirfieldEventAction airfieldAction) {
        boolean result;

        try {
            result = AirfieldEventAction.valueOf(action) == airfieldAction;
        } catch (IllegalArgumentException ex) {
            log.error("Unable to convert airfield action: '{}'", action);
            result = false;
        }

        return result;
    }

    /**
     * Parse the string version of the airfield names into a list of airfield names.
     *
     * @param nameString A comma separated list of airfield names. May be null.
     * @return A list of airfield names.
     */
    private List<String> parseNames(final String nameString) {
        return Optional.ofNullable(nameString)
                .map(airfieldNames -> Stream.of(airfieldNames.split("\\s*,\\s*"))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Get the string version of the airfield names.
     *
     * @param airfieldNames The list of airfield names.
     * @return The string version of the airfield names.
     */
    private String getNames(@NotNull final List<String> airfieldNames) {
        return String.join(", ", airfieldNames);
    }

    /**
     * If a value is not present output an "*".
     *
     * @param matchValue The value to log.
     * @return The value that is actually logged.
     */
    private Object logValue(final Object matchValue) {
        return Optional.ofNullable(matchValue).orElse("*");
    }


    /**
     * The airfield names are converted into a comma separated string if possible. If no airfield names are specified then "*"
     * is returned.
     *
     * @param matchValue The list of airfield names.
     * @return A comma separated list of airfield names.
     */
    private String logName(final List<String> matchValue) {
        return Optional.ofNullable(matchValue)
                .map(this::getNames)
                .orElse("*");
    }
}
