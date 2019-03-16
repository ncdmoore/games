package engima.waratsea.model.airfield.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents airfield's data in the game.
 */
public class AirfieldData {

    @Getter
    @Setter
    private String name;

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
