package engima.waratsea.model.airfield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.airfield.data.AirfieldData;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents airfield's in the game.
 */
public class Airfield {

    @Getter
    private final String name;

    @Getter
    private Side side;

    @Getter
    private final int maxCapacity;   //Capacity in steps.

    @Getter
    @Setter
    private int capacity;            //Capacity in steps.

    @Getter
    private String location;

    /**
     * Constructor called by guice.
     * @param side The side of the airfield ALLIES or AXIS.
     * @param data The airfield data read in from a JSON file.
     */
    @Inject
    public Airfield(@Assisted final Side side,
                    @Assisted final AirfieldData data) {
        this.side = side;
        name = data.getName();
        maxCapacity = data.getCapacity();
        capacity = maxCapacity;
    }
}
