package engima.waratsea.view.map;

import com.google.inject.Inject;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.presenter.dto.map.MinefieldDTO;
import engima.waratsea.utility.ColorMap;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.marker.preview.MineMarker;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a map view. A map view is as it sounds a view of the game map. For example the minefield
 * preview map is a map view of the game map.
 */
@Slf4j
public class MinefieldPreviewMapView {
    private ViewProps props;
    private ImageResourceProvider imageResourceProvider;

    private GameMap gameMap;
    private MapView mapView;
    private ColorMap colorMap;

    @Setter
    private Side side;

    private Set<String> markedMines = new HashSet<>();

    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     * @param imageResourceProvider The image resource provider.
     * @param gameMap The game map.
     * @param mapView A utility to aid in drawing the map grid.
     * @param colorMap The color map.
     */
    @Inject
    public MinefieldPreviewMapView(final ViewProps props,
                                   final ImageResourceProvider imageResourceProvider,
                                   final GameMap gameMap,
                                   final MapView mapView,
                                   final ColorMap colorMap) {
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.gameMap = gameMap;
        this.mapView = mapView;
        this.colorMap = colorMap;
    }

    /**
     * Draws the map grid.
     *
     * @return The node containing the map grid.
     */
    public Node draw() {
        ImageView imageView = imageResourceProvider.getImageView("previewMap.png");
        int gridSize = props.getInt("taskforce.previewMap.gridSize");

        Node grid = mapView.draw(imageView, gridSize);

        StackPane map = new StackPane(imageView, grid);
        map.setAlignment(Pos.TOP_LEFT);

        return map;
    }

    /**
     * Highlight the minefield grids.
     *
     * @param dto The minefield data transfer object.
     */
    public void highlight(final MinefieldDTO dto) {
        dto
                .getMinefield()
                .getZone()
                .getGrids()
                .forEach(mapRef -> highlightAndRegister(mapRef, dto.getAddMineHandler()));

        markExisting(dto);
    }

    /**
     * Remove the highlight from the minefield grids.
     *
     * @param dto The minefield data transfer object.
     */
    public void removeHighLight(final MinefieldDTO dto) {
        dto
                .getMinefield()
                .getZone()
                .getGrids()
                .forEach(this::removeHighlightAndUnregister);
    }

    /**
     * Mark a grid as having a mine.
     *
     * @param dto The minefield data transfer object.
     */
    public void markMine(final MinefieldDTO dto) {
        Minefield minefield = dto.getMinefield();

        GridView gridView = mapView.getGridView(dto.getEvent());
        GameGrid gameGrid = gameMap.getGrid(gridView.getRow(), gridView.getColumn());
        String mapRef = gameGrid.getMapReference();

        MineMarker mineMarker = new MineMarker(mapView, gridView);
        mineMarker.draw(dto);
        minefield.addMine(mapRef);

        markedMines.add(mapRef);
    }

    /**
     * Un mark a grid as having a mine.
     *
     * @param dto The minefield data transfer object.
     */
    public void unMarkMine(final MinefieldDTO dto) {
        Minefield minefield = dto.getMinefield();

        Node node = (Node) dto
                .getEvent()
                .getSource();

        MineMarker mineMarker = (MineMarker) node.getUserData();

        GridView gridView = mineMarker.getGridView();

        GameGrid gameGrid = gameMap.getGrid(gridView.getRow(), gridView.getColumn());
        String mapRef = gameGrid.getMapReference();

        minefield.removeMine(mapRef);

        mineMarker.remove();

        markedMines.remove(mapRef);
    }

