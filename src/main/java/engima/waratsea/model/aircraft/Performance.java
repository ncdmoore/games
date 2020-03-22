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
    private static final int SEARCH_MODIFIER = 4;              // Squadron configured for search has less ordinance and more fuel. This is the increase in range.
    private static final int ORDINANCE_PAYLOAD_THRESHOLD = 2;  // Additional range threshold for SEARCH and REDUCED_ORDINANCE configurations.

    @Getter private final int gameRange;
    @Getter private final int endurance; // Endurance is equal to turns. Thus, if the endurance is 2, then this equates to 2 turns.
    @Getter private int ferryDistance;   // This is the distance the aircraft may move without returning to base. One way distance.
    @Getter private final int radius;    // The combat radius. This is the distance the aircraft may move in and still return to base.

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
     * @param currentEndurance The aircraft's current endurance.
     * @return The current combat radius.
     */
    private int calculateRadius(final int currentEndurance) {
        return  (gameRange * currentEndurance) / 2 + ((gameRange % 2) * currentEndurance) / 2;  // Two way distance. Return.
    }

    /**
     * Get the total search modifier for this aircraft. If aircraft has extra fuel
     * capacity, then the search modifier is increased by a set amount.
     *
     * @param land The squadron's land attack factor.
     * @param naval The squadron's naval attack factor.
     * @return The total search modifier for this aircraft.
     */
    public int getSearchModifier(final AttackFactor land, final AttackFactor naval) {
        return SEARCH_MODIFIER + (hasExtraFuelCapacity(land, naval) ? SEARCH_MODIFIER : 0);
    }

    /**
     * Determine if this aircraft has extra capacity for fuel if its naval/land payload is reduced.
     * The aircraft must have an ordinance capacity great enough to carry extra fuel. This is
     * determined by the naval/land attack factor of the aircraft. If one of these factors is
     * greater than the defined threshold, then this aircraft can carry extra fuel in place of
     * ordinance to extend its range.
     *
     * @param land The squadron's land attack factor.
     * @param naval The squadron's naval attack factor.
     * @return True if this aircraft has extra fuel capacity. False otherwise.
     */
    private boolean hasExtraFuelCapacity(final AttackFactor land, final AttackFactor naval) {
        return land.getFull() >= ORDINANCE_PAYLOAD_THRESHOLD
                || naval.getFull() >= ORDINANCE_PAYLOAD_THRESHOLD;
    }
}
