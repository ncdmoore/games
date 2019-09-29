package engima.waratsea.view.map.marker.main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.model.map.BaseGridType;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
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

    private final Game game;

    private final ImageView imageView;
    private final ImageView roundel;

    /**
     * The constructor.
     *
     * @param baseGrid The base's map grid.
     * @param gridView The view of the map grid.
     * @param game The game.
     * @param imageResourceProvider Provides images for this marker.
     * @param props The view properties.
     */
    @Inject
    public BaseMarker(@Assisted final BaseGrid baseGrid,
                      @Assisted final GridView gridView,
                      final Game game,
                      final ImageResourceProvider imageResourceProvider,
                      final ViewProps props) {
        this.baseGrid = baseGrid;
        this.game = game;

        String scenarioName = game.getScenario().getName();

        BaseGridType type = baseGrid.getType();

        final String imagePrefix = baseGrid.getSide().getValue().toLowerCase();
        this.imageView = imageResourceProvider.getImageView(scenarioName, imagePrefix + type.getValue() + ".png");
        this.roundel = imageResourceProvider.getImageView(scenarioName, imagePrefix + "Roundel12x12.png");

        imageView.setX(gridView.getX());
        imageView.setY(gridView.getY());
        imageView.setViewOrder(ViewOrder.MARKER.getValue());

        imageView.setUserData(this);

        roundel.setX(gridView.getX() + props.getInt("main.map.base.marker.roudel.x.offset"));
        roundel.setY(gridView.getY() + 1);
        roundel.setViewOrder(ViewOrder.MARKER_DECORATION.getValue());

        imageView.setId("map-base-grid-marker");
    }

    /**
     * Draw the base marker.
     *
     * @param mapView The map upon which the base marker is drawn.
     */
    public void draw(final MapView mapView) {
        mapView.add(imageView);

        if (areSquadronsPresent() && game.getHumanPlayer().getSide() == baseGrid.getSide()) {
            mapView.add(roundel);
        }
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

    /**
     * Determine if any squadrons are present at the this marker's airfield, if
     * this marker contains an airfield.
     *
     * @return True if this marker's airfield contains squadrons. False otherwise.
     */
    private boolean areSquadronsPresent() {
        return baseGrid
                .getAirfield()
                .map(Airfield::areSquadronsPresent)
                .orElse(false);
    }

}
