package engima.waratsea.view.map;

import com.google.inject.Inject;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;

/**
 * The view of the main map.
 */
@Slf4j
public class MainMapView {

    private GameMap gameMap;
    private ViewProps props;
    private ImageResourceProvider imageResourceProvider;


    private MapView mapView;

    /**
     * Constructor called by guice.
     * @param gameMap The game map.
     * @param props The view properties.
     * @param imageResourceProvider provides images.
     * @param mapView A utility to draw the map's grid.
     */
    @Inject
    public MainMapView(final GameMap gameMap,
                       final ViewProps props,
                       final ImageResourceProvider imageResourceProvider,
                       final MapView mapView) {
        this.gameMap = gameMap;
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
        this.mapView = mapView;
    }

    /**
     * Build the main game map view.
     * @return  The main game map.
     */
    public Node build() {
        ImageView mapImageView = imageResourceProvider.getImageView("mainMap.png");
        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        Node mapGrid = mapView.draw(gridSize);

        gameMap.getBases().forEach(base -> mapView.highlight(gameMap.getGrid(base.getReference())));

        mapView.registerMouseClick(this::mouseClicked);

        StackPane map = new StackPane(mapImageView, mapGrid);
        map.setAlignment(Pos.TOP_LEFT);

        return map;
    }

    /**
     * Callback when main map grid is clicked.
     *
     * @param event The mouse click event.
     */
    private void mouseClicked(final MouseEvent event) {
        Rectangle r = (Rectangle) event.getSource();
        GridView gv = mapView.getGridView(r);
        log.info("row={},column={}", gv.getRow(), gv.getColumn());

        log.info(gameMap.convertRowColumnToRef(gv.getRow(), gv.getColumn()));

    }
}
