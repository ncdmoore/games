package engima.waratsea.model.game.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.weather.WeatherType;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * This class represents the weather rules for games.
 *
 * For some games the next turns weather may vary based on the time of year.
 */
@Singleton
public class WeatherRules {
    private final GameTitle gameTitle;

    private final Map<GameName, Map<Integer, BiFunction<WeatherType, Integer, WeatherType>>> weatherMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param title The game title.
     */
    @Inject
    public WeatherRules(final GameTitle title) {
        this.gameTitle = title;

        buildWeatherRules();
    }
    /**
     * Execute the weather rule. Determine the turn's weather.
     *
     * @param result The dice result.
     * @param current The current weather.
     * @param month The current month.
     * @return The new weather.
     */
    public WeatherType determineWeather(final int result, final WeatherType current, final int month) {
        return weatherMap
                .get(gameTitle.getName())
                .get(result)
                .apply(current, month);
    }
    /**
     * Build the weather rules.
     */
    private void buildWeatherRules() {
        final int rollOne = 1;
        final int rollTwo = 2;
        final int rollThree = 3;
        final int rollFour = 4;
        final int rollFive = 5;
        final int rollSix = 6;

        BiFunction<WeatherType, Integer, WeatherType> worsen = (weather, month) -> weather.worsen();
        BiFunction<WeatherType, Integer, WeatherType> improve = (weather, month) -> weather.improve();
        BiFunction<WeatherType, Integer, WeatherType> same = (weather, month) -> weather.noChange();
        BiFunction<WeatherType, Integer, WeatherType> monthDependent = this::determineWeather;

        Map<Integer, BiFunction<WeatherType, Integer, WeatherType>> bombAlleyWeather = new HashMap<>();

        bombAlleyWeather.put(rollOne, improve);
        bombAlleyWeather.put(rollTwo, improve);
        bombAlleyWeather.put(rollThree, same);
        bombAlleyWeather.put(rollFour, same);
        bombAlleyWeather.put(rollFive, monthDependent);
        bombAlleyWeather.put(rollSix, worsen);

        Map<Integer, BiFunction<WeatherType, Integer, WeatherType>> coralSeaWeather = new HashMap<>();

        coralSeaWeather.put(rollOne, improve);
        coralSeaWeather.put(rollTwo, improve);
        coralSeaWeather.put(rollThree, same);
        coralSeaWeather.put(rollFour, same);
        coralSeaWeather.put(rollFive, same);
        coralSeaWeather.put(rollSix, worsen);

        weatherMap.put(GameName.BOMB_ALLEY, bombAlleyWeather);
        weatherMap.put(GameName.CORAL_SEA, coralSeaWeather);
    }

    /**
     * A method that determines the weather based on the month.
     *
     * @param current The current weather.
     * @param month The current game month.
     * @return The new weather.
     */
    private WeatherType determineWeather(final WeatherType current, final int month) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                return current.worsen();
            default:
                return current.noChange();
        }
    }
}
