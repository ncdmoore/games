package engima.waratsea.model.game.event.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Asset;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.ships.ShipType;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * This class is used to match ship events. An entity that is looking for a particular ship event can use this
 * class to detect if the wanted event has occurred.
 */
@Slf4j
public class ShipEventMatcher {
    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private String taskForceName;

    @Getter
    @Setter
    private String shipType;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private Asset by;    // The game asset Ship, aircraft or sub that caused the event to fire. The asset that did the event.

    /**
     * Constructor.
     * @param data Ship event matcher data read in from JSON.
     * @param gameMap The game map.
     */
    @Inject
    public ShipEventMatcher(@Assisted final ShipMatchData data,
                                      final GameMap gameMap) {
        action = data.getAction();
        name = data.getName();
        side = data.getSide();
        taskForceName = data.getTaskForceName();
        shipType = data.getShipType();
        by = data.getBy();

        location = Optional.ofNullable(data.getLocation())
                .map(gameMap::convertNameToReference)
                .orElse(null);
    }

    /**
     * Determines if two ship events are equal.
     *
     * @param firedEvent The ship event to test for matching.
     * @return True if the ship event matches. False otherwise.
     */
    public boolean match(final ShipEvent firedEvent) {

        return side == firedEvent.getShip().getShipId().getSide()
                && isActionEqual(firedEvent.getAction())
                && isShipTypeEqual(firedEvent.getShip().getType())
                && isTaskForceNameEqual(firedEvent.getShip().getTaskForce())
                && isLocationEqual(firedEvent.getShip().getTaskForce())
                && isByEqual(firedEvent.getBy());
    }

    /**
     * Returns a text string indicating under what conditions the ship event is matched.
     * @return The text explanation string.
     */
    public String getExplanation() {
        String taskForce = taskForceName == null ? "" : " in " + taskForceName;
        String ship = shipType == null ? "ship" : ShipType.valueOf(shipType).toString();
        String place = location == null ? "" : "at " + location;

        return "if an " + side.getPossesive() + " " + ship + taskForce + " " + action.toLowerCase() + place + ".";
    }

    /**
     * Determine if the event fired matches the desired action.
     * @param shipAction The action of the fired event.
     * @return True if the action of the fired event matches. False otherwise.
     */
    private boolean isActionEqual(final ShipEventAction shipAction) {
        return action == null
                || matchActionWildcard(shipAction)
                || matchAction(shipAction);
    }

    /**
     * Determine if the task force names between the two ship events are equal.
     *
     * @param taskForce The event task force.
     * @return True if the two task force names are equal. False otherwise.
     */
    private boolean isTaskForceNameEqual(final TaskForce taskForce) {
        return taskForceName == null                                                                                    // Non specified task force name matches all.
                || taskForceName.equalsIgnoreCase(taskForce.getName());
    }

    /**
     * Determine if the ship types between two ship events are equal.
     *
     * @param firedShipType The other ship event's ship type.
     * @return True if the two ship event's ship types are equal. False otherwise.
     */
    private boolean isShipTypeEqual(final ShipType firedShipType) {
        return shipType == null                                                                                         // If wildcard then event ship type does not matter.
                || ShipType.valueOf(shipType) == firedShipType;
    }

    /**
     * Determine if the location of the event is matched.
     * @param taskForce The task force of the ship that experienced the event.
     * @return True if the ship's location matched. False otherwise.
     */
    private boolean isLocationEqual(final TaskForce taskForce) {
        return location == null                                                                                         // If the location is not specified then it does not matter.
                || matchAnyEnemyBase(taskForce)
                || matchAnyFriendlyBase(taskForce)
                || location.equalsIgnoreCase(taskForce.getLocation());
    }

    /**
     * Determine if the asset that caused the event to fire matches.
     * @param eventBy The asset that caused the event to fire.
     * @return True if the asset that caused the event to fire is matched. False otherwise.
     */
    private boolean isByEqual(final Asset eventBy) {
        return by == null                                                                                               // If the by asset is not specified then it does not matter.
                || by.equals(eventBy);
    }

    /**
     * Determine if the action matches.
     * @param shipAction The event ship action.
     * @return True if the ship event action matches the desired action. False otherwise.
     */
    private boolean matchAction(final ShipEventAction shipAction) {
        boolean result;

        try {
            result = ShipEventAction.valueOf(action) == shipAction;
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to convert ship action: '{}'", action);
            result = false;
        }

        return result;
    }

    /**
     * Matches when a task force is located at an enemy base. Used to match ship
     * bombardment events.
     * @param taskForce The task force that contains the ship experiencing the event.
     * @return True if the ship is at an enemy base and any base will match. False otherwise.
     */
    private boolean matchAnyEnemyBase(final TaskForce taskForce) {
        return location.equalsIgnoreCase("ANY_ENEMY_BASE")
                && taskForce.atEnemyBase();
    }

    /**
     * Matches the event location if the desired location is a friendly location and the event location occurs at a
     * friendly base.
     * @param taskForce The task force of the ship that experienced the event.
     * @return True if the location matches. False otherwise.
     */
    private boolean matchAnyFriendlyBase(final TaskForce taskForce) {
        return location.equalsIgnoreCase("ANY_FRIENDLY_BASE")
                && taskForce.atFriendlyBase();
    }

    /**
     * Matches event action wildcard. For example if the event action is DAMAGED_HULL and the
     * desired event is simply DAMAGED, then there is a match.
     * @param shipAction The ship event action.
     * @return True if the ship event action matches. Otherwise false.
     */
    private boolean matchActionWildcard(final ShipEventAction shipAction) {
        return shipAction.toString().toLowerCase().contains(action.trim().toLowerCase());
    }

    /**
     * Log the ship event match criteria.
     */
    public void log() {
        log.info("Match action {}", logValue(action));
        log.info("Match name {}", logValue(name));
        log.info("Match side {}", logValue(side));
        log.info("Match task force name {}", logValue(taskForceName));
        log.info("Match ship type {}", logValue(shipType));
        log.info("Match location {}", logValue(location));
        log.info("Match by {}", logValue(by));
    }

    /**
     * If not value is present output an "*".
     * @param value The value to log.
     * @return The value that is actually logged.
     */
    private Object logValue(final Object value) {
        return (value == null) ? "*" : value;
    }
}
