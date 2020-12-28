package engima.waratsea.model.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.rules.GameRules;
import engima.waratsea.utility.Dice;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class Weather {
    private final GameRules rules;
    private final Dice dice;

    @Getter
    @Setter
    private WeatherType current;

    /**
     * Constructor called by guice.
     *
     * @param rules Game rules.
     * @param dice Dice utility.
     */
    @Inject
    public Weather(final GameRules rules,
                   final Dice dice) {
        this.rules = rules;
        this.dice = dice;
    }

    /**
     * Determine the current turns weather.
     *
     * @param month The game's month.
     **/
    public void determine(final int month) {
        int result = dice.roll6();

        WeatherType newWeather = rules.determineWeather(result, current, month);

        log.info("Die result: '{}', Current weather was: '{}'. Weather now: '{}'", new Object[]{result, current, newWeather});

        current = newWeather;
    }
}
