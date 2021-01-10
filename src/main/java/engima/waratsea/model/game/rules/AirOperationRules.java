package engima.waratsea.model.game.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.game.Turn;
import engima.waratsea.model.game.TurnType;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;
import engima.waratsea.utility.Dice;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents the air operation rules for the game.
 *
 * Air operations are:
 *
 *  - Take off
 *  - Landing
 *
 *  This class determines if a given squadron looses a step on take off or landing.
 *
 *  Take of and landing crashes are influenced by the
 *   - weather
 *   - time of day (turn)
 *   - airbase: land, sea or carrier
 */
@Singleton
public class AirOperationRules {

    private final Map<AirbaseType, Integer> landingTypeMap = new HashMap<>();
    private final Map<SquadronAction, Map<WeatherType, Integer>> weatherMap = new HashMap<>();
    private final Map<SquadronAction, Map<TurnType, Integer>> turnTypeMap = new HashMap<>();

    private final Weather weather;
    private final Turn turn;
    private final Dice dice;

    @Inject
    public AirOperationRules(final Weather weather,
                             final Turn turn,
                             final Dice dice) {
        //CHECKSTYLE:OFF: checkstyle:magicnumber

        landingTypeMap.put(AirbaseType.LAND, -2);
        landingTypeMap.put(AirbaseType.SEAPLANE, -1);
        landingTypeMap.put(AirbaseType.CARRIER, -1);
        landingTypeMap.put(AirbaseType.SURFACE_SHIP, -1);

        // The board games values are all negative for these maps. But, making them positive values
        // allows the math to work out more easily.
        Map<WeatherType, Integer> takeOffWeatherMap = new HashMap<>();
        takeOffWeatherMap.put(WeatherType.CLEAR, 0);    // No effect.
        takeOffWeatherMap.put(WeatherType.CLOUDY, 1);   // Slight effect.
        takeOffWeatherMap.put(WeatherType.RAIN, 2);     // Worse effect.
        takeOffWeatherMap.put(WeatherType.SQUALL, 3);   // Worse still.
        takeOffWeatherMap.put(WeatherType.STORM, 100);  // Squadrons are guaranteed to crash on take off.
        takeOffWeatherMap.put(WeatherType.GALE, 100);   // Squadrons are guaranteed to crash on take off.

        Map<WeatherType, Integer> landingWeatherMap = new HashMap<>();
        landingWeatherMap.put(WeatherType.CLEAR, 0);    // No effect.
        landingWeatherMap.put(WeatherType.CLOUDY, 1);   // Slight effect.
        landingWeatherMap.put(WeatherType.RAIN, 2);     // Worse effect.
        landingWeatherMap.put(WeatherType.SQUALL, 3);   // Worse still.
        landingWeatherMap.put(WeatherType.STORM, 4);    // Landing is fairly dangerous
        landingWeatherMap.put(WeatherType.GALE, 100);   // Squadrons are guaranteed to crash on landing.

        weatherMap.put(SquadronAction.TAKE_OFF, takeOffWeatherMap);
        weatherMap.put(SquadronAction.LAND, landingWeatherMap);

        Map<TurnType, Integer> takeOffTurnMap = new HashMap<>();
        takeOffTurnMap.put(TurnType.DAY, 0);
        takeOffTurnMap.put(TurnType.NIGHT, 1);

        Map<TurnType, Integer> landingTurnMap = new HashMap<>();
        landingTurnMap.put(TurnType.DAY, 0);
        landingTurnMap.put(TurnType.NIGHT, 3);

        turnTypeMap.put(SquadronAction.TAKE_OFF, takeOffTurnMap);
        turnTypeMap.put(SquadronAction.LAND, landingTurnMap);

        //CHECKSTYLE:ON: checkstyle:magicnumber

        this.weather = weather;
        this.turn = turn;
        this.dice = dice;
    }

    /**
     * Get the probability that the air operation will fail with a crash.
     *
     * @param airfieldType The airfield type attempting the air operation.
     * @param action The action of the squadron: TAKE_OFF or LAND.
     * @return A percentage as a whole number indicating how likely a crash occurs.
     */
    public int getProbabilityCrash(final AirbaseType airfieldType, final SquadronAction action) {
        int factor = getFactor(airfieldType, action); // The number of hits.
        return dice.probabilityPercentage(factor, 1);
    }

    private int getFactor(final AirbaseType airfieldType, final SquadronAction action) {
        return landingTypeMap.get(airfieldType)
                + weatherMap.get(action).get(weather.getCurrent())
                + turnTypeMap.get(action).get(turn.getTrue());
    }
}
