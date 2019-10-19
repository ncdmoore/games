package engima.waratsea.model.weather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The types of weather represented in the game.
 */
public enum WeatherType {

    CLEAR,
    CLOUDY,
    RAIN,
    SQUALL,
    STORM,
    GALE;

    private static List<WeatherType> values = new ArrayList<>(Arrays.asList(WeatherType.values()));

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
}
