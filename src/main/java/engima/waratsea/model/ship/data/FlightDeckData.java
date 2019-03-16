package engima.waratsea.model.ship.data;

import engima.waratsea.model.ship.ArmourType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Flight deck that is persisted.
 */
public class FlightDeckData {
    @Getter
    @Setter
    private ArmourType armour;

    // The maximum capacity. This is fixed and never changes.
    @Getter
    @Setter
    private  List<Integer> capacityList;    // Capacity in steps.

    // The current flight deck health. The health is an index into the capacity list
    @Getter
    @Setter
    private int health;
}
