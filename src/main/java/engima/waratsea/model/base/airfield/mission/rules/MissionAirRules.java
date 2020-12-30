package engima.waratsea.model.base.airfield.mission.rules;

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
}
