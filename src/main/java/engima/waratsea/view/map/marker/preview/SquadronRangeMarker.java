package engima.waratsea.view.map.marker.preview;

import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;


public class SquadronRangeMarker {

    @Getter
    private final MapView mapView;

    @Getter
    private final GridView gridView;

    @Getter
    private Asset squadron;

    private List<Circle> rangeCircles;

    private ImageView imageView;

    /**
     * The constructor.
     *
     * @param dto The data transfer object.
     */
    public SquadronRangeMarker(final AssetMarkerDTO dto) {
        mapView = dto.getMapView();
        gridView = dto.getGridView();
        squadron = dto.getAsset();
        imageView = dto.getImageView();
    }

    /**
     * Display the squadron range radius.
     */
    public void display() {
        draw();                    // Draw a new circle.
        rangeCircles.forEach(mapView::add);

    }

    /**
     * Hide or remove the squadron range radius.
     */
    public void hide() {
        rangeCircles.forEach(mapView::remove);
    }

    /**
     * Draw the squadron range radius.
     */
    private void draw() {
        rangeCircles = ((Squadron) squadron)
                .getRadius()
                .stream()
                .map(this::drawCircle)
                .collect(Collectors.toList());
    }

    /**
     * Draw a combat radius circle.
     *
     * @param combatRadius The squadron's combat radius.
     * @return A circle that corresponds to the squadron's combat radius.
     */
    private Circle drawCircle(final int combatRadius) {
        double radius = combatRadius * gridView.getSize();
        double offset = (double) gridView.getSize() / 2;

        Circle circle = new Circle(gridView.getX() + offset, gridView.getY() + offset, radius);
        circle.setFill(null);
        circle.setStroke(Color.BLACK);

        // Use the image to get a rectangle that can be used to clip the range radius circles to keep them
        // from leaking outside the map.
        Image image = imageView.getImage();
        circle.setClip(new Rectangle(imageView.getX(), imageView.getY(), image.getWidth(), image.getHeight()));

        circle.setViewOrder(ViewOrder.MARKER.getValue());


        return circle;
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
        Circle circle = new Circle(x + radius, y + radius, radius);
        circle.setFill(null);
        circle.setStroke(Color.BLACK);
       // circle.getStyleClass().add("mine-marker");
        return circle;
    }
}
