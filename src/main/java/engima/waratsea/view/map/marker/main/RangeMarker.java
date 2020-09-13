package engima.waratsea.view.map.marker.main;

import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Optional;

public class RangeMarker {

    private final MapView mapView;
    private final GridView gridView;

    private Circle circle;

    /**
     * Constructor.
     *
     * @param mapView The view of the map.
     * @param gridView The grid view of the grid containing the marker.
     */
    public RangeMarker(final MapView mapView, final GridView gridView) {
        this.mapView = mapView;
        this.gridView = gridView;
    }

    /**
     * Draw the range marker.
     *
     * @param gridRadius The grid radius of the range marker.
     */
    public void draw(final int gridRadius) {
        hide(); // Hide any old circles.

        int offset = gridView.getSize() / 2;

        int radius  = gridRadius * gridView.getSize();

        circle = new Circle(gridView.getX() + offset, gridView.getY() + offset, radius);
        circle.setStroke(Color.BROWN);
        circle.setFill(null);

        // Clip the circle with the main map rectangle to prevent the circles near the edges from flowing over the map
        // boundaries.
        ImageView mapImageView = mapView.getBackground();
        Image image = mapImageView.getImage();
        circle.setClip(new Rectangle(mapImageView.getX(), mapImageView.getY(), image.getWidth(), image.getHeight()));

        mapView.add(circle);

    }

    /**
     * Hide the range marker.
     */
    public void hide() {
        Optional
                .ofNullable(circle)
                .ifPresent(mapView::remove);
    }
}
