package engima.waratsea.model.map;

import lombok.Getter;
import lombok.Setter;

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
    private String mapReference;

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
        buildMapReference();
    }

    /**
     * Get the map reference that corresponds to the game grid.
     */
    private void buildMapReference() {
        final int asciiA = 65;

        int oneBasedRow = row + 1;
        int factor = column / ALPHABET_SIZE;
        int mod = column % ALPHABET_SIZE;

        if (factor == 0) {
            mapReference = Character.toString((char) (asciiA + column)) + oneBasedRow;
        } else {
            char first = (char) (asciiA - 1 + factor);
            char second = (char) (asciiA + mod);
            mapReference = first + Character.toString(second) + oneBasedRow;
        }
    }
}
