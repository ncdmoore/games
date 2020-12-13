package engima.waratsea.model.base.airfield.patrol.rules;

import engima.waratsea.model.squadron.Squadron;

import java.util.List;
import java.util.Map;

public interface PatrolAirRules {

    /**
     * Get the base search success including weather factors.
     *
     * @param distance The distance to the target from the air base.
     * @param squadrons The number of squadrons that are in range of the target.
     * @return An integer representing the percentage chance of spotting the target.
     */
    int getBaseSearchSuccess(int distance, List<Squadron> squadrons);

    /**
     * Get the base search success excluding weather factors.
     *
     * @param distance The distance to the target from the air base.
     * @param squadrons The number of squadrons that are in range of the target.
     * @return An integer representing the percentage chance of spotting the target.
     */
    int getBaseSearchSuccessNoWeather(int distance, List<Squadron> squadrons);

    /**
     * Get the base attack success including weather affects.
     *
     * @param distance The distance to the target from the air base.
     * @param squadrons The number of squadrons that are in range of the target.
     * @return An integer representing the percentage chance of successfully attacking the target.
     */
    int getBaseAttackSuccess(int distance, List<Squadron> squadrons);

    /**
     * Determine if the current weather affects a patrol.
     *
     * @return True if the weather affects the patrol. False otherwise.
     */
    boolean isAffectedByWeather();

    /**
     * The base search success factors.
     *
     * @param distance The distance to the target from the patrol base.
     * @param squadrons The squadrons that are within range of the target.
     * @return A map of factor name to factor value.
     */
    Map<String, String> getBaseSearchFactors(int distance, List<Squadron> squadrons);

    /**
     * The base attack success factors.
     *
     * @param distance The distance to the target from the patrol base.
     * @param squadrons The squadrons that are within range of the target.
     * @return A map of factor name to factor value.
     */
    Map<String, String> getBaseAttackFactors(int distance, List<Squadron> squadrons);
}
