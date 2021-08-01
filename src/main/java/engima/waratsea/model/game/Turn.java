package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.data.TurnData;
import engima.waratsea.model.game.rules.GameRules;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.weather.Weather;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static engima.waratsea.model.game.TurnIndex.DAY_1;
import static engima.waratsea.model.game.TurnType.DAY;
import static engima.waratsea.model.game.TurnType.TWILIGHT;
import static engima.waratsea.model.game.TurnType.NIGHT;

/**
 * Represents the game's turn.
 */
@Singleton
@Slf4j
public class Turn {

    private final GameRules rules;   //Game specific rules.

    @Getter
    private int number;              //One day equals 6 turns.

    @Getter
    private TurnType type;

    @Getter
    private TurnIndex index;         //Used to determine the type of turn.

    private Date date;

    // A day consists of 3 day turns, 1 twilight turn and 2 night turns.
    private static final List<TurnType> DAY_TURNS = new ArrayList<>(Arrays.asList(DAY, DAY, DAY, TWILIGHT, NIGHT, NIGHT));

    private final Weather weather;

    /**
     * Constructor called by guice.
     *
     * @param rules The game rules.
     * @param weather The game weather.
     */
    @Inject
    public Turn(final GameRules rules,
                final Weather weather) {
        this.rules = rules;

        this.number = 1;
        this.index = DAY_1;

        this.type = DAY_TURNS.get(index.getValue());

        this.weather = weather;
    }

    /**
     * Initialize the turn from the persistent turn data.
     *
     * @param data The persistent turn data.
     */
    public void init(final TurnData data) {
        number = data.getTurn();
        index = data.getIndex();
        date = data.getDate();
        type = DAY_TURNS.get(index.getValue());
    }

    /**
     * Get the persistent turn data. The data that is read and written to a JSON file.
     *
     * @return The persistent turn data.
     */
    public TurnData getData() {
        TurnData data = new TurnData();
        data.setTurn(number);
        data.setIndex(index);
        data.setDate(date);
        return data;
    }

    /**
     * Get the game's current date.
     *
     * @return The game's current date.
     */
    public Date getDate() {
        return new Date(date.getTime());
    }


    /**
     * Set the game's current date.
     *
     * @param scenario The selected scenario
     */
    public void start(final Scenario scenario) {
        date = new Date(scenario.getDate().getTime());

        index = Optional
                .ofNullable(scenario.getTurnIndex())
                .orElse(DAY_1);

        type = DAY_TURNS.get(index.getValue());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
        String dateString = simpleDateFormat.format(date);

        log.info("Starting Date: '{}', Turn:  {}, Type: {}", new Object[]{dateString, number, type});

        startWeather(scenario);
    }

    /**
     * Advance the game turn.
     */
    public void next() {
        number++;

        index = index.next();
        if (index == DAY_1) {
            incrementDay();
        }

        type = DAY_TURNS.get(index.getValue());

        weather.determine(getMonth());
    }

    /**
     * Get the true turn type.
     *
     * @param month The game month.
     * @return The game turn's true type. This is needed for twilight turns which
     * are treated as either day or night turns. Depending on the game type and the
     * time of year (month).
     */
    public TurnType getTrue(final int month) {
        return type.getTrue(rules, month);
    }

    /**
     * Get the game's current true turn type.
     *
     * @return The game turn's true type. This is needed for twilight turns which
     * are treated as either day or night turns. Depending on the game type and the
     * time of year (month).
     */
    public TurnType getTrue() {
        return type.getTrue(rules, getMonth());
    }

    /**
     * Get the game's current month.
     *
     * @return The game's current month 1-12.
     */
    private int getMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }

    /**
     * Increment the date.
     */
    private void incrementDay() {
        date = Optional.ofNullable(date).map(d -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return calendar.getTime();
        }).orElse(null);
    }

    /**
     * Roll for the initial weather.
     *
     * @param scenario The selected game scenario.
     */
    private void startWeather(final Scenario scenario) {
        weather.setCurrent(scenario.getWeather());
        weather.determine(getMonth());
    }
}
