package engima.waratsea.view.map;

import engima.waratsea.model.map.GameGrid;
import lombok.Getter;

/**
 * Represents a grid view on the game map view. A view of a map is scaled. For example the preview map is small and
 * therefore has a small grid view. The main map is larger and has a larger grid view.
 */
public class GridView {
    @Getter
    private final int row;

    @Getter
    private final int column;

    @Getter
    private final int x;

    @Getter
    private final int y;

    @Getter
    private final int size;

    /**
     * Create a map grid.
     *
     * @param size The size of the grid. The grid is a square.
     * @param grid The game grid that this view represents.
     */
    public GridView(final int size, final GameGrid grid) {
        this.row = grid.getRow();
        this.column = grid.getColumn();
        this.x = column * size;                                         // x-coordinate of the top left corner.

        int yOffset = (column & 1) == 1 ? size / 2 : 0;

        this.y = row * size + yOffset;                                  // y-coordinate of the top left corner.

        this.size = size;
    }
}
