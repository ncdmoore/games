package engima.waratsea.model.aircraft;

import com.google.inject.name.Named;
import engima.waratsea.model.aircraft.data.AircraftData;

/**
 * Creates aircraft.
 */
public interface AircraftFactory {
    /**
     * Creates aircraft.
     *
     * @param data The aircraft's data.
     * @return The aircraft.
     */
    @Named("aircraft")
    Aircraft createAircraft(AircraftData data);

    /**
     * Create a fighter aircraft.
     *
      * @param data The aircraft's data.
     * @return The fighter aircraft.
     */
    @Named("fighter")
    Aircraft createFighter(AircraftData data);

    /**
     * Creates an Italian bomber.
     *
     * @param data The aircraft's data.
     * @return The poor naval bomber aircraft.
     */
    @Named("poorNaval")
    Aircraft createPoorNavalBomber(AircraftData data);
}
