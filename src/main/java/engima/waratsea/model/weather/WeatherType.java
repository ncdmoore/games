package engima.waratsea.model.weather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The types of weather represented in the game.
 */
public enum WeatherType {

    CLEAR("Clear"),
    CLOUDY("Cloudy"),
    RAIN("Rain"),
    SQUALL("Squall"),
    STORM("Storm"),
    GALE("Gale");

    private static final List<WeatherType> VALUES = new ArrayList<>(Arrays.asList(WeatherType.values()));

    private final String value;

    /**
     * The constructor.
     *
     * @param value The String representation of this enum.
     */
    WeatherType(final String value) {
        this.value = value;
    }

    /**
     * The weather has worsened.
     *
     * @return The new weather.
     */
    public WeatherType worsen() {
        int index = VALUES.indexOf(this);

        if (!(index == VALUES.size() - 1)) {
            index++;
        }

        return VALUES.get(index);
    }

    /**
     * The weather has improved.
     *
     * @return The new weather.
     */
    public WeatherType improve() {
        int index = VALUES.indexOf(this);

        if (index != 0) {
            index--;
        }

        return VALUES.get(index);
    }

    /**
     * The weather has not changed.
     *
     * @return The new weather.
     */
    public WeatherType noChange() {
        return this;
    }

    /**
     * Get the String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Get the lower case String representation of this enum.
     *
     * @return THe lower case String representation of this enum.
     */
    public String toLower() {
        return toString().toLowerCase();
    }
}
