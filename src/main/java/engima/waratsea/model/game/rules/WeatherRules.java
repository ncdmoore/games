package engima.waratsea.model.game.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.weather.WeatherType;
import engima.waratsea.utility.Dice;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * This class represents the weather rules for games.
 *
 * For some games the next turns weather may vary based on the time of year.
 */
@Singleton
public class WeatherRules {
    private final GameTitle gameTitle;
    private final Dice dice;

    private final Map<GameName, Map<Integer, BiFunction<WeatherType, Integer, WeatherType>>> weatherMap = new HashMap<>();

    private final Set<Integer> mediterraneanWinterMonths = Set.of(Calendar.JANUARY, Calendar.FEBRUARY, Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER);
    private final Set<Integer> arcticSummerMonths = Set.of(Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER);

    private final int rollThree = 3;

    /**
     * Constructor called by guice.
     *
     * @param title The game title.
     * @param dice The dice utility for rolling dice.
     */
    @Inject
    public WeatherRules(final GameTitle title,
                        final Dice dice) {
        this.gameTitle = title;
        this.dice = dice;

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
        //  rollThree is declared above as it is used in multiple places.
        final int rollFour = 4;
        final int rollFive = 5;
        final int rollSix = 6;

        BiFunction<WeatherType, Integer, WeatherType> worsen = (weather, month) -> weather.worsen();
        BiFunction<WeatherType, Integer, WeatherType> improve = (weather, month) -> weather.improve();
        BiFunction<WeatherType, Integer, WeatherType> same = (weather, month) -> weather.noChange();
        BiFunction<WeatherType, Integer, WeatherType> monthDependent = this::determineWeather;

        BiFunction<WeatherType, Integer, WeatherType> arcticRollOne = this::getArcticWeatherRollOne;
        BiFunction<WeatherType, Integer, WeatherType> arcticRollTwo = this::getArcticWeatherRollTwo;
        BiFunction<WeatherType, Integer, WeatherType> arcticRollThree = this::getArcticWeatherRollThree;
        BiFunction<WeatherType, Integer, WeatherType> arcticRollFour = this::getArcticWeatherRollFour;
        BiFunction<WeatherType, Integer, WeatherType> arcticRollFive = this::getArcticWeatherRollFive;
        BiFunction<WeatherType, Integer, WeatherType> arcticRollSix = this::getArcticWeatherRollSix;

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

        Map<Integer, BiFunction<WeatherType, Integer, WeatherType>> arcticConvoyWeather = new HashMap<>();

        arcticConvoyWeather.put(rollOne, arcticRollOne);
        arcticConvoyWeather.put(rollTwo, arcticRollTwo);
        arcticConvoyWeather.put(rollThree, arcticRollThree);
        arcticConvoyWeather.put(rollFour, arcticRollFour);
        arcticConvoyWeather.put(rollFive, arcticRollFive);
        arcticConvoyWeather.put(rollSix, arcticRollSix);

        weatherMap.put(GameName.BOMB_ALLEY, bombAlleyWeather);
        weatherMap.put(GameName.CORAL_SEA, coralSeaWeather);
        weatherMap.put(GameName.ARCTIC_CONVOY, arcticConvoyWeather);
    }

    /**
     * A method that determines the weather based on the month.
     *
     * @param current The current weather.
     * @param month The current game month.
     * @return The new weather.
     */
    private WeatherType determineWeather(final WeatherType current, final int month) {
        return mediterraneanWinterMonths.contains(month) ? current.worsen() : current.noChange();
    }

    private WeatherType getArcticWeatherRollOne(final WeatherType current, final int month) {
        return arcticSummerMonths.contains(month) ? getArcticSummerRollOne(current) : WeatherType.CLEAR;
    }

    private WeatherType getArcticWeatherRollTwo(final WeatherType current, final int month) {
        return arcticSummerMonths.contains(month) ? getArcticSummerRollTwo(current) : getArcticWinterRollTwo(current);
    }

