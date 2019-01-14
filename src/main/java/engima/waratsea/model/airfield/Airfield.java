package engima.waratsea.model.airfield;

import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents airfield's in the game.
 */
public class Airfield {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private int capacity;   //Capacity in steps.


    @Getter
    @Setter
    private String location;
}
