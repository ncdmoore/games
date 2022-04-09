package engima.waratsea.model.base.airfield.data;

import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.mission.data.MissionsData;
import engima.waratsea.model.base.airfield.patrol.data.PatrolsData;
import engima.waratsea.model.base.airfield.squadron.data.SquadronsData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import lombok.Data;

import java.util.List;

/**
 * Represents airfield's data in the game.
 */
@Data
public class AirfieldData {
    private String name;
    private String title;
    private Side side;
    private List<LandingType> landingType;
    private Region region;
    private int maxCapacity;   //Capacity in steps.
    private int capacity;
    private int antiAir;
    private String location;
    private SquadronsData squadronsData; //squadrons stationed at the airfield.
    private MissionsData missionsData;
    private PatrolsData patrolsData;
}
