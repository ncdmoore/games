package engima.waratsea.model.base.airfield.mission.rules;

import com.google.inject.Inject;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the air mission sweep rules.
 */
public class MissionAirSweepRules implements MissionAirRules {

    private final Map<WeatherType, Integer> weatherModifier = new HashMap<>();

    private final Weather weather;

    /**
     * Constructor called by guice.
     *
     * @param weather The weather.
     */
    @Inject
    public MissionAirSweepRules(final Weather weather) {
        this.weather = weather;

        //CHECKSTYLE:OFF: checkstyle:magicnumber

        weatherModifier.put(WeatherType.CLEAR, 0);
        weatherModifier.put(WeatherType.CLOUDY, 0);
        weatherModifier.put(WeatherType.RAIN, -1);
        weatherModifier.put(WeatherType.SQUALL, -1);
        weatherModifier.put(WeatherType.STORM, -1);
        weatherModifier.put(WeatherType.GALE, -1);

        //CHECKSTYLE:ON: checkstyle:magicnumber
    }

    /**
     * Get the land attack modifier based on the current weather.
     *
     * @return The land attack modifier based on the current weather.
     */
    @Override
    public int getModifier() {
        return weatherModifier.get(weather.getCurrent());
    }
}
