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

    private static List<WeatherType> values = new ArrayList<>(Arrays.asList(WeatherType.values()));

    private String value;

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
        int index = values.indexOf(this);

        if (!(index == values.size() - 1)) {
            index++;
        }

        return values.get(index);
    }

    /**
     * The weather has improved.
     *
     * @return The new weather.
     */
    public WeatherType improve() {
        int index = values.indexOf(this);

        if (index != 0) {
            index--;
        }

        return values.get(index);
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
}