    private WeatherType getArcticWeatherRollThree(final WeatherType current, final int month) {
        return arcticSummerMonths.contains(month) ? getArcticSummerRollThree(current) : getArcticWinterRollThree(current);
    }

    private WeatherType getArcticWeatherRollFour(final WeatherType current, final int month) {
        return arcticSummerMonths.contains(month) ? getArcticSummerRollFour(current) : getArcticWinterRollFour(current);
    }

    private WeatherType getArcticWeatherRollFive(final WeatherType current, final int month) {
        return arcticSummerMonths.contains(month) ? getArcticSummerRollFive(current) : getArcticWinterRollFive(current);
    }

    private WeatherType getArcticWeatherRollSix(final WeatherType current, final int month) {
        return arcticSummerMonths.contains(month) ? current.worsen() : getArcticWinterRollSix(current);
    }

    private WeatherType getArcticSummerRollOne(final WeatherType current) {
        switch (current) {  // Verified correct with game manual.
            case CLEAR:
            case CLOUDY:
                return WeatherType.CLEAR;
            default:
                return WeatherType.CLOUDY;
        }
    }

    private WeatherType getArcticSummerRollTwo(final WeatherType current) {
        switch (current) {  // Verified correct with game manual.
            case CLEAR:
            case CLOUDY:
                return WeatherType.CLEAR;
            case RAIN:
                return WeatherType.CLOUDY;
            default:
                return WeatherType.RAIN;
        }
    }

    private WeatherType getArcticSummerRollThree(final WeatherType current) {
        switch (current) {  // Verified correct with game manual.
            case CLEAR:
            case CLOUDY:
                return WeatherType.CLOUDY;
            case STORM:
                return WeatherType.SQUALL;
            default:
                return WeatherType.RAIN;
        }
    }

    private WeatherType getArcticSummerRollFour(final WeatherType current) {
        switch (current) {  // Verified correct with game manual.
            case CLEAR:
            case CLOUDY:
                return WeatherType.CLOUDY;
            case RAIN:
                return WeatherType.RAIN;
            default:
                return WeatherType.SQUALL;
        }
    }

    private WeatherType getArcticSummerRollFive(final WeatherType current) {
        switch (current) {  // Verified correct with game manaul.
            case CLEAR:
                return WeatherType.CLOUDY;
            case CLOUDY:
                return WeatherType.RAIN;
            case RAIN:
            case SQUALL:
                return WeatherType.SQUALL;
            default:
                return WeatherType.STORM;
        }
    }

    private WeatherType getArcticWinterRollTwo(final WeatherType current) {
        switch (current) {
            case CLEAR:
            case CLOUDY:
                return WeatherType.CLEAR;
            default:
                return WeatherType.CLOUDY;
        }
    }

    private WeatherType getArcticWinterRollThree(final WeatherType current) {
        switch (current) {
            case CLEAR:
                return WeatherType.CLEAR;
            case CLOUDY:
                return WeatherType.CLOUDY;
            default:
                return WeatherType.RAIN;
        }
    }

    private WeatherType getArcticWinterRollFour(final WeatherType current) {
        switch (current) {
            case CLEAR:
                return WeatherType.CLOUDY;
            case CLOUDY:
            case RAIN:
                return WeatherType.RAIN;
            default:
                return WeatherType.SQUALL;
        }
    }

    private WeatherType getArcticWinterRollFive(final WeatherType current) {
        switch (current) {
            case CLEAR:
            case CLOUDY:
                return WeatherType.RAIN;
            case RAIN:
            case SQUALL:
                return WeatherType.SQUALL;
            default:
                return WeatherType.STORM;
        }
    }

    private WeatherType getArcticWinterRollSix(final WeatherType current) {
        switch (current) {
            case CLEAR:
            case CLOUDY:
            case RAIN:
                return WeatherType.SQUALL;
            case SQUALL:
                return WeatherType.STORM;
            case STORM:
                int die = dice.roll();
                return die > rollThree ? WeatherType.GALE : WeatherType.STORM;
            default:
                return WeatherType.GALE;
        }
    }
}
