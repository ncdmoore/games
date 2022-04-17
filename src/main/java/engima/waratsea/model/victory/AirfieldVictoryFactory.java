package engima.waratsea.model.victory;

import com.google.inject.name.Named;

/**
 * Creates airfield victory conditions.
 *
 * @param <E> The type of game event. This is the type of event that triggers the victory condition.
 * @param <D> The type of victory data. This is the data class that is used to persist the victory condition.
 */
public interface AirfieldVictoryFactory<E, D> {
    /**
     * Create the airfield's victory condition.
     *
     * @param data Airfield victory data from a JSON file.
     * @return The airfield victory condition object.
     */
    @Named("airfield")
    VictoryCondition<E, D> createAirfield(D data);
}
