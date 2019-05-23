package engima.waratsea.view.map;

import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.presenter.dto.map.MinefieldDTO;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Getter;


/**
 * A mine marker on a map.
 */
public class MineMarker  {
    private static final double OPACITY = 1.0;

    private final MapView mapView;

    @Getter
    private GridView gridView;

    private Rectangle rectangle;

    private Circle circle;

    @Getter
    private Minefield minefield;

    /**
     * Construct a marker.
     *
     * @param mapView The map view that contains the mine marker.
     * @param gridView The grid view of the mine marker.
     */
    public MineMarker(final MapView mapView, final GridView gridView) {
        this.mapView = mapView;
        this.gridView = gridView;
    }

    /**
     * Draw the marker on the provided map. Register a mouse click callback for the marker.
     *
     * @param dto The data transfer object.
     */
    public void draw(final MinefieldDTO dto) {
        minefield = dto.getMinefield();


        double radius = (double) gridView.getSize() / 2;
        circle = new Circle(gridView.getX() + radius, gridView.getY() + radius, radius);
        circle.setOnMouseClicked(dto.getRemoveMineHandler());
        circle.setUserData(this);

        circle.getStyleClass().add("mine-marker");

        mapView.add(circle);
    }

    /**
     * Remove the marker from the provided map.
     **/
    public void remove() {
        mapView.remove(circle);
    }

    /**
     * Get the map legend key. This is just a duplicate circle that is the same
     * size and type as used for this marker. It is used in the map legend.
     *
     * @param x The marker's x coordinate.
     * @param y The marker's y coordinate.
     * @param radius The radius of the marker.
     * @return The marker legend key.
     */
    public static Node getLegend(final double x, final double y, final double radius) {
        Circle c = new Circle(x + radius, y + radius, radius);
        c.getStyleClass().add("mine-marker");
        return c;
    }

}
