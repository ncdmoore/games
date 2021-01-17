package engima.waratsea.model.base.airfield.mission.rules;

import com.google.inject.Inject;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    /**
     * Determine if the mission is affected by the current weather.
     *
     * @return True if the mission is adversely affected by the current weather. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return getModifier() < 0;
    }

    /**
     * Get the mission air rules modifiers. This returns a map of the game
     * event to how this event affects the mission. For example,
     * <p>
     * Weather => -1
     *
     * @return A map of game events to how the event affects the mission.
     */
    @Override
    public Map<String, Integer> getModifierMap() {
        Map<String, Integer> modifierMap = new LinkedHashMap<>();
        modifierMap.put("Weather", getModifier());
        return modifierMap;
    }
}
