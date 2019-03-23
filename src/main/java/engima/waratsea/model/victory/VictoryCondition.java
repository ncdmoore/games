package engima.waratsea.model.victory;

import engima.waratsea.model.PersistentData;

/**
 * A victory condition.
 *
 * @param <E> The corresponding event type. This is the type of event that triggers the victory condition.
 * @param <D> The JSON data type. This is the data class that is used to persist the victory condition.
 */
public interface VictoryCondition<E, D> extends PersistentData<D> {

    /**
     * Indicates if the victory condition is met.
     *
     * @return True if the condition is met.
     */
    boolean isRequirementMet();

    /**
     * Get the points awarded for this condition.
     *
     * @param event The fired event that triggers the victory award.
     * @return The awarded victory points.
     */
    int getPoints(E event);

    /**
     * Determine if the fired event matches this victory condition trigger.
     *
     * @param event The fired event.
     * @return True if the event matches. False otherwise.
     */
    boolean match(E event);

    /**
     * Get the victory condition data that is read and written to a JSON file.
     *
     * @return The persistent victory condition data.
     */
    D getData();

}
