package engima.waratsea.view.map;

import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.MultiKeyMap;

/**
 * Utility class for drawing a grid on a map.
 */
@Slf4j
public class MapView {

    @Getter
    private int gridSize;

    @Getter
    private Group map = new Group();

    private MultiKeyMap<Integer, Rectangle> grid = new MultiKeyMap<>();

    /**
     * Draw the map's grid.
     * @param gameMap The game map that this view represents.
     * @param sizeOfTheGrids Size of the square grid in pixels.
     * @return The grid.
     */
    public Group draw(final GameMap gameMap, final int sizeOfTheGrids) {
        int numberOfRows = gameMap.getRows();
        int numberOfColumns = gameMap.getColumns();

        gridSize = sizeOfTheGrids;

        int currentNumberOfRows = numberOfRows;

        for (int col = 0; col < numberOfColumns; col++) {
            for (int row = 0; row < currentNumberOfRows; row++) {
                GridView gridView = getGridView(new GameGrid(row, col));
                Rectangle r = drawSingleGrid(gridView);
                map.getChildren().add(r);
                grid.put(row, col, r);
            }

            currentNumberOfRows = (currentNumberOfRows == numberOfRows) ? numberOfRows - 1 : numberOfRows;
        }

        return map;
    }

    /**
     * Get a grid view.
     * @param gameGrid The game grid for which the grid view corresponds.
     * @return The grid view.
     */
    public GridView getGridView(final GameGrid gameGrid) {
        return new GridView(gridSize, gameGrid);
    }

    /**
     * Add a node to the map. This is called to add rectangles to the map.
     * @param node The node added to the map.
     */
    public void add(final Node node) {
        map.getChildren().add(node);
    }

    /**
     * Remove a node from the map.
     * @param node The node removed from the map.
     */
    public void remove(final Node node) {
        map.getChildren().remove(node);
    }

    /**
     * Draw a rectangle.
     *
     * @param gridView The grid's view.
     * @return A rectangle.
     */
    private Rectangle drawSingleGrid(final GridView gridView) {

        final double opacity = 0.07;

        Rectangle r = new Rectangle(gridView.getX(), gridView.getY(), gridView.getSize(), gridView.getSize());
        r.setStroke(Color.BLACK);
        r.setFill(null);
        r.setOpacity(opacity);
        return r;
    }

    /**
     * Highlight a single grid on the map.
     *
     * @param gameGrid The corresponding game grid.
     */
    public void highlight(final GameGrid gameGrid) {

        Rectangle r = grid.get(gameGrid.getRow(), gameGrid.getColumn());

        if (r == null) {
            log.error("highlight row: {}, column: {}", gameGrid.getRow(), gameGrid.getColumn());
            return;
        }

        r.setOpacity(1.0);
        r.setStroke(Color.RED);
    }
}
