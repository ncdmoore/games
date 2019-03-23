package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a victory condition that must be matched in order for the player to win the game.
 */
@Slf4j
public class RequiredShipVictory implements VictoryCondition<ShipEvent, ShipVictoryData> {

    private List<ShipEventMatcher> matchers;

    @Getter
    private boolean requirementMet;

    /**
     * Constructor.
     *
     * @param data The victory condition data as read from a JSON file.
     * @param factory Factory for creating ship event matchers.
     */
    @Inject
    public RequiredShipVictory(@Assisted final ShipVictoryData data,
                                         final ShipEventMatcherFactory factory) {

        matchers = data.getEvents()
                .stream()
                .map(factory::create)
                .collect(Collectors.toList());

        requirementMet = data.isRequirementMet();

        log.debug("Required victory condition match set:");
        matchers.forEach(ShipEventMatcher::log);

    }

    /**
     * Get the victory data that is read and written. This is all the data that needs to persist for this class.
     *
     * @return The corresponding persistent victory data.
     */
    @Override
    public ShipVictoryData getData() {
        ShipVictoryData data = new ShipVictoryData();
        data.setEvents(PersistentUtility.getData(matchers));
        data.setRequirementMet(requirementMet);
        return data;
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
        requirementMet = matchers
                .stream()
                .anyMatch(matcher -> matcher.match(event));

        String location = event.getShip().getTaskForce().getName();
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
