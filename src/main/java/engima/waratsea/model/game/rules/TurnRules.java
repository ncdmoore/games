package engima.waratsea.model.game.rules;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.TurnType;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents the Game turn rules. This controls how twilight turns are treated.
 * This varies per game. Twilight turns may either be treated as a NIGHT or DAY
 * turn depending on the time of year and the type of game.
 */
@Singleton
public class TurnRules {
    private final GameTitle gameTitle;

    private final Map<GameName, Function<Integer, TurnType>> twilightMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param title The game title.
     */
    @Inject
    public TurnRules(final GameTitle title) {
        this.gameTitle = title;

        buildTurnRules();
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
     * Build the twilight turn rules.
     */
    private void buildTurnRules() {
        Function<Integer, TurnType> coralSeaTwilight = (month) -> TurnType.NIGHT;   // Twilight is always night for Coral Sea.
        Function<Integer, TurnType> bombAlleyTwilight = this::determineTwilight;    // Twilight varies for Bomb Alley.

        twilightMap.put(GameName.BOMB_ALLEY, bombAlleyTwilight);
        twilightMap.put(GameName.CORAL_SEA, coralSeaTwilight);
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
}
