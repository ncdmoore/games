package engima.waratsea.model.aircraft;

import engima.waratsea.model.aircraft.data.RangeData;
import lombok.Getter;

/**
 * Represents an aircraft's ferryDistance.
 */
public class Range {
    @Getter
    private final int ferryDistance;

    @Getter
    private final int radius;

    @Getter
    private final int endurance;

    /**
     * Constructor.
     *
     * @param data The aircraft's ferryDistance data read in from a JSON file.
     */
    public Range(final RangeData data) {
        this.ferryDistance = data.getFerryDistance();
        this.radius = ferryDistance / 2 + ferryDistance % 2;
        this.endurance = data.getEndurance();
    }
}
