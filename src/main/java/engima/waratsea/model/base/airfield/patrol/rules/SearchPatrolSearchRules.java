package engima.waratsea.model.base.airfield.patrol.rules;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;
import engima.waratsea.utility.Dice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class for air search.
 */
public class SearchPatrolSearchRules {
    private static final int SHIP_SEARCH_FACTOR = 3;
    private static final int DEFAULT_STEP_FACTOR = 3;
    private static final int DEFAULT_DISTANCE_FACTOR = -4;

    private final Map<Integer, Integer> stepFactor = new HashMap<>();
    private final Map<WeatherType, Integer> weatherFactor = new HashMap<>();
    private final Map<Integer, Integer> distanceFactor = new HashMap<>();
    private final Map<Boolean, Integer> fighterFactor = new HashMap<>();

    private final Weather weather;
    private final Dice dice;

    private final int searchFactor;

    /**
     * Constructor called by guice.
     *
     *  @param weather The game's weather.
     * @param dice A dice utility.
     */
    @Inject
    public SearchPatrolSearchRules(final Weather weather,
                                   final Dice dice) {
        this.weather = weather;
        this.dice = dice;

        //CHECKSTYLE:OFF: checkstyle:magicnumber
        stepFactor.put(0, 0);
        stepFactor.put(1, 0);
        stepFactor.put(2, 0);
        stepFactor.put(3, 1);
        stepFactor.put(4, 1);
        stepFactor.put(5, 2);

        weatherFactor.put(WeatherType.CLEAR, 0);
        weatherFactor.put(WeatherType.CLOUDY, -1);
        weatherFactor.put(WeatherType.RAIN, -2);
        weatherFactor.put(WeatherType.SQUALL, -3);
        weatherFactor.put(WeatherType.STORM, -100);
        weatherFactor.put(WeatherType.GALE, -100);

        distanceFactor.put(0, 0);
        distanceFactor.put(1, 0);
        distanceFactor.put(2, 0);
        distanceFactor.put(3, -1);
        distanceFactor.put(4, -1);
        distanceFactor.put(5, -2);
        distanceFactor.put(6, -2);
        distanceFactor.put(7, -3);
        distanceFactor.put(8, -3);

        fighterFactor.put(false, 0);
        fighterFactor.put(true, -1);
        //CHECKSTYLE:ON: MagicNumber

        searchFactor = SHIP_SEARCH_FACTOR;
    }

    /**
     * Get the chance of a spotting a task force.
     *
     * @param distance The distance of the target from the air base.
     * @param squadrons The number of squadrons involved in the search.
     * @return An integer indicating the percentage chance of success.
     */
    public int getBaseSearchSuccess(final int distance, final List<Squadron> squadrons) {
        int factor = getBaseFactor(distance, squadrons);
        return dice.probabilityPercentage(factor + searchFactor, 1);
    }

    /**
     * Get the chance of spotting a task force excluding current weather conditions.
     *
     * @param distance The distance of the target from the air base.
     * @param squadrons The number of squadrons involved in the search.
     * @return An integer indicating the percentage chance of success excluding the current weather conditions.
     */
    public int getBaseSearchSuccessNoWeather(final int distance, final List<Squadron> squadrons) {
        int factor = getBaseFactorNoWeather(distance, squadrons);
        return dice.probabilityPercentage(factor + searchFactor, 1);
    }

    /**
     * Get the factors that determine the chance of spotting a tas force.
     *
     * @param distance The distance of the target from the air base.
     * @param squadrons The number of squadrons involved in the search.
     * @return A map of factor name to factor value.
     */
    public Map<String, String> getBaseFactors(final int distance, final List<Squadron> squadrons) {
        int steps = getSteps(squadrons);
        boolean fighterPresent = areFightersPresent(squadrons);

        Map<String, String> factorMap = new LinkedHashMap<>();
        factorMap.put("Base Factor", searchFactor + "");
        factorMap.put("Distance Factor", getDistanceFactor(distance) + "");
        factorMap.put("Step Factor", getStepFactor(steps) + "");
        factorMap.put("Fighters Factor", getFighterFactor(fighterPresent) + "");
        factorMap.put("Weather Factor", getWeatherFactor() + "");

        return factorMap;
    }

    /**
     * Determine if the search patrol is affected by the current weather.
     *
     * @return True if the patrol is adversely affected by the current weather. False otherwise.
     */
    public boolean isAffectedByWeather() {
        return weatherFactor.get(weather.getCurrent()) < 0;
    }

    /**
     * Get the base air search factor. The base search factor does not take into account the type of target, nor
     * the actions of the target (Like unloading). The base air search factor is used to give an indication of
     * the chance of successfully spotting an enemy task force.
     *
     * @param distance The distance to the target.
     * @param squadrons The number of squadrons involved in the air search.
     * @return The base factor.
     */
    private int getBaseFactor(final int distance, final List<Squadron> squadrons) {
        int steps = getSteps(squadrons);
        boolean fighterPresent = areFightersPresent(squadrons);
        return getDistanceFactor(distance) + getWeatherFactor() + getStepFactor(steps) + getFighterFactor(fighterPresent);
    }

    /**
     * Get the base air search factor excuding the weather. This show how much the weather affects air search.
     *
     * @param distance The distance to the target.
     * @param squadrons The number of squadrons involved in the air search.
     * @return The base factor excluding weather conditions.
     */
    private int getBaseFactorNoWeather(final int distance, final List<Squadron> squadrons) {
        int steps = getSteps(squadrons);
        boolean fighterPresent = areFightersPresent(squadrons);
        return getDistanceFactor(distance) + getStepFactor(steps) + getFighterFactor(fighterPresent);
    }

    /**
     * Determine the air search step factor.
     *
     * @param steps The number of steps involved in the search.
     * @return The step factor.
     */
    private int getStepFactor(final int steps) {
        return stepFactor.getOrDefault(steps, DEFAULT_STEP_FACTOR);
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
        return distanceFactor.getOrDefault(distance, DEFAULT_DISTANCE_FACTOR);
    }

    /**
     * Get the fighter squadron present factor.
     *
     * @param present Indicates if fighters are present.
     * @return The fighter squadron present factor.
     */
    private int getFighterFactor(final boolean present) {
        return fighterFactor.get(present);
    }

    /**
     * Get the number of steps involved in the patrol.
     *
     * @param squadrons The squadrons on the patrol.
     * @return The number of steps on the patrol.
     */
    private int getSteps(final List<Squadron> squadrons) {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .map(BigDecimal::intValue)
                .reduce(0, Integer::sum);
    }

    /**
     * Determine if any fighters are present on the patrol.
     *
     * @param squadrons The squadrons on the patrol.
     * @return True if one of the squadrons on the patrol is a fighter squadron. False otherwise.
     */
    private boolean areFightersPresent(final List<Squadron> squadrons) {
        return squadrons
                .stream()
                .anyMatch(squadron -> squadron.getType() == AircraftType.FIGHTER);
    }
}
