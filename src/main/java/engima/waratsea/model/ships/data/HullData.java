package engima.waratsea.model.ships.data;

import engima.waratsea.model.ships.ArmourType;
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
