package engima.waratsea.model.base.airfield.mission.rules;

import com.google.inject.Inject;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the air mission land strike rules.
 */
public class MissionAirStrikeRules implements MissionAirRules {
    private final Map<WeatherType, Integer> weatherModifier;
    private final Weather weather;

    /**
     * Constructor called by guice.
     *
     * @param weather The weather.
     */
    @Inject
    public MissionAirStrikeRules(final Weather weather) {
        this.weather = weather;

        //CHECKSTYLE:OFF: checkstyle:magicnumber
        weatherModifier = Map.of(
                WeatherType.CLEAR, 0,
                WeatherType.CLOUDY, 0,
                WeatherType.RAIN, -1,
                WeatherType.SQUALL, -2,
                WeatherType.STORM, -100,
                WeatherType.GALE, -100
        );
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
