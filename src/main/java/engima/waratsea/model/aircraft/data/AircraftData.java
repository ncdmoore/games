package engima.waratsea.model.aircraft.data;

import engima.waratsea.model.aircraft.AircraftId;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.AltitudeType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.aircraft.ServiceType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronConfig;
import lombok.Data;

import java.util.Set;

/**
 * Represents aircraft data.
 */
@Data
public class AircraftData {
    private AircraftId aircraftId;
    private String name;
    private AircraftType type;
    private Nation nationality;
    private ServiceType service;
    private String designation;
    private AltitudeType altitude;
    private LandingType landing;
    private LandingType takeoff;
    private AttackData navalWarship;
    private AttackData navalTransport;
    private AttackData land;
    private AttackData air;
    private PerformanceData performance;
    private FrameData frame;
    private Set<SquadronConfig> config;
}
