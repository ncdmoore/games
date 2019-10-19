package engima.waratsea.model.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Rules;
import engima.waratsea.model.game.Turn;
import engima.waratsea.utility.Dice;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class Weather {
    private final Rules rules;
    private final Turn turn;
    private final Dice dice;

    @Getter
    @Setter
    private WeatherType current;

    /**
     * Constructor called by guice.
     *
     * @param rules Game rules.
     * @param turn The game turn;
     * @param dice Dice utility.
     */
    @Inject
    public Weather(final Rules rules,
                   final Turn turn,
                   final Dice dice) {
        this.rules = rules;
        this.turn = turn;
        this.dice = dice;
    }

    /**
     * Determine the current turns weather.
     **/
    public void determine() {
        int result = dice.roll6();

        WeatherType newWeather = rules.determineWeather(result, current, turn.getMonth());

        log.info("Die result: '{}', Current weather was: '{}'. Weather now: '{}'", new Object[]{result, current, newWeather});

        current = newWeather;
    }
}
