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
     * Creates an Italian bomber.
     *
     * @param data The aircraft's data.
     * @return The aircraft.
     */
    @Named("poorNaval")
    Aircraft createPoorNavalBomber(AircraftData data);
}
