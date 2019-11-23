package engima.waratsea.model.base.airfield.patrol.rules;

import com.google.inject.Inject;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;
import engima.waratsea.utility.Dice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A singleton that calculates air ASW results.
 *
 */
public class AirAswRules implements AirRules {
    private static final int ATTACK_FACTOR = 1;

    private static final int STEP_FACTOR = 5;

    private Map<WeatherType, Integer> weatherFactor = new HashMap<>();

    private final Weather weather;
    private final Dice dice;
    private final SearchRules searchRules;

    /**
     * Constructor called by guice.
     *
     * @param weather The game's weather.
     * @param dice A dice utility.
     */
    @Inject
    public AirAswRules(final Weather weather,
                       final Dice dice) {
        this.weather = weather;
        this.dice = dice;
        this.searchRules = new SearchRules(AssetType.SUB, weather, dice);

        //CHECKSTYLE:OFF: checkstyle:magicnumber

        weatherFactor.put(WeatherType.CLEAR, 0);
        weatherFactor.put(WeatherType.CLOUDY, 0);
        weatherFactor.put(WeatherType.RAIN, -1);
        weatherFactor.put(WeatherType.SQUALL, -2);
        weatherFactor.put(WeatherType.STORM, -100);
        weatherFactor.put(WeatherType.GALE, -100);

        //CHECKSTYLE:ON: checkstyle:magicnumber
    }

    /**
     * Get the ASW attack success rate.
     *
     * @param distance The distance from the patrol base to the target in grids.
     * @param squadrons The squadrons on ASW patrol.
     * @return An integer indicating the percentage chance of success.
     */
    public int getBaseAttackSuccess(final int distance, final List<Squadron> squadrons) {
        int factor = getBaseFactor(squadrons);
        int canSearch = getBaseSearchSuccess(distance, squadrons);
        return canSearch > 0 ? dice.probability6(factor + ATTACK_FACTOR, 1) : 0;
    }

    /**
     * Determine if the current weather affects a patrol.
     *
     * @return True if the weather affects the patrol. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return weatherFactor.get(weather.getCurrent()) < 0 || searchRules.isAffectedByWeather();
    }

    /**
     * Get the ASW search success rate.
     *
     * @param distance The distance from the patrol base to the target in grids.
     * @param squadrons The squadrons on ASW patrol.
     * @return An integer indicating the percentage chance of success.
     */
    public int getBaseSearchSuccess(final int distance, final List<Squadron> squadrons) {
        return searchRules.getBaseSearchSuccess(distance, squadrons);
    }

    /**
     * Get the chance of a spotting a task force excluding current weather conditions.
     *
     * @param distance The distance of the target from the air base.
     * @param squadrons The number of squadrons involved in the search.
     * @return An integer indicating the percentage chance of success excluding the current weather conditions.
     */
    public int getBaseSearchSuccessNoWeather(final int distance, final List<Squadron> squadrons) {
        return searchRules.getBaseSearchSuccessNoWeather(distance, squadrons);
    }

    /**
     * Get the base air ASW factor. The base ASW factor does not take into account the type of target, nor
     * the actions of the target (Like location). The base ASW search factor is used to give an indication of
     * the chance of successfully attacking an enemy submarine.
     *
     * @param squadrons The number of squadrons involved in the air search.
     * @return The base factor.
     */
    private int getBaseFactor(final List<Squadron> squadrons) {
        int steps = squadrons
                .stream()
                .map(Squadron::getSteps)
                .map(BigDecimal::intValue)
                .reduce(0, Integer::sum);

        return  getWeatherFactor() + getStepFactor(steps);
    }

    /**
     * Get the step factor.
     *
     * @param steps The number of steps in the ASW patrol.
     * @return The step factor.
     */
    private int getStepFactor(final int steps) {
        return steps / STEP_FACTOR;
    }

    /**
     * Get the weather factor.
     * @return The weather factor.
     */
    private int getWeatherFactor() {
        return weatherFactor.get(weather.getCurrent());
    }
}
