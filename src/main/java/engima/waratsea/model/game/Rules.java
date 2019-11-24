package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.weather.WeatherType;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Singleton
public class Rules {

    private final GameTitle gameTitle;

    private final Map<PatrolType, Map<GameName, Function<Squadron, Boolean>>> patrolMap = new HashMap<>();

    private final Map<GameName, Function<Integer, TurnType>> twilightMap = new HashMap<>();

    private final Map<GameName, Map<Integer, BiFunction<WeatherType, Integer, WeatherType>>> weatherMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param title The game title.
     */
    @Inject
    public Rules(final GameTitle title) {
        this.gameTitle = title;

        buildTurnRules();
        buildWeatherRules();
        buildPatrolRules();
    }

    /**
     * Execute the Twilight turn rule. Determine the true type of a twilight turn. It will either be
     * DAY or NIGHT.
     *
     * @param month The game month.
     * @return DAY or NIGHT.
     */
    public TurnType getTwilightTurnType(final int month) {
        return twilightMap
                .get(gameTitle.getName())
                .apply(month);
    }

    /**
     * Execute the ASW filter rule. Determine if the given squadron can perform an ASW patrol.
     *
     * @param patrolType The patrol type.
     * @param squadron Any given squadron.
     * @return True if the given squadron can perform ASW. False otherwise.
     */
    public boolean patrolFilter(final PatrolType patrolType, final Squadron squadron) {
        return patrolMap
                .get(patrolType)
                .get(gameTitle.getName())
                .apply(squadron);
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
     * Build the twilight turn rules.
     */
    private void buildTurnRules() {
        Function<Integer, TurnType> coralSeaTwilight = (month) -> TurnType.NIGHT;
        Function<Integer, TurnType> bombAlleyTwlight = this::determineTwilight;

        twilightMap.put(GameName.BOMB_ALLEY, bombAlleyTwlight);
        twilightMap.put(GameName.CORAL_SEA, coralSeaTwilight);
    }

    /**
     * Build the patrol rules.
     */
    private void buildPatrolRules() {
        // In bomb Alley land based planes may not do ASW patrols. Note, any carrier based plane may do an ASW patrol.
        Function<Squadron, Boolean> bombAlleyAswFilter = (squadron) -> squadron.getLandingType() != LandingType.LAND;
        Function<Squadron, Boolean> coralSeaAswFilter = (squadron) -> true;

        Function<Squadron, Boolean> capFilter = (squadron) -> squadron.getType() == AircraftType.FIGHTER;

        Function<Squadron, Boolean> searchFilter = (squadron -> true);

        Map<GameName, Function<Squadron, Boolean>> aswFilterMap = new HashMap<>();
        aswFilterMap.put(GameName.BOMB_ALLEY, bombAlleyAswFilter);
        aswFilterMap.put(GameName.CORAL_SEA,  coralSeaAswFilter);

        Map<GameName, Function<Squadron, Boolean>> capFilterMap = new HashMap<>();
        capFilterMap.put(GameName.BOMB_ALLEY, capFilter);
        capFilterMap.put(GameName.CORAL_SEA, capFilter);

        Map<GameName, Function<Squadron, Boolean>> searchFilterMap = new HashMap<>();
        searchFilterMap.put(GameName.BOMB_ALLEY, searchFilter);
        searchFilterMap.put(GameName.CORAL_SEA, searchFilter);

        patrolMap.put(PatrolType.ASW, aswFilterMap);
        patrolMap.put(PatrolType.CAP, capFilterMap);
        patrolMap.put(PatrolType.SEARCH, searchFilterMap);
    }

    /**
     * A method that determines how twilight turns are treated.
     *
     * @param month The current game month.
     * @return How a twilight turn is treated.
     */
    private TurnType determineTwilight(final int month) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                return TurnType.NIGHT;
            default:
                return TurnType.DAY;
        }
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
