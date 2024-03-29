package engima.waratsea.view.map;

import com.google.inject.Inject;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.GridView;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.MultiKeyMap;

/**
 * Utility class for drawing a grid on a map.
 */
@Slf4j
public class MapView {
    private static final double GRID_OPACITY = 0.07;
    private static final double BACKGROUND_OPACITY = 0.45;

    private final GameMap gameMap;

    private int numberOfRows;
    private int numberOfColumns;

    @Getter private ImageView background;
    @Getter private int gridSize;

    private final Group map = new Group();

    private final MultiKeyMap<Integer, Rectangle> grid = new MultiKeyMap<>();    //Row, Column to Rectangle map.
    private final MultiKeyMap<Integer, Node> gridLabels = new MultiKeyMap<>();   //Row, Column to grid label map.

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game map.
     */
    @Inject
    public MapView(final GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * Draw the map's grid.
     *
     * @param backgroundImage The background image of the map.
     * @param sizeOfTheGrids Size of the square grid in pixels.
     * @return The grid.
     */
    public Group draw(final ImageView backgroundImage, final int sizeOfTheGrids) {

        numberOfRows = gameMap.getRows();
        numberOfColumns = gameMap.getColumns();

        background = backgroundImage;
        gridSize = sizeOfTheGrids;

        int currentNumberOfRows = numberOfRows;

        map.getChildren().clear();   // This is needed if another game is loaded. We clear out the stale map.

        for (int col = 0; col < numberOfColumns; col++) {
            for (int row = 0; row < currentNumberOfRows; row++) {
                GridView gridView = getGridView(gameMap.getGrid(row, col));
                Rectangle r = drawSingleGrid(gridView);
                r.setUserData(gridView);

                r.setViewOrder(ViewOrder.GRID.getValue());

                map.getChildren().add(r);
                grid.put(row, col, r);
            }

            currentNumberOfRows = (currentNumberOfRows == numberOfRows) ? numberOfRows - 1 : numberOfRows;
        }

        return map;
    }

    /**
     * Draw the map reference labels.
     */
    public void drawMapReference() {
        int currentNumberOfRows = numberOfRows;

        for (int col = 0; col < numberOfColumns; col++) {
            for (int row = 0; row < currentNumberOfRows; row++) {
                Node node = addMapReference((GridView) grid.get(row, col).getUserData());
                gridLabels.put(row, col, node);
            }

            currentNumberOfRows = (currentNumberOfRows == numberOfRows) ? numberOfRows - 1 : numberOfRows;
        }
    }

    /**
     * Register a mouse click handler for each rectangle that makes up the map grid.
     *
     * @param handler The mouse click handler.
     */
    public void registerMouseClick(final EventHandler<? super MouseEvent> handler) {
        int currentNumberOfRows = numberOfRows;

        for (int col = 0; col < numberOfColumns; col++) {
            for (int row = 0; row < currentNumberOfRows; row++) {
                registerMouseClick(grid.get(row, col), handler);
            }

            currentNumberOfRows = (currentNumberOfRows == numberOfRows) ? numberOfRows - 1 : numberOfRows;
        }
    }

    /**
     * Get a grid view.
     *
     * @param gameGrid The game grid for which the grid view corresponds.
     * @return The grid view.
     */
    public GridView getGridView(final GameGrid gameGrid) {
        return new GridView(gridSize, gameGrid);
    }

    /**
     * Get a grid view.
     *
     * @param event A mouse click event.
     * @return The grid view.
     */
    public GridView getGridView(final MouseEvent event) {
        Rectangle r = (Rectangle) event.getSource();
        return (GridView) r.getUserData();
    }

    /**
     * Add a node to the map. This is called to add rectangles to the map.
     * The node will be displayed on the map.
     *
     * @param node The node added to the map.
     */
    public void add(final Node node) {
        if (!map.getChildren().contains(node)) {
            map.getChildren().add(node);
        }
    }

    /**
     * Remove a node from the map.
     *
     * @param node The node removed from the map.
     */
    public void remove(final Node node) {
        map.getChildren().remove(node);
    }

    /**
     * Toggle the map's grid. Right now we can only toggle the labels. Javafx has a bug around the view order
     * that causes the base markers to move when the grid id toggled.
     *
     * @param visible If true the grid and its labels are shown. If false the grid and its labels are hidden.
     */
    public void toggleGrid(final boolean visible) {
        int currentNumberOfRows = numberOfRows;

        for (int col = 0; col < numberOfColumns; col++) {
            for (int row = 0; row < currentNumberOfRows; row++) {
                //grid.get(row, col).setVisible(visible);
                gridLabels.get(row, col).setVisible(visible);
            }

            currentNumberOfRows = (currentNumberOfRows == numberOfRows) ? numberOfRows - 1 : numberOfRows;
        }
    }

    /**
     * Draw a rectangle.
     *
     * @param gridView The grid's view.
     * @return A rectangle.
     */
    private Rectangle drawSingleGrid(final GridView gridView) {
        Rectangle r = new Rectangle(gridView.getX(), gridView.getY(), gridView.getSize(), gridView.getSize());
        r.setStroke(Color.BLACK);
        r.setFill(Color.TRANSPARENT);
        r.setOpacity(GRID_OPACITY);

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

    /**
     * Set the background for a grid.
     *
     * @param gameGrid The grid for which the background is set.
     * @param color The color of the the new background.
     */
    public void setBackground(final GameGrid gameGrid, final Paint color) {

        Rectangle r = grid.get(gameGrid.getRow(), gameGrid.getColumn());

        if (r == null) {
            log.error("set background row: {}, column: {}", gameGrid.getRow(), gameGrid.getColumn());
            return;
        }

        r.setOpacity(BACKGROUND_OPACITY);
        r.setFill(color);
    }

    /**
     * Remove the background from a grid.
     *
     * @param gameGrid The grid for which the background is removed.
     */
    public void removeBackgroud(final GameGrid gameGrid) {
        Rectangle r = grid.get(gameGrid.getRow(), gameGrid.getColumn());

        if (r == null) {
            log.error("set background row: {}, column: {}", gameGrid.getRow(), gameGrid.getColumn());
            return;
        }

        r.setOpacity(GRID_OPACITY);
        r.setFill(Color.TRANSPARENT);
    }

    /**
     * Register a mouse click event handler for a grid.
     *
     * @param gameGrid The grid for which the mouse handler is registered.
     * @param handler The callback for when the grid is clicked.
     */
    public void registerMouseClick(final GameGrid gameGrid, final EventHandler<? super MouseEvent> handler) {
        Rectangle r = grid.get(gameGrid.getRow(), gameGrid.getColumn());

        registerMouseClick(r, handler);
    }

    /**
     * Get the map's legend key. This is just a rectangle of the same size as the grid
     * that can be used as a basis for map legend keys.
     *
     * @param x The key's x-coordinate.
     * @param y The keys's y-coordinate.
     * @param size The size of the key.
     * @return The map legend key.
     */
    public static Node getLegend(final double x, final double y, final double size) {
        Rectangle r = new Rectangle(x, y, size, size);
        r.setStroke(Color.BLACK);
        r.setFill(Color.TRANSPARENT);
        r.setOpacity(GRID_OPACITY);
        return r;
    }

    /**
     * Grid rectangle mouse over event registration.
     *
     * @param r rectangle.
     * @param handler The mouse click handler.
     */
    private void registerMouseClick(final Rectangle r, final EventHandler<? super MouseEvent> handler) {
        r.setOnMouseClicked(handler);
    }

    /**
     * Add a map reference label to the grid.
     *
     * @param gridView The grid view of map grid.
     * @return The node containing the map reference label.
     */
    private Node addMapReference(final GridView gridView) {
        GameGrid gameGrid = gameMap.getGrid(gridView.getRow(), gridView.getColumn());
        String mapRef = gameGrid.getMapReference();

        Text text = new Text(mapRef);
        VBox mapRefNode = new VBox(text);
        mapRefNode.setLayoutX(gridView.getX() + 1);
        mapRefNode.setLayoutY(gridView.getY() + 1);
        text.setViewOrder(ViewOrder.GRID_DECORATION.getValue());
        mapRefNode.setViewOrder(ViewOrder.GRID_DECORATION.getValue());

        text.getStyleClass().add("map-ref-text");

        add(mapRefNode);

        return mapRefNode;
    }
}
