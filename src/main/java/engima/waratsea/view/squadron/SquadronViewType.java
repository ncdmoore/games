package engima.waratsea.view.squadron;

import engima.waratsea.model.aircraft.AircraftType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final String value;

    private static final Map<AircraftType, SquadronViewType> VIEW_TYPE_MAP = new HashMap<>();

    static {
        VIEW_TYPE_MAP.put(AircraftType.FIGHTER, FIGHTER);
        VIEW_TYPE_MAP.put(AircraftType.BOMBER, BOMBER);
        VIEW_TYPE_MAP.put(AircraftType.ITALIAN_BOMBER, BOMBER);
        VIEW_TYPE_MAP.put(AircraftType.DIVE_BOMBER, DIVE_BOMBER);
        VIEW_TYPE_MAP.put(AircraftType.TORPEDO_BOMBER, TORPEDO_BOMBER);
        VIEW_TYPE_MAP.put(AircraftType.RECONNAISSANCE, RECONNAISSANCE);
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
        return VIEW_TYPE_MAP.get(type);
    }

    /**
     * Convert a model squadron type map to a view squadron type map.
     * @param map The map to convert.
     * @return A converted map.
     */
    public static Map<SquadronViewType, BigDecimal> convertBigDecimal(final Map<AircraftType, BigDecimal> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> get(entry.getKey()),     // Convert to squadron view type key.
                        Map.Entry::getValue,              // Copy over the squadron list.
                        BigDecimal::add));                // Merge any aircraft type keys that map to the same squadron view type.
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

    /**
     * Get a stream of this enum's values.
     *
     * @return A stream of this enum's values.
     */
    public static Stream<SquadronViewType> stream() {
        return Stream.of(SquadronViewType.values());
    }
}
