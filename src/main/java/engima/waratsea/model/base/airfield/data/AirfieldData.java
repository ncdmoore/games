package engima.waratsea.model.base.airfield.data;

import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents airfield's data in the game.
 */
public class AirfieldData {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private List<LandingType> landingType;

    @Getter
    @Setter
    private Region region;

    @Getter
    @Setter
    private int maxCapacity;   //Capacity in steps.

    @Getter
    @Setter
    private int capacity;

    @Getter
    @Setter
    private int antiAir;

    @Getter
    @Setter
    private String location;

}
