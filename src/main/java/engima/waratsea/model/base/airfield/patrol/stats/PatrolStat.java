package engima.waratsea.model.base.airfield.patrol.stats;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an individual patrol stat.
 */
public class PatrolStat {
    @Getter
    @Setter
    private String value; // The actual patrol stat value.

    @Getter
    @Setter
    private String factors; // The factors that determined or contributed to the stat value.

    public PatrolStat(final String value) {
        this.value = value;
    }

    public PatrolStat(final int value) {
        this.value = value + "";
    }

    public PatrolStat(final String value, final String factors) {
        this.value = value;
        this.factors = factors;
    }

    public PatrolStat(final int value, final String factors) {
        this.value = value + "";
        this.factors = factors;
    }
}
