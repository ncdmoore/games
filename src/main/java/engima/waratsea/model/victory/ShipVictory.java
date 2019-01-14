package engima.waratsea.model.victory;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.victory.data.ShipVictoryData;

/**
 * Represents a ship victory condition.
 */
public class ShipVictory {

    private ShipEventMatcher matcher;

    private int points;

    /**
     * Constructor.
     * @param data The victory condition data as read from a JSON file.
     * @param side The side ALLIES or AXIS.
     */
    public ShipVictory(final ShipVictoryData data, final Side side) {
        matcher = new ShipEventMatcher(data.getEvent());
        points = data.getPoints();

        matcher.setSide(side);
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
