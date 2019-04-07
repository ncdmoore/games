package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.aircraft.data.AttackFactorData;
import engima.waratsea.model.aircraft.data.FrameData;
import engima.waratsea.model.aircraft.data.RangeData;
import engima.waratsea.model.game.nation.Nation;
import lombok.Getter;

/**
 * Represents an aircraft.
 */
public class AircraftImpl implements Aircraft {

    @Getter
    private final String name;

    @Getter
    private final AircraftType type;

    @Getter
    private final String designation;

    @Getter
    private final Nation nation;

    @Getter
    private final ServiceType service;

    @Getter
    private final AltitudeType altitude;

    @Getter
    private final LandingType landing;

    @Getter
    private final AttackFactorData naval;

    @Getter
    private final AttackFactorData land;

    @Getter
    private final AttackFactorData air;

    @Getter
    private final RangeData range;

    @Getter
    private final FrameData frame;

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     */
    @Inject
    public AircraftImpl(@Assisted final AircraftData data) {
        this.name = data.getName();
        this.type = data.getType();
        this.designation = data.getDesignation();
        this.nation = data.getNation();
        this.service = data.getService();
        this.altitude = data.getAltitude();
        this.landing = data.getLanding();
        this.naval = data.getNaval();
        this.land = data.getLand();
        this.air = data.getAir();
        this.range = data.getRange();
        this.frame = data.getFrame();
    }
}
