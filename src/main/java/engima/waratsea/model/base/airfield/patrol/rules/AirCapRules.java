package engima.waratsea.model.base.airfield.patrol.rules;

import com.google.inject.Inject;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;
import engima.waratsea.utility.Dice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirCapRules implements AirRules {
    private static final int INTERCEPT_FACTOR = 3;

    private final Map<WeatherType, Integer> weatherFactor = new HashMap<>();
    private final Map<Integer, Integer> distanceFactor = new HashMap<>();

    private final Weather weather;
    private final Dice dice;

    /**
     * Constructor called by guice.
     *
     * @param weather The game's weather.
     * @param dice A dice utility.
     */
    @Inject
    public AirCapRules(final Weather weather, final Dice dice) {
        this.weather = weather;
        this.dice = dice;

        //CHECKSTYLE:OFF: checkstyle:magicnumber
        weatherFactor.put(WeatherType.CLEAR, 0);
        weatherFactor.put(WeatherType.CLOUDY, 0);
        weatherFactor.put(WeatherType.RAIN, -1);
        weatherFactor.put(WeatherType.SQUALL, -100);
        weatherFactor.put(WeatherType.STORM, -100);
        weatherFactor.put(WeatherType.GALE, -100);

        distanceFactor.put(0, 0);
        distanceFactor.put(1, -1);
        distanceFactor.put(2, -2);

    }

    /**
     * Get the chance of a intercepting an enemy air mission.
     *
     * @param distance The distance intercept takes place from the air base.
     * @return An integer indicating the percentage chance of success.
     */
    public int getBaseSearchSuccess(final int distance, final List<Squadron> squadrons) {
        int factor = squadrons.isEmpty() ? 0 : getBaseFactor(distance) + INTERCEPT_FACTOR;

        return dice.probability6(factor, 1);
    }

    /**
     * Get the chance of a intercepting an enemy air mission excluding weather factors.
     *
     * @param distance The distance intercept takes place from the air base.
     * @return An integer indicating the percentage chance of success.
     */
    public int getBaseSearchSuccessNoWeather(final int distance, final List<Squadron> squadrons) {
        int factor = squadrons.isEmpty() ? 0 : getBaseFactorNoWeather(distance) + INTERCEPT_FACTOR;

        return dice.probability6(factor, 1);
    }

    @Override
    public int getBaseAttackSuccess(final int distance, final List<Squadron> squadrons) {
        return 0;
    }

    /**
     * Determine if the current weather affects a patrol.
     *
     * @return True if the weather affects the patrol. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return weatherFactor.get(weather.getCurrent()) < 0;
    }

    /**
     * Get the base air cap intercept factor. The base air intercept factor is used to give an indication of
     * the chance of successfully intercepting an enemy air mission.
     *
     * @param distance The distance from the air base to the intercept.
     * @return The base factor.
     */
    private int getBaseFactor(final int distance) {
        return getDistanceFactor(distance) + getWeatherFactor();
    }

    /**
     * Get the base air cap intercept factor excluding the influence of the weather.
     * The base air intercept factor is used to give an indication of
     * the chance of successfully intercepting an enemy air mission.
     *
     * @param distance The distance from the air base to the intercept.
     * @return The base factor.
     */
    private int getBaseFactorNoWeather(final int distance) {
        return getDistanceFactor(distance);
    }

    /**
     * Determine the air search weather factor.
     *
     * @return The weather factor.
     */
    private int getWeatherFactor() {
        return weatherFactor.get(weather.getCurrent());
    }

    /**
     * Determine the air search distance factor.
     *
     * @param distance The distance to the target.
     * @return The distance factor.
     */
    private int getDistanceFactor(final int distance) {
        return distanceFactor.getOrDefault(distance, -100);
    }
}
