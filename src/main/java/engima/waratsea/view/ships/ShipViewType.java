package engima.waratsea.view.ships;

import engima.waratsea.model.ships.ShipType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * How the GUI classifies ship types.
 */
public enum ShipViewType {
    AIRCRAFT_CARRIER("Aircraft Carrier"),
    BATTLESHIP("Battle Ship"),
    CRUISER("Cruiser"),
    DESTROYER("Destroyer"),
    DESTROYER_ESCORT("Destroyer Escort"),
    PATROL("Patrol"),
    TRANSPORT("Transport");

    @Getter
    private String value;

    private static Map<ShipType, ShipViewType> viewTypeMap = new HashMap<>();

    static {
        viewTypeMap.put(ShipType.AIRCRAFT_CARRIER, AIRCRAFT_CARRIER);
        viewTypeMap.put(ShipType.SEAPLANE_CARRIER, AIRCRAFT_CARRIER);
        viewTypeMap.put(ShipType.BATTLECRUISER, BATTLESHIP);
        viewTypeMap.put(ShipType.BATTLESHIP, BATTLESHIP);
        viewTypeMap.put(ShipType.CRUISER, CRUISER);
        viewTypeMap.put(ShipType.DESTROYER, DESTROYER);
        viewTypeMap.put(ShipType.DESTROYER_ESCORT, DESTROYER_ESCORT);
        viewTypeMap.put(ShipType.MINESWEEPER, PATROL);
        viewTypeMap.put(ShipType.MINELAYER, PATROL);
        viewTypeMap.put(ShipType.SLOOP, PATROL);
        viewTypeMap.put(ShipType.OILER, TRANSPORT);
        viewTypeMap.put(ShipType.TRANSPORT, TRANSPORT);
    }

    /**
     * Constructor.
     * @param value The string value of the enum.
     */
    ShipViewType(final String value) {
        this.value = value;
    }

    /**
     * Determine the view ship type that corresponds to the given model ship type. Several model's of ships
     * are classified into the same view type in the GUI.
     * @param type The model ship type.
     * @return The view ship type.
     */
    public static ShipViewType get(final ShipType type) {
        return viewTypeMap.get(type);
    }
}
