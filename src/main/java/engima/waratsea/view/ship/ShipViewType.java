package engima.waratsea.view.ship;

import engima.waratsea.model.ship.ShipType;
import lombok.Getter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * How the GUI classifies ship types.
 */
public enum ShipViewType  implements Comparator<ShipViewType> {
    AIRCRAFT_CARRIER("Aircraft Carrier", 1),
    BATTLESHIP("Battleship", 2),
    CRUISER("Cruiser", 3),
    DESTROYER("Destroyer", 4),
    DESTROYER_ESCORT("Destroyer Escort", 5),
    PATROL("Patrol", 6),
    TRANSPORT("Transport", 7);

    @Getter
    private final String value;

    private final Integer order;  // Enum sort order.

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
        VIEW_TYPE_MAP.put(ShipType.FLAK_SHIP, PATROL);
        VIEW_TYPE_MAP.put(ShipType.SLOOP, PATROL);
        VIEW_TYPE_MAP.put(ShipType.OILER, TRANSPORT);
        VIEW_TYPE_MAP.put(ShipType.TRANSPORT, TRANSPORT);
    }

    /**
     * Constructor.
     *
     * @param value The string value of the enum.
     * @param order The sort ordr.
     */
    ShipViewType(final String value, final int order) {
        this.value = value;
        this.order = order;
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

    /**
     * Get a stream of all of the ship view types.
     *
     * @return A stream of the ship view types.
     */
    public static Stream<ShipViewType> stream() {
        return Stream.of(ShipViewType.values());
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     * <p>
     * The implementor must ensure that {@code sgn(compare(x, y)) ==
     * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
     * implies that {@code compare(x, y)} must throw an exception if and only
     * if {@code compare(y, x)} throws an exception.)<p>
     * <p>
     * The implementor must also ensure that the relation is transitive:
     * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
     * {@code compare(x, z)>0}.<p>
     * <p>
     * Finally, the implementor must ensure that {@code compare(x, y)==0}
     * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
     * {@code z}.<p>
     * <p>
     * It is generally the case, but <i>not</i> strictly required that
     * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."<p>
     * <p>
     * In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(final ShipViewType o1, final ShipViewType o2) {
        return o1.order.compareTo(o2.order);
    }
}
