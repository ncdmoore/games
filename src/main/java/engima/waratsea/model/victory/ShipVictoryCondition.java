package engima.waratsea.model.victory;

import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.victory.data.ShipVictoryData;

/**
 * Represents a ship victory condition.
 */
public interface ShipVictoryCondition {
    /**
     * Indicates that the victory condition was satisfied.
     *
     * @return True if hte victory condition was satisfied. False otherwise.
     */
    boolean isRequirementMet();

    /**
     * Get the victory points awarded when this condition is met.
     *
     * @param  event The fired ship event.
     * @return The victory points awarded.
     */
    int getPoints(ShipEvent event);

    /**
     * Determine if the fired ship event results in any victory points being awarded.
     *
     * @param event The fired ship event.
     * @return True if the fired ship event results in an award of victory points. False otherwise.
     */
    boolean match(ShipEvent event);

    /**
     * Get the corresponding ship victory data that is read and written. This is all the data that
     * needs to persist for this class.
     *
     * @return The corresponding persistent ship victory data.
     */
    ShipVictoryData getData();
}
