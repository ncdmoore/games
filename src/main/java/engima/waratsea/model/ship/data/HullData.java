package engima.waratsea.model.ship.data;

import engima.waratsea.model.ship.ArmourType;
import lombok.Data;

/**
 * Hull data that is persisted.
 */
@Data
public class HullData {
    private int maxHealth;
    private ArmourType armour;
    private int health;
    private boolean deck;
}
