package engima.waratsea.model.aircraft;

import engima.waratsea.model.aircraft.data.PerformanceData;
import lombok.Getter;

/**
 * Represents an aircraft's range.
 *
 * The ferry distance is the aircraft's one way distance. This is used to calculate how far an aircraft squadron
 * may be ferried. The ferry distance determines which friendly airbases a squadron may be ferried.
 *
 * The radius is the squadron combat radius. This is the distance a squadron can fly and still return to base.
 * This determines which targets a squadron may strike and still make it safely back to base.
 *
 * Note, that if an aircraft has an endurance greater than one, that it may take multiple turns for a squadron
 * to reach it's destination. If an aircraft has an endurance greater than two, then it is possible that
 * it make take two turns for the squadron to reach it's attack target. For example if the endurance is three:
 * one turn flying toward the target; one turn continuing to target, attacking target and then heading home; and
 * one turn flying toward home.
 *
 */
public class Performance {
    @Getter private final int gameRange;       // This is the range marked on the board game aircraft piece.
    @Getter private final int endurance;       // Endurance is equal to turns. Thus, if the endurance is 2, then this equates to 2 turns.
    @Getter private final int ferryDistance;   // This is the distance the aircraft may move without returning to base. One way distance.
    @Getter private final int radius;          // The combat radius. This is the distance the aircraft may move in and still return to base.

    /**
     * Constructor.
     *
     * @param data The aircraft's range data read in from a JSON file.
     */
    public Performance(final PerformanceData data) {
        gameRange = data.getRange();       //This is the range marked on the board game aircraft piece.
        endurance = data.getEndurance();  //This is the endurance marked on the board game aircraft piece.
        ferryDistance = calculateFerryDistance(endurance);
        radius = calculateRadius(endurance);
    }

    /**
     * A convenience method for getting the aircraft's range.
     * This is the game range. The range marked on the game piece.
     *
     * Note range and radius are equal when endurance is unity.
     *
     * @return The aircraft's range.
     */
    public int getRange() {
        return gameRange;
    }

    /**
     * Get the enhanced range based off of data from a squadron configuration.
     *
     * @param enhancedRange The enhanced range.
     * @param enhancedEndurance The enhanced endurance.
     * @return The enhanced combat radius.
     */
    public int getEnhancedRadius(final int enhancedRange, final int enhancedEndurance) {
        return  (enhancedRange * enhancedEndurance) / 2 + ((enhancedRange % 2) * enhancedEndurance) / 2;  // Two way distance. Return.
    }

    /**
     * Get the enhanced ferry distance based of the data from a squadron configuration.
     *
     * @param enhancedRange The enhanced range.
     * @param enhancedEndurance The enhanced endurance.
     * @return The enhanced ferry distance.
     */
    public int getEnhancedFerryDistance(final int enhancedRange, final int enhancedEndurance) {
        return enhancedRange * enhancedEndurance;
    }

    /**
     * Calculate the ferry distance.
     *
     * @param currentEndurance The aircraft's current endurance.
     * @return The current ferry distance.
     */
    private int calculateFerryDistance(final int currentEndurance) {
         return gameRange * currentEndurance;  // One way distance. No return.
    }

    /**
     * Calculate the combat radius.
     *
     * Note, range and combat radius are equal when current endurance is unity.
     *
     * @param currentEndurance The aircraft's current endurance.
     * @return The current combat radius.
     */
    private int calculateRadius(final int currentEndurance) {
        return  (gameRange * currentEndurance) / 2 + ((gameRange % 2) * currentEndurance) / 2;  // Two way distance. Return.
    }
}
