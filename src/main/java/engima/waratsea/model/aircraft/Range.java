package engima.waratsea.model.aircraft;

import engima.waratsea.model.aircraft.data.RangeData;
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
 * Note, that if an aircraft has an endurance greater than one, that it may take multiple turns for an squadron
 * to reach it's ferry destination. If an aircraft has an endurance greater than two, then it is possible that
 * it make take two turns for the squadron to reach it's attack target. For example if the endurance is three:
 * one turn flying toward the target; one turn continuing to target, attacking target and then heading home; and
 * one turn flying toward home.
 *
 */
public class Range {
    @Getter
    private final int ferryDistance; // This is the distance the aircraft may move without returning to base. One way distance.

    @Getter
    private final int radius;    // The combat radius. This is the distance the aircraft may move in and still return to base.

    @Getter
    private final int endurance; // Endurance is equal to turns. Thus, if the endurance is 2, then this equates to 2 turns.

    /**
     * Constructor.
     *
     * @param data The aircraft's ferryDistance data read in from a JSON file.
     */
    public Range(final RangeData data) {

        int gameRange = data.getRange();       //This is the range marked on the board game aircraft piece.
        this.endurance = data.getEndurance();  //This is the endurance marked on the board game aircraft piece.

        this.ferryDistance = gameRange * endurance;  // One way distance. No return.

        this.radius = (gameRange * endurance) / 2 + ((gameRange % 2) * endurance) / 2;  // Two way distance. Return.
    }
}
