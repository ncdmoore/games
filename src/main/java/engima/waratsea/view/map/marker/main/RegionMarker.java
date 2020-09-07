package engima.waratsea.view.map.marker.main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.RegionGrid;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RegionMarker {
    @Getter private final RegionGrid regionGrid;
    private final MapView mapView;
    private final ViewProps props;

    private final Node title;

    @Getter private final List<BaseMarker> baseMarkers = new ArrayList<>();

    /**
     *  Constructor called by guice.
     *
     * @param regionGrid The region marker's map grid.
     * @param mapView The map view.
     * @param props The view properties.
     */
    @Inject
    public RegionMarker(@Assisted final RegionGrid regionGrid,
                        @Assisted final MapView mapView,
                                  final ViewProps props) {
        this.regionGrid = regionGrid;
        this.mapView = mapView;
        this.props = props;

        title = buildTitle(regionGrid);
    }

    public void add(final BaseMarker baseMarker) {
        baseMarkers.add(baseMarker);
    }

    /**
     * Show this region's title.
     */
    public void draw() {
        mapView.add(title);
    }

    /**
     * Hide this region's title.
     */
    public void hide() {
        mapView.remove(title);
    }

    /**
     * Set the Base marker mouse enter handler.
     *
     * @param handler The mouse entered event handler.
     */
    public void setRegionMouseEnterHandler(final EventHandler<? super MouseEvent> handler) {
        title.setOnMouseEntered(handler);
    }

    /**
     * Set the Base marker mouse exit handler.
     *
     * @param handler The mouse exit event handler.
     */
    public void setRegionMouseExitHandler(final EventHandler<? super MouseEvent> handler) {
        title.setOnMouseExited(handler);
    }

    /**
     * Build the region's title.
     *
     * @param regionGrid The game map region grid.
     * @return A node containing the region's title.
     */
    private Node buildTitle(final RegionGrid regionGrid) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(regionGrid
                .getNations()
                .stream()
                .map(Nation::toString)
                .collect(Collectors.joining("\n")));
        tooltip.getStyleClass().add("region-tooltip");


        Label label = new Label(regionGrid.getName());
        label.setTooltip(tooltip);

        label.getStyleClass().add("region-label");

        VBox vBox = new VBox(label);

        vBox.getStyleClass().add("region-name-vbox");

        int gridSize = props.getInt("taskforce.mainMap.gridSize");

        GridView gridView = new GridView(gridSize, regionGrid.getGameGrid());

        vBox.setLayoutY(gridView.getY());
        vBox.setLayoutX(gridView.getX() - (gridSize / 2.0));
        vBox.setUserData(this);
        vBox.setId("region-marker-title");
        return vBox;
    }
}
