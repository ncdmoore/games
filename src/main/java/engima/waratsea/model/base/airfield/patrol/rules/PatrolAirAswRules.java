package engima.waratsea.model.base.airfield.patrol.rules;

import com.google.inject.Inject;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;
import engima.waratsea.utility.Dice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A singleton that calculates air ASW results.
 *
 */
public class PatrolAirAswRules implements PatrolAirRules {
    private static final int ATTACK_FACTOR = 1;

    private static final int STEP_FACTOR = 5;

    private final Map<WeatherType, Integer> weatherFactor = new HashMap<>();

    private final Weather weather;
    private final Dice dice;
    private final AswPatrolSearchRules searchRules;

    /**
     * Constructor called by guice.
     *
     * @param weather The game's weather.
     * @param dice A dice utility.
     */
    @Inject
    public PatrolAirAswRules(final Weather weather,
                             final Dice dice) {
        this.weather = weather;
        this.dice = dice;
        this.searchRules = new AswPatrolSearchRules(weather, dice);

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
        int factor = getBaseAttackFactor(squadrons);
        int canSearch = getBaseSearchSuccess(distance, squadrons);
        return canSearch > 0 ? dice.probabilityPercentage(factor + ATTACK_FACTOR, 1) : 0;
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
     * The base search success factors.
     *
     * @param distance  The distance to the target from the patrol base.
     * @param squadrons The squadrons that are within range of the target.
     * @return A map of factor name to factor value.
     */
    @Override
    public Map<String, String> getBaseSearchFactors(final int distance, final List<Squadron> squadrons) {
        return searchRules.getBaseFactors(distance, squadrons);
    }

    /**
     * The base attack success factors.
     *
     * @param distance  The distance to the target from the patrol base.
     * @param squadrons The squadrons that are within range of the target.
     * @return A map of factor name to factor value.
     */
    @Override
    public Map<String, String> getBaseAttackFactors(final int distance, final List<Squadron> squadrons) {
        int steps = getSteps(squadrons);

        Map<String, String> factors = new LinkedHashMap<>();

        factors.put("Base", ATTACK_FACTOR + "");
        factors.put("Weather", getWeatherFactor() + "");
        factors.put("Steps", getStepFactor(steps) + "");

        return factors;
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
    private int getBaseAttackFactor(final List<Squadron> squadrons) {
        int steps = getSteps(squadrons);
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

    /**
     * Get the number of squadron steps on patrol.
     *
     * @param squadrons The squadrons on patrol.
     * @return The number of steps on patrol.
     */
    private int getSteps(final List<Squadron> squadrons) {
        int totalNumberOfAircraft = squadrons
                .stream()
                .map(Squadron::getAircraftNumber)
                .reduce(0, Integer::sum);

        return SquadronStrength.calculateSteps(totalNumberOfAircraft);
    }
}
