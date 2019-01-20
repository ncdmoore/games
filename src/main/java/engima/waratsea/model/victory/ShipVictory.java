package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.victory.data.ShipVictoryData;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a ship victory condition.
 */
@Slf4j
public class ShipVictory {

    private ShipEventMatcher matcher;

    private int points;

    /**
     * Constructor.
     * @param data The victory condition data as read from a JSON file.
     * @param side The side ALLIES or AXIS.
     * @param factory Factory for creating ship event matchers.
     */
    @Inject
    public ShipVictory(@Assisted final ShipVictoryData data,
                       @Assisted final Side side,
                                 final ShipEventMatcherFactory factory) {


        matcher = factory.create(data.getEvent());
        points = data.getPoints();

        matcher.setSide(side);

        log.info("Victory condition match:");
        matcher.log();
        log.info("Points: {}", points);
    }

    /**
     * Determine if a ship event thrown resutls in a change in victory points.
     * @param event The fired ship event.
     * @return True if the fired ship event is one that resutls in a change in victory points.
     */
    public boolean match(final ShipEvent event) {
         return matcher.match(event);
    }

    /**
     * Determine the victory points.
     * @param event The fired ship event.
     * @return The number of victory points award due to the fired ship event.
     */
    public int getPoints(final ShipEvent event) {
        int result;

        switch (ShipEventAction.valueOf(matcher.getAction())) {
            case SUNK:
                result = event.getShip().getVictoryPoints();
                break;
            case OUT_OF_FUEL:
                result = event.getShip().getVictoryPoints() / 2;
                break;
            default:
                result = points;
        }
        return result;
    }
}
