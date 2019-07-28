package engima.waratsea.view.map.marker;

import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.game.Nation;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.scene.Node;
import javafx.scene.shape.Polygon;
import lombok.Getter;


/**
 * A mine marker on a map.
 */
public class AirfieldMarker {
    private static final double OPACITY = 0.7;

    private final MapView mapView;

    @Getter
    private final GridView gridView;

    private Polygon triangle;

    @Getter
    private Asset airfield;

    @Getter
    private final Nation nation;

    private final PopUp popUp;

    /**
     * Construct a marker.
     *
     * @param dto The data transfer object.
     */
    public AirfieldMarker(final AssetMarkerDTO dto) {
        mapView = dto.getMapView();
        gridView = dto.getGridView();
        nation = dto.getNation();

        dto.setStyle("popup-airfield");
        popUp = new PopUp(dto);
    }

    /**
     * Draw the marker on the provided map. Register a mouse click callback for the marker.
     *
     * @param dto The data transfer object.
     */
    public void draw(final AssetMarkerDTO dto) {
        airfield = dto.getAsset();

        double size = (double) gridView.getSize();
        triangle = new Polygon(gridView.getX(), gridView.getY() + size,
                gridView.getX() + size, gridView.getY() + size,
                gridView.getX() + size / 2, gridView.getY());
        triangle.setOpacity(OPACITY);

        triangle.setOnMouseClicked(dto.getMarkerEventHandler());
        triangle.setUserData(this);

        String style = nation.toString().toLowerCase().replace(" ", "-") + "-airfield-marker";

        triangle.getStyleClass().add(style);

        mapView.add(triangle);

        popUp.draw(dto);
    }

    /**
     * Remove the marker from the provided map.
     **/
    public void remove() {
        mapView.remove(triangle);
        popUp.hide(mapView);
    }

    /**
     * Select this marker. The marker is now the currently selected marker.
     *
     * @param map The game map.
     * @param name The name of the task force.
     */
    public void select(final MapView map, final String name) {
        triangle.setOpacity(1.0);
        popUp.display(map, name);
    }

    /**
     * Clear this marker. The marker is no longer selected if it was selected.
     *
     * @param map The game map.
     **/
    public void clear(final MapView map) {
        triangle.setOpacity(OPACITY);
        popUp.hide(map);
    }

    /**
     * Move the marker's popup away from the bottom of the map.
     *
     * @param offset How much the y is adjusted once.
     * @param yThreshold Determines if the popup is near the bottom and needs to be moved up.
     **/
    public void adjustY(final int offset, final int yThreshold) {
        popUp.adjustY(offset, yThreshold);
    }

    /**
     * Get the map legend key. This is just a marker that used in the map legend.
     *
     * @param nation The nation.
     * @param x The marker's x coordinate.
     * @param y The marker's y coordinate.
     * @param size The size of the marker.
     * @return The marker legend key.
     */
    public static Node getLegend(final Nation nation, final double x, final double y, final double size) {
        Polygon triangle = new Polygon(x, y + size,
                x + size, y + size,
                x + size / 2, y);

        String style = nation.toString().toLowerCase().replace(" ", "-") + "-airfield-marker";

        triangle.getStyleClass().add(style);
        return triangle;
    }

}
