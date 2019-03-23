package engima.waratsea.model.victory;

import com.google.inject.name.Named;

/**
 * Creates squadron victory conditions.
 *
 * @param <E> The type of game event. This is the type of event that triggers the victory condition.
 * @param <D> The type of victory data. This is the data class that is used to persist the victory condition.
 */
public interface SquadronVictoryFactory<E, D> {
    /**
     * Create's the squadron victory condition.
     *
     * @param data squadron victory data from a JSON file.
     * @return The squadron victory condition object.
     */
    @Named("squadron")
    VictoryCondition<E, D> create(D data);
}
