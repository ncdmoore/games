package engima.waratsea.view.ship;

import engima.waratsea.model.ship.ShipType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * How the GUI classifies ship types.
 */
public enum ShipViewType  {
    AIRCRAFT_CARRIER("Aircraft Carrier"),
    BATTLESHIP("Battleship"),
    CRUISER("Cruiser"),
    DESTROYER("Destroyer"),
    DESTROYER_ESCORT("Destroyer Escort"),
    PATROL("Patrol"),
    TRANSPORT("Transport");

    @Getter
    private final String value;

    private static final Map<ShipType, ShipViewType> VIEW_TYPE_MAP = new HashMap<>();

    static {
        VIEW_TYPE_MAP.put(ShipType.AIRCRAFT_CARRIER, AIRCRAFT_CARRIER);
        VIEW_TYPE_MAP.put(ShipType.SEAPLANE_CARRIER, AIRCRAFT_CARRIER);
        VIEW_TYPE_MAP.put(ShipType.BATTLECRUISER, BATTLESHIP);
        VIEW_TYPE_MAP.put(ShipType.BATTLESHIP, BATTLESHIP);
        VIEW_TYPE_MAP.put(ShipType.CRUISER, CRUISER);
        VIEW_TYPE_MAP.put(ShipType.DESTROYER, DESTROYER);
        VIEW_TYPE_MAP.put(ShipType.DESTROYER_ESCORT, DESTROYER_ESCORT);
        VIEW_TYPE_MAP.put(ShipType.MINESWEEPER, PATROL);
        VIEW_TYPE_MAP.put(ShipType.MINELAYER, PATROL);
        VIEW_TYPE_MAP.put(ShipType.SLOOP, PATROL);
        VIEW_TYPE_MAP.put(ShipType.OILER, TRANSPORT);
        VIEW_TYPE_MAP.put(ShipType.TRANSPORT, TRANSPORT);
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
     *
     * @param type The model ship type.
     * @return The view ship type.
     */
    public static ShipViewType get(final ShipType type) {
        return VIEW_TYPE_MAP.get(type);
    }

    /**
     * Return the string representation of the ship view type.
     *
     * @return The string representation of the ship view type.
     */
    @Override
    public String toString() {
        return value;
    }

}
