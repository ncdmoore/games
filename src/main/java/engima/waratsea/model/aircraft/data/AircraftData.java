package engima.waratsea.model.aircraft.data;

import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.AltitudeType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.aircraft.ServiceType;
import engima.waratsea.model.game.nation.Nation;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents aircraft data.
 */
public class AircraftData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private AircraftType type;

    @Getter
    @Setter
    private Nation nation;

    @Getter
    @Setter
    private ServiceType service;

    @Getter
    @Setter
    private String designation;

    @Getter
    @Setter
    private AltitudeType altitude;

    @Getter
    @Setter
    private LandingType landing;

    @Getter
    @Setter
    private AttackFactorData naval;

    @Getter
    @Setter
    private AttackFactorData land;

    @Getter
    @Setter
    private AttackFactorData air;

    @Getter
    @Setter
    private RangeData range;

    @Getter
    @Setter
    private FrameData frame;
}