    /**
     * Get the task force preview map legend.
     *
     * @return A grid pane that contains the task force preview map legend.
     */
    public Node getLegend() {
        int gridSize = props.getInt("taskforce.previewMap.gridSize");

        final double opacity = 0.6;

        GridPane gridPane = new GridPane();
        Node baseKey = MapView.getLegend(0, 0, gridSize);
        ((Shape) baseKey).setFill(colorMap.getBaseColor(side));
        baseKey.setOpacity(opacity);

        Node mineZoneKey = MapView.getLegend(0, 0, gridSize);
        ((Shape) mineZoneKey).setFill(Color.GRAY);
        mineZoneKey.setOpacity(opacity);

        gridPane.add(baseKey, 0, 0);
        gridPane.add(new Label("Friendly Port (May Mine)"), 1, 0);

        gridPane.add(mineZoneKey, 0, 1);
        gridPane.add(new Label("Mine Zone (May Mine)"), 1, 1);

        Node minefieldKey = MineMarker.getLegend(0, 0, gridSize / 2.0);
        gridPane.add(minefieldKey, 0, 2);
        gridPane.add(new Label("Minefield"), 1, 2);

        gridPane.setId("map-legend-grid");

        return gridPane;
    }

    /**
     * Highlight and register a map reference grid.
     *
     * @param mapRef The map reference of a game grid.
     * @param handler The mouse click event handler for the game grid.
     */
    private void highlightAndRegister(final String mapRef, final EventHandler<? super MouseEvent> handler) {
        gameMap.getGrid(mapRef)
                .ifPresent(gameGrid -> highlightAndRegisterGrid(gameGrid, handler));
    }

    /**
     * Highlight the game grid. Register a mouse click event callback for the game grid.
     *
     * @param gameGrid The game grid.
     * @param handler The mouse click event handler.
     */
    private void highlightAndRegisterGrid(final GameGrid gameGrid, final EventHandler<? super MouseEvent> handler) {

        Paint backgroundColor = gameMap.isLocationBase(gameGrid) ? getBaseColor(gameGrid) : Color.GRAY;

        mapView.setBackground(gameGrid, backgroundColor);
        mapView.registerMouseClick(gameGrid, handler);
    }

    /**
     * Get the color of a base.
     *
     * @param gameGrid The game grid.
     * @return The color of the side.
     */
    private Paint getBaseColor(final GameGrid gameGrid) {
        return colorMap.getBaseColor(side);
    }

    /**
     * Remove the highlight and unregister a map reference grid.
     *
     * @param mapRef The map reference of a game grid.
     */
    private void removeHighlightAndUnregister(final String mapRef) {
        gameMap.getGrid(mapRef)
                .ifPresent(this::removeHighlightAndUnregisterGrid);
    }

    /**
     * Remove the highlight from the game grid. Unregister a mouse click event callback for the game grid.
     *
     * @param gameGrid The game grid.
     */
    private void removeHighlightAndUnregisterGrid(final GameGrid gameGrid) {
        mapView.removeBackgroud(gameGrid);
    }

    /**
     * Mark all of a minefields existing mines.
     *
     * @param dto the minefield data transfer object.
     */
    private void markExisting(final MinefieldDTO dto) {
        List<String> existingMines = dto.getMinefield().getActiveMapRef();

        existingMines
                .stream()
                .filter(mapRef -> !markedMines.contains(mapRef))
                .forEach(mapRef -> markExistingMine(mapRef, dto));
    }

    /**
     * Mark a grid as having a mine. This is needed when a user navigates between the minefield screen and other
     * screens. We need to mark any existing mines on the selected minefield.
     *
     * @param mapRef The map reference where the mine is marked.
     * @param dto The minefield data transfer object.
     */
    private void markExistingMine(final String mapRef, final MinefieldDTO dto) {
        GameGrid gameGrid = gameMap.getGrid(mapRef).orElse(null);
        if (gameGrid != null) {
            GridView gridView = new GridView(mapView.getGridSize(), gameGrid);

            MineMarker mineMarker = new MineMarker(mapView, gridView);
            mineMarker.draw(dto);

            markedMines.add(mapRef);
        } else {
            log.error("Unable to get grid from map reference: '{}'", mapRef);
        }
    }
}
