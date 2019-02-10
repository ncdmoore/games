package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.victory.data.ShipVictoryData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a victory condition that must be matched in order for the player to win the game.
 */
@Slf4j
public class RequiredShipVictory implements ShipVictoryCondition {

    private GameMap gameMap;

    private List<ShipEventMatcher> matchers;

    @Getter
    private boolean requirementMet = false;

    /**
     * Constructor.
     *
     * @param data The victory condition data as read from a JSON file.
     * @param side The side ALLIES or AXIS.
     * @param factory Factory for creating ship event matchers.
     * @param gameMap The game map.
     */
    @Inject
    public RequiredShipVictory(@Assisted final ShipVictoryData data,
                               @Assisted final Side side,
                                         final ShipEventMatcherFactory factory,
                                         final GameMap gameMap) {

        matchers = data.getEvents().stream().map(factory::create).collect(Collectors.toList());
        matchers.forEach(matcher -> matcher.setSide(side));

        log.info("Required victory condition match set:");
        matchers.forEach(ShipEventMatcher::log);

        this.gameMap = gameMap;
    }

    /**
     * Determine if a ship event thrown results in meeting the required victory condition.
     *
     * @param event The fired ship event.
     * @return True if the requirement is met. False otherwise.
     */
    public boolean match(final ShipEvent event) {
        // Check all event matchers. If any one event matcher matches the fired event
        // then the victory condition is met.
        requirementMet = matchers.stream().anyMatch(matcher -> matcher.match(event));
        String location = gameMap.convertReferenceToName(event.getShip().getTaskForce().getLocation());
        log.info("Ship '{}' '{}' at location '{}' results in requirement met: {}",
                new Object[] {event.getShip().getName(), event.getAction(), location, requirementMet});

        return requirementMet;
    }

    /**
     * Get the victory points awarded when this condition is met.
     *
     * @return The victory points awarded.
     */
    @Override
    public int getPoints(final ShipEvent event) {
        return 0;
    }
}
