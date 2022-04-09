package engima.waratsea.model.ship.data;

import engima.waratsea.model.ship.ArmourType;
import lombok.Data;

import java.util.List;

/**
 * Flight deck that is persisted.
 */
@Data
public class FlightDeckData {
    private ArmourType armour;

    // The maximum capacity. This is fixed and never changes.
    private  List<Integer> capacityList;    // Capacity in steps.

    // The current flight deck health. The health is an index into the capacity list
    private int health;
}
