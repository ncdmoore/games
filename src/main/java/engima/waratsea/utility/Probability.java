package engima.waratsea.utility;

import com.google.inject.Singleton;

/**
 * Probability utility class used to group probability functions in a single location.
 *
 */
@Singleton
public class Probability {

    private static final int PERCENTAGE = 100;
    /**
     * Convert a decimal value into an integer percentage value.
     *
     * @param decimalValue The decimal value.
     * @return The equivalent integer percentage truncated.
     */
    public int percentage(final double decimalValue) {
        return (int) (decimalValue * PERCENTAGE);
    }
}
