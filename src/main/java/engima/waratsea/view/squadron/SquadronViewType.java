package engima.waratsea.view.squadron;

import engima.waratsea.model.aircraft.AircraftType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maps the model's aircraft types into a squadron view type.
 */
public enum SquadronViewType {
    FIGHTER("Fighter"),
    BOMBER("Bomber"),
    DIVE_BOMBER("Dive Bomber"),
    TORPEDO_BOMBER("Torpedo Bomber"),
    RECONNAISSANCE("Reconnaissance");

    @Getter
    private String value;

    private static Map<AircraftType, SquadronViewType> viewTypeMap = new HashMap<>();

    static {
        viewTypeMap.put(AircraftType.FIGHTER, FIGHTER);
        viewTypeMap.put(AircraftType.BOMBER, BOMBER);
        viewTypeMap.put(AircraftType.POOR_NAVAL_BOMBER, BOMBER);
        viewTypeMap.put(AircraftType.DIVE_BOMBER, DIVE_BOMBER);
        viewTypeMap.put(AircraftType.TORPEDO_BOMBER, TORPEDO_BOMBER);
        viewTypeMap.put(AircraftType.RECONNAISSANCE, RECONNAISSANCE);
    }

    /**
     * Constructor.
     * @param value The String value of the enum.
     */
    SquadronViewType(final String value) {
        this.value = value;
    }

    /**
     * Determine the view squadron type that corresponds to the given model aircraft type. Some model's of aircraft
     * are classified into the same view type in the GUI.
     *
     * @param type The aircraft type.
     * @return The view squadron type.
     */
    public static SquadronViewType get(final AircraftType type) {
        return viewTypeMap.get(type);
    }

    /**
     * Return the string representation of the squadron view type.
     *
     * @return The string representation of the squadron view type.
     */
    @Override
    public String toString() {
        return value;
    }
}
