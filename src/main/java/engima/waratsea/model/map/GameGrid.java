package engima.waratsea.model.map;

import lombok.Getter;

/**
 * Represents map grid coordinates. This is a single rectangular grid.
 */
public class GameGrid {

    @Getter
    private final int row;

    @Getter
    private final int column;

    /**
     * Create a map grid.
     *
     * @param row The map row that corresponds to this grid.
     * @param column The map column that corresponds to this grid.
     */
    public GameGrid(final int row, final int column) {
        this.row = row;
        this.column = column;
    }
}
