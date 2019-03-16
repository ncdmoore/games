package engima.waratsea.model.ship.data;

import engima.waratsea.model.ship.ArmourType;
import lombok.Getter;
import lombok.Setter;

/**
 * Hull data that is persisted.
 */
public class HullData {
    @Getter
    @Setter
    private int maxHealth;

    @Getter
    @Setter
    private ArmourType armour;

    @Getter
    @Setter
    private int health;

    @Getter
    @Setter
    private boolean deck;
}
