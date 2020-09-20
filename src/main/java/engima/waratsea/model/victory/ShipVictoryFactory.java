package engima.waratsea.model.victory;

import com.google.inject.name.Named;

/**
 * Factory used by guice to create ship victory conditions.
 *
 * @param <E> The type of game event. This is the type of event that triggers the victory condition.
 * @param <D> The type of victory data. This is the data class that is used to persist the victory condition.
 */
public interface ShipVictoryFactory<E, D> {

    /**
     * Creates the ship victory condition.
     *
     * @param data Ship victory data from a JSON file.
     * @return The ship victory object.
     */
    @Named("ship")
    VictoryCondition<E, D> createShip(D data);


    /**
     * Creates the ship victory condition.
     *
     * @param data Ship victory data from a JSON file.
     * @return The ship victory object.
     */
    @Named("required")
    VictoryCondition<E, D> createRequired(D data);

}
