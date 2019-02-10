package engima.waratsea.model.victory;

import engima.waratsea.model.game.event.ship.ShipEvent;

/**
 * Victory condition.
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
}
