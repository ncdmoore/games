package engima.waratsea.view.squadron;

import engima.waratsea.model.aircraft.AircraftType;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Convert a model squadron type map to a view squadron type map.
     *
     * @param <T> The type of list.
     * @param map The map to convert.
     * @return A converted map.
     */
    public static <T> Map<SquadronViewType, List<T>> convertList(final Map<AircraftType, List<T>> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> get(entry.getKey()),     // Convert to squadron view type key.
                        Map.Entry::getValue,              // Copy over the squadron list.
                        ListUtils::union));               // Merge any aircraft type keys that map to the same squadron view type.
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
     * Convert a model squadron type map to a view squadron type map.
     * @param map The map to convert.
     * @return A converted map.
     */
    public static Map<SquadronViewType, Integer> convertInteger(final Map<AircraftType, Integer> map) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> get(entry.getKey()),     // Convert to squadron view type key.
                        Map.Entry::getValue,              // Copy over the squadron list.
                        Integer::sum));                   // Merge any aircraft type keys that map to the same squadron view type.
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
