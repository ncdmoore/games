package engima.waratsea.model.base.airfield.mission.rules;

import java.util.Map;

public interface MissionAirRules {
    /**
     * Get the mission air rule modifier.
     *
     * @return The mission air rule modifier.
     */
    int getModifier();

    /**
     * Determine if the mission is affected by the current weather.
     *
     * @return True if the mission is adversely affected by the current weather. False otherwise.
     */
    boolean isAffectedByWeather();

    /**
     * Get the mission air rules modifiers. This returns a map of the game
     * event to how this event affects the mission. For example,
     *
     *  Weather => -1
     *
     * @return A map of game events to how the event affects the mission.
     */
    Map<String, Integer> getModifierMap();
}
