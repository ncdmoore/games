package engima.waratsea.model.base.airfield.patrol.rules;

import com.google.inject.Inject;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.utility.Dice;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A singleton that calculates air search results.
 *
 */
public class PatrolAirSearchRules implements PatrolAirRules {
    private final PatrolSearchRules searchRules;

    /**
     * Constructor called by guice.
     *
     * @param weather The current weather.
     * @param dice A dice utility.
     */
    @Inject
    public PatrolAirSearchRules(final Weather weather, final Dice dice) {
        searchRules = new PatrolSearchRules(AssetType.SHIP, weather, dice);
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
     * The base search success factors.
     *
     * @param distance The distance to the target from the patrol base.
     * @param squadrons The squadrons that are within range of the target.
     * @return A map of factor name to factor value.
     */
    @Override
    public Map<String, String> getBaseSearchFactors(final int distance, final List<Squadron> squadrons) {
        return searchRules.getBaseFactors(distance, squadrons);
    }

    /**
     * The base attack success factors.
     *
     * @param distance  The distance to the target from the patrol base.
     * @param squadrons The squadrons that are within range of the target.
     * @return A map of factor name to factor value.
     */
    @Override
    public Map<String, String> getBaseAttackFactors(final int distance, final List<Squadron> squadrons) {
        return Collections.emptyMap();
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

    /**
     * Determine if the current weather affects a patrol.
     *
     * @return True if the weather affects the patrol. False otherwise.
     */
    @Override
    public boolean isAffectedByWeather() {
        return searchRules.isAffectedByWeather();
    }
}
