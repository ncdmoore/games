package engima.waratsea.model.ships.data;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.ships.ShipId;
import engima.waratsea.model.ships.ShipType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the ship's data that is read and written to JSON files.
 */
public class ShipData {

    @Getter
    @Setter
    private transient ShipId shipId;

    @Getter
    @Setter
    private ShipType type;

    @Getter
    @Setter
    private String shipClass;

    @Getter
    @Setter
    private Nation nationality;

    @Getter
    @Setter
    private WeaponData weapons;

    @Getter
    @Setter
    private ArmourData armour;

    @Getter
    @Setter
    private MovementData movement;

    @Getter
    @Setter
    private int hull;

    @Getter
    @Setter
    private int fuel;                                             //Fuel is n times the value in the board game.

    @Getter
    @Setter
    private int cargo;                                            //Cargo is 3 times the value in the board game. This is done to avoid fractions.

    @Getter
    @Setter
    private List<Integer> flightDeck;

}
