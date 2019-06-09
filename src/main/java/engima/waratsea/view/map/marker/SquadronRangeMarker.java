package engima.waratsea.view.map.marker;

import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Getter;


public class SquadronRangeMarker {

    @Getter
    private final MapView mapView;

    @Getter
    private final GridView gridView;

    @Getter
    private Asset squadron;

    private Circle rangeCircle;

    private ImageView imageView;

    /**
     * The constructor.
     *
     * @param dto The data transfer object.
     */
    public SquadronRangeMarker(final TaskForceMarkerDTO dto) {
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
        mapView.add(rangeCircle);  // Add the new circle.
    }

    /**
     * Hide or remove the squadron range radius.
     */
    public void hide() {
        mapView.remove(rangeCircle);
    }

    /**
     * Draw the squadron range radius.
     */
    private void draw() {
        double radius = ((Squadron) squadron).getRange() * gridView.getSize();
        double offset = (double) gridView.getSize() / 2;

        rangeCircle = new Circle(gridView.getX() + offset, gridView.getY() + offset, radius);
        rangeCircle.setFill(null);
        rangeCircle.setStroke(Color.BLACK);

        // Use the image to get a rectangle that can be used to clip the range radius circles to keep them
        // from leaking outside the map.
        Image image = imageView.getImage();
        rangeCircle.setClip(new Rectangle(imageView.getX(), imageView.getY(), image.getWidth(), image.getHeight()));
    }
}
