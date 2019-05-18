package engima.waratsea.view.map;

import com.google.inject.Inject;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * The view of the main map.
 */
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

        Node mapGrid = mapView.draw(gameMap, gridSize);


        gameMap.getBases().forEach(base -> mapView.highlight(gameMap.getGrid(base.getReference())));


        mapView.highlight(gameMap.getGrid("AJ24"));

        StackPane map = new StackPane(mapImageView, mapGrid);
        map.setAlignment(Pos.TOP_LEFT);

        return map;
    }

    public void addPopup(final Node node) {
        mapView.add(node);
    }
}
