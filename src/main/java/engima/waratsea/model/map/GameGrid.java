package engima.waratsea.model.map;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents map grid coordinates. This is a single rectangular grid.
 */
public class GameGrid {
    private static final int ALPHABET_SIZE = 26;

    @Getter
    private final int row;

    @Getter
    private final int column;

    @Getter
    private final String mapReference;

    @Getter
    @Setter
    private GridType type;

    /**
     * Create a map grid.
     *
     * @param row The map row that corresponds to this grid.
     * @param column The map column that corresponds to this grid.
     */
    public GameGrid(final int row, final int column) {
        this.row = row;
        this.column = column;
        this.mapReference = buildMapReference();
    }

    /**
     * Determine if this game grid is equal to another grid.
     *
     * @param o The other grid.
     * @return True if this grid is equal to the given other gird. False otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GameGrid grid = (GameGrid) o;
        return row == grid.row && column == grid.column;
    }

    /**
     * The hash code for this class.
     *
     * @return This object's hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    /**
     * Get the map reference that corresponds to this game grid.
     *
     * @return The map reference that corresponds to this game grid.
     */
    private String buildMapReference() {
        String mapRef;
        final int asciiA = 65;

        int oneBasedRow = row + 1;
        int factor = column / ALPHABET_SIZE;
        int mod = column % ALPHABET_SIZE;

        if (factor == 0) {
            mapRef = Character.toString((char) (asciiA + column)) + oneBasedRow;
        } else {
            char first = (char) (asciiA - 1 + factor);
            char second = (char) (asciiA + mod);
            mapRef = first + Character.toString(second) + oneBasedRow;
        }

        return mapRef;
    }
}
