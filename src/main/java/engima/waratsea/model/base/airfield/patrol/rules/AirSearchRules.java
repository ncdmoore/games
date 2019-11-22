package engima.waratsea.model.base.airfield.patrol.rules;

import com.google.inject.Inject;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.utility.Dice;

import java.util.List;

/**
 * A singleton that calculates air search results.
 *
 */
public class AirSearchRules implements AirRules {
    private final SearchRules searchRules;

    /**
     * Constructor called by guice.
     *
     * @param weather The current weather.
     * @param dice A dice utility.
     */
    @Inject
    public AirSearchRules(final Weather weather, final Dice dice) {
        searchRules = new SearchRules(AssetType.SHIP, weather, dice);
    }

    /**
     * The base search success including weather conditions.
     *
     * @param distance The distance to the target from the patrol base.
     * @param squadrons The squadrons that are within range of the target.
     * @return A percentage representing the chance of successfully finding a task force.
     */
    @Override
    public int getBaseSearchSuccess(final int distance, final List<Squadron> squadrons) {
        return searchRules.getBaseSearchSuccess(distance, squadrons);
    }

    /**
     * The base search success excluding weather conditions.
     *
     * @param distance The distance to the target from the patrol base.
     * @param squadrons The squadrons that are within range of the target.
     * @return A percentage representing the chance of successfully finding a task force.
     */
    @Override
    public int getBaseSearchSuccessNoWeather(final int distance, final List<Squadron> squadrons) {
        return searchRules.getBaseSearchSuccessNoWeather(distance, squadrons);
    }

    /**
     * NA.
     * @param distance The distance to the target from the patrol base.
     * @param squadrons The squadron that are within range of the target.
     * @return 0.
     */
    @Override
    public int getBaseAttackSuccess(final int distance, final List<Squadron> squadrons) {
        return 0;
    }
}
