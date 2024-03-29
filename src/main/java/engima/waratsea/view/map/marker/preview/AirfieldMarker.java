package engima.waratsea.view.map.marker.preview;

import com.google.inject.Inject;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.model.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import engima.waratsea.view.map.marker.preview.adjuster.Adjuster;
import engima.waratsea.view.map.marker.preview.adjuster.AdjusterProvider;
import javafx.scene.Node;
import javafx.scene.shape.Polygon;
import lombok.Getter;


/**
 * A mine marker on a map.
 */
public class AirfieldMarker {
    private static final double OPACITY = 0.7;

    private final AdjusterProvider adjusterProvider;

    private MapView mapView;

    @Getter
    private GridView gridView;

    private Polygon triangle;

    @Getter
    private Asset airfield;

    @Getter
    private Nation nation;

    private PopUp popUp;

    @Inject
    public AirfieldMarker(final AdjusterProvider adjusterProvider) {
        this.adjusterProvider = adjusterProvider;
    }

    /**
     * Construct a marker.
     *
     * @param dto The data transfer object.
     */
    public void build(final AssetMarkerDTO dto) {
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

        Adjuster adjuster = adjusterProvider.get(airfield.getName());

        double x = adjuster.adjustX(gridView.getX());
        double y = adjuster.adjustY(gridView.getY());

        double size = gridView.getSize();
        triangle = new Polygon(x, y + size,
                x + size, y + size,
                x + size / 2, y);
        triangle.setOpacity(OPACITY);

        triangle.setOnMouseClicked(dto.getMarkerEventHandler());
        triangle.setUserData(this);

        triangle.setViewOrder(ViewOrder.MARKER.getValue());

        String style = convertNationName(nation) + "-airfield-" + ((Airfield) airfield).getAirbaseType() + "-marker";

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
     * Get the map legend key for airfields. This is just a marker that is used in the map legend.
     *
     * @param nation The nation.
     * @param x The marker's x coordinate.
     * @param y The marker's y coordinate.
     * @param size The size of the marker.
     * @return The airfield marker legend key.
     */
    public static Node getLegendAirfield(final Nation nation, final double x, final double y, final double size) {
        Polygon triangle = new Polygon(x, y + size,
                x + size, y + size,
                x + size / 2, y);

        String style = convertNationName(nation) + "-airfield-land-marker";

        triangle.getStyleClass().add(style);
        return triangle;
    }

    /**
     * Get the map legend key for seaplanes. This is just a seaplane marker that is used in the map legend.
     *
     * @param nation The nation.
     * @param x The marker's x coordinate.
     * @param y The marker's y coordinate.
     * @param size The size of the marker.
     * @return The seaplane marker legend key.
     */
    public static Node getLegendSeaplane(final Nation nation, final double x, final double y, final double size) {
        Polygon triangle = new Polygon(x, y + size,
                x + size, y + size,
                x + size / 2, y);

        String style = convertNationName(nation) + "-airfield-seaplane-marker";

        triangle.getStyleClass().add(style);
        return triangle;
    }

    /**
     * Get the map legend key for airfield's that support land and seaplanes.
     * This is just a land and seaplane marker that is used in the map legend.
     *
     * @param nation The nation.
     * @param x The marker's x coordinate.
     * @param y The marker's y coordinate.
     * @param size The size of the marker.
     * @return The seaplane marker legend key.
     */
    public static Node getLegendBoth(final Nation nation, final double x, final double y, final double size) {
        Polygon triangle = new Polygon(x, y + size,
                x + size, y + size,
                x + size / 2, y);

        String style = convertNationName(nation) + "-airfield-both-marker";

        triangle.getStyleClass().add(style);
        return triangle;
    }



    /**
     * Convert the nation name to a CSS style name.
     *
     * @param country The nation: BRITISH, ITALLIAN, etc ...
     * @return The CSS style version of the nation's name.
     */
    private static String convertNationName(final Nation country) {
        return country.toString().toLowerCase().replace(" ", "-");
    }
}
