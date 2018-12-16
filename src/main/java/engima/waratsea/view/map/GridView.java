package engima.waratsea.view.map;

import lombok.Getter;
import lombok.Setter;

public class GridView {
    @Getter
    @Setter
    private int row;

    @Getter
    @Setter
    private int column;

    @Getter
    @Setter
    private int x;

    @Getter
    @Setter
    private int y;


    @Getter
    private int size;

    /**
     * Create a map grid.
     *
     * @param size The size of the grid. The grid is a square.
     * @param row The map row that corresponds to this grid.
     * @param column The map column that corresponds to this grid.
     */
    public GridView(final int size, final int row, final int column) {
        this.row = row;
        this.column = column;
        this.x = column * size;                                         // x-coordinate of the top left corner.

        int yOffset = (column & 1) == 1 ? size / 2 : 0;

        this.y = row * size + yOffset;                                  // y-coordinate of the top left corner.

        this.size = size;
    }
}
