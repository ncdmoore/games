package engima.waratsea.model.base.airfield.mission.rules;

import com.google.inject.Inject;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the air mission land strike rules.
 */
public class MissionAirStrikeRules implements MissionAirRules {

    private Map<WeatherType, Integer> weatherModifier = new HashMap<>();

    private Weather weather;

    /**
     * Constructor called by guice.
     *
     * @param weather The weather.
     */
    @Inject
    public MissionAirStrikeRules(final Weather weather) {
        this.weather = weather;

        //CHECKSTYLE:OFF: checkstyle:magicnumber

        weatherModifier.put(WeatherType.CLEAR, 0);
        weatherModifier.put(WeatherType.CLOUDY, 0);
        weatherModifier.put(WeatherType.RAIN, -1);
        weatherModifier.put(WeatherType.SQUALL, -2);
        weatherModifier.put(WeatherType.STORM, -100);
        weatherModifier.put(WeatherType.GALE, -100);

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
