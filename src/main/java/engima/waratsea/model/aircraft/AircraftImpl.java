package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import lombok.Getter;

/**
 * Represents an aircraft.
 */
public class AircraftImpl implements Aircraft {

    @Getter
    private final AircraftType type;

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     */
    @Inject
    public AircraftImpl(@Assisted final AircraftData data) {
        this.type = data.getType();
    }
}
