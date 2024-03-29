package engima.waratsea.model.map;

import lombok.Getter;

/**
 * Represents a grid view on the game map view. A view of a map is scaled. For example the preview map is small and
 * therefore has a small grid view. The main map is larger and has a larger grid view.
 *
 * This is a singular rectangular grid view of the game map grid.
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

    @Getter
    private final Point center;

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

        int xCenter = x + (size / 2);
        int yCenter = y - (size / 2);

        center = new Point(xCenter, yCenter);
    }

    public GridView(final int size, final Point point) {
        this.x = point.getX();
        this.y = point.getY();
        this.size = size;

        this.column = x / size;

        int yOffset = (column & 1) == 1 ? size / 2 : 0;

        double r = (double) ((y - yOffset)) / size;

        this.row = (int) Math.ceil(r); //always round up to get the correct row.

        int xCenter = x + (size / 2);
        int yCenter = y - (size / 2);

        center = new Point(xCenter, yCenter);
    }


}
