package engima.waratsea.model.map;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents map grid coordinates.
 */
public class Grid {

    @Getter
    @Setter
    private int row;

    @Getter
    @Setter
    private int column;

    /**
     * Create a map grid.
     *
     * @param row The map row that corresponds to this grid.
     * @param column The map column that corresponds to this grid.
     */
    public Grid(final int row, final int column) {
        this.row = row;
        this.column = column;
    }
}
