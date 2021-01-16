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
    @Named("bomber")
    Aircraft createBomber(AircraftData data);

    /**
     * Create a fighter aircraft.
     *
      * @param data The aircraft's data.
     * @return The fighter aircraft.
     */
    @Named("fighter")
    Aircraft createFighter(AircraftData data);

    /**
     * Create a reconnaissance aircraft.
     *
     * @param data The aircraft's data.
     * @return The reconnaissance aircraft.
     */
    @Named("recon")
    Aircraft createRecon(AircraftData data);
}
