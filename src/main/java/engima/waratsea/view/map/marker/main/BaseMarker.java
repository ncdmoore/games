package engima.waratsea.view.map.marker.main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.model.map.BaseGridType;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
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
     * @param imageResourceProvider Provides images for this marker.
     */
    @Inject
    public BaseMarker(@Assisted final BaseGrid baseGrid,
                      @Assisted final GridView gridView,
                                final ImageResourceProvider imageResourceProvider) {
        this.baseGrid = baseGrid;

        BaseGridType type = baseGrid.getType();

        final String imagePrefix = baseGrid.getSide().getValue().toLowerCase();
        this.imageView = imageResourceProvider.getImageView(imagePrefix + type.getValue() + ".png");

        imageView.setX(gridView.getX());
        imageView.setY(gridView.getY());
        imageView.setViewOrder(ViewOrder.MARKER.getValue());

        imageView.setUserData(this);

        imageView.setId("map-base-grid-marker");
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
     * Hide the base marker.
     *
     * @param mapView The map where the base marker is hidden.
     */
    public void hide(final MapView mapView) {
        mapView.remove(imageView);
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
