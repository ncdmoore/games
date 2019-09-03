package engima.waratsea.view.map.marker.main;

import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BaseMarker {

    @Getter
    private final BaseGrid baseGrid;


    private final ImageView imageView;

    /**
     * The constructor.
     *
     * @param baseGrid The base's map grid.
     * @param gridView The view of the map grid.
     * @param imageView The background image of this marker.
     */
    public BaseMarker(final BaseGrid baseGrid, final GridView gridView, final ImageView imageView) {
        this.baseGrid = baseGrid;
        this.imageView = imageView;

        imageView.setX(gridView.getX());
        imageView.setY(gridView.getY());

        imageView.setUserData(this);
    }

    /**
     * Draw the base marker.
     *
     * @param mapView The map upon which the base marker is drawn.
     */
    public void draw(final MapView mapView) {
        mapView.add(imageView);
    }

    /**
     * Set the Base marker clicked handler.
     *
     * @param handler The mouse click event handler.
     */
    public void setBaseClickHandler(final EventHandler<? super MouseEvent> handler) {
        imageView.setOnMouseClicked(handler);
    }
}
