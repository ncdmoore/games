package engima.waratsea.model.weather;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Phase;
import engima.waratsea.model.game.Phases;
import engima.waratsea.model.game.Turn;
import engima.waratsea.model.game.rules.GameRules;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.Dice;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class Weather {
    private final GameRules rules;
    private final Provider<Turn> turnProvider;
    private final Dice dice;

    @Getter
    @Setter
    private WeatherType current;

    /**
     * Constructor called by guice.
     *
     * @param rules Game rules.
     * @param phases The game turn phases.
     * @param turnProvider Provides the game turn.
     * @param dice Dice utility.
     */
    @Inject
    public Weather(final GameRules rules,
                   final Phases phases,
                   final Provider<Turn> turnProvider,
                   final Dice dice) {
        this.rules = rules;
        this.turnProvider = turnProvider;
        this.dice = dice;

        phases.register(Phase.WEATHER, this::determine);
    }

    /**
     * Initialize the starting weather from the scenario.
     *
     * @param scenario The game scenario.
     */
    public void start(final Scenario scenario) {
        setCurrent(scenario.getWeather());
        determine();
    }

    /**
     * Determine the current turns weather.
     **/
    private void determine() {
        int month = turnProvider
                .get()
                .getMonth();

        calculate(month);
    }

    private void calculate(final int month) {
        int result = dice.roll();

        WeatherType newWeather = rules.determineWeather(result, current, month);

        log.info("Die result: '{}', Current weather was: '{}'. Weather now: '{}'", new Object[]{result, current, newWeather});

        current = newWeather;
    }
}
