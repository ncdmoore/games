package engima.waratsea.model.aircraft;

import engima.waratsea.model.aircraft.data.RangeData;
import lombok.Getter;

/**
 * Represents an aircraft's range.
 */
public class Range {
    @Getter
    private final int range;

    @Getter
    private final int endurance;

    /**
     * Constructor.
     *
     * @param data The aircraft's range data read in from a JSON file.
     */
    public Range(final RangeData data) {
        this.range = data.getRange();
        this.endurance = data.getEndurance();
    }
}
