package engima.waratsea.view.map;

import com.google.inject.Inject;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.presenter.dto.map.MinefieldDTO;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

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

    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     * @param imageResourceProvider The image resource provider.
     * @param gameMap The game map.
     * @param mapView A utility to aid in drawing the map grid.
     */
    @Inject
    public MinefieldPreviewMapView(final ViewProps props,
                                   final ImageResourceProvider imageResourceProvider,
                                   final GameMap gameMap,
                                   final MapView mapView) {
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.gameMap = gameMap;
        this.mapView = mapView;
    }

    /**
     * Draws the map grid.
     *
     * @return The node containing the map grid.
     */
    public Node draw() {
        ImageView imageView = imageResourceProvider.getImageView("previewMap.png");
        int gridSize = props.getInt("taskforce.previewMap.gridSize");

        Node grid = mapView.draw(gridSize);

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

        log.info("Mark mine");

        if (minefield.hasRoom()) {
            GridView gridView = mapView.getGridView(dto.getEvent());
            GameGrid gameGrid = new GameGrid(gridView.getRow(), gridView.getColumn());
            String mapRef = gameMap.convertGridToReference(gameGrid);

            MineMarker mineMarker = new MineMarker(mapView, gridView);
            mineMarker.draw(dto);
            minefield.addMine(mapRef);
        }
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

        GameGrid gameGrid = new GameGrid(gridView.getRow(), gridView.getColumn());
        String mapRef = gameMap.convertGridToReference(gameGrid);

        minefield.removeMine(mapRef);

        mineMarker.remove();
    }

    /**
     * Highlight and register a map reference grid.
     *
     * @param mapRef The map reference of a game grid.
     * @param handler The mouse click event handler for the game grid.
     */
    private void highlightAndRegister(final String mapRef, final EventHandler<? super MouseEvent> handler) {
        Optional.ofNullable(gameMap.getGrid(mapRef))
                .ifPresent(gameGrid -> highlightAndRegisterGrid(gameGrid, handler));
    }

    /**
     * Highlight the game grid. Register a mouse click event callback for the game grid.
     *
     * @param gameGrid The game grid.
     * @param handler The mouse click event handler.
     */
    private void highlightAndRegisterGrid(final GameGrid gameGrid, final EventHandler<? super MouseEvent> handler) {
        mapView.setBackground(gameGrid);
        mapView.registerMouseClick(gameGrid, handler);
    }

    /**
     * Remove the highlight and unregister a map reference grid.
     *
     * @param mapRef The map reference of a game grid.
     */
    private void removeHighlightAndUnregister(final String mapRef) {
        Optional.ofNullable(gameMap.getGrid(mapRef))
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
}
