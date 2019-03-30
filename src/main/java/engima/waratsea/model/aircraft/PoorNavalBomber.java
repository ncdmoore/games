package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;

/**
 * Represents an Italian level bomber.
 */
public class PoorNavalBomber extends AircraftImpl {

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     */
    @Inject
    public PoorNavalBomber(@Assisted final AircraftData data) {
        super(data);
    }
}
