package engima.waratsea.view.map.marker.main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.map.BaseGrid;
import engima.waratsea.model.map.BaseGridType;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BaseMarker {

    private static final int SHADOW_RADIUS = 3;

    @Getter
    private final BaseGrid baseGrid;

    private final MapView mapView;
    private final Game game;
    private final VBox imageView;
    private final VBox roundel;
    private final VBox flag;
    private final Node title;

    @Getter
    private final PatrolRadii patrolRadii;

    @Getter
    private MenuItem airfieldMenuItem;

    @Getter
    private MenuItem taskForceMenuItem;

    private boolean selected = false;

    /**
     * The constructor.
     *
     * @param baseGrid The base's map grid.
     * @param gridView The view of the map grid.
     * @param mapView The map view.
     * @param game The game.
     * @param imageResourceProvider Provides images for this marker.
     * @param props The view properties.
     */
    @Inject
    public BaseMarker(@Assisted final BaseGrid baseGrid,
                      @Assisted final GridView gridView,
                      @Assisted final MapView mapView,
                      final Game game,
                      final ImageResourceProvider imageResourceProvider,
                      final ViewProps props) {
        this.baseGrid = baseGrid;
        this.mapView = mapView;
        this.game = game;

        String scenarioName = game.getScenario().getName();

        BaseGridType type = baseGrid.getType();

        final String imagePrefix = baseGrid.getSide().getValue().toLowerCase();
        this.imageView = new VBox(imageResourceProvider.getImageView(scenarioName, imagePrefix + type.getValue() + ".png"));
        this.roundel = new VBox(imageResourceProvider.getImageView(scenarioName, imagePrefix + "Roundel12x12.png"));
        this.flag = new VBox(imageResourceProvider.getImageView(scenarioName, imagePrefix + "Flag18x12.png"));

        imageView.setLayoutX(gridView.getX());
        imageView.setLayoutY(gridView.getY());
        imageView.setViewOrder(ViewOrder.MARKER.getValue());
        imageView.setUserData(this);
        imageView.setId("map-base-grid-marker");

        InnerShadow innerShadow = new InnerShadow(SHADOW_RADIUS, Color.WHITE);
        imageView.setEffect(innerShadow);

        roundel.setLayoutX(gridView.getX() + props.getInt("main.map.base.marker.roudel.x.offset"));
        roundel.setLayoutY(gridView.getY() + 1);
        roundel.setViewOrder(ViewOrder.MARKER_DECORATION.getValue());
        roundel.setUserData(this);

        flag.setLayoutX(gridView.getX() + 1);
        flag.setLayoutY(gridView.getY() + props.getInt("main.map.base.marker.flag.y.offset"));
        flag.setViewOrder(ViewOrder.MARKER_DECORATION.getValue());
        flag.setUserData(this);

        title = buildTitle(gridView);

        patrolRadii = new PatrolRadii(mapView, baseGrid, gridView);


        if (baseGrid.getSide()  == game.getHumanSide()) {

            ContextMenu contextMenu = new ContextMenu();

            airfieldMenuItem = new MenuItem("Airfield...");
            airfieldMenuItem.setUserData(getBaseGrid().getAirfield());

            taskForceMenuItem = new MenuItem("Task Forces...");
            taskForceMenuItem.setDisable(true);

            contextMenu.getItems().addAll(airfieldMenuItem, taskForceMenuItem);

            imageView.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
            roundel.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
            flag.setOnContextMenuRequested(e -> contextMenu.show(imageView, e.getScreenX(), e.getScreenY()));
        }
    }

    /**
     * Draw the base marker.
     */
    public void draw() {
        mapView.add(imageView);

        if (areSquadronsPresent() && game.getHumanPlayer().getSide() == baseGrid.getSide()) {
            mapView.add(roundel);
        }

        if (areTaskForcesPresent() && game.getHumanPlayer().getSide() == baseGrid.getSide()) {
            mapView.add(flag);
        }
    }

    /**
     * This base marker has been selected.
     */
    public void selectMarker() {
        selected = !selected;

        if (selected) {
            imageView.setEffect(null);
            showTitle();
        } else {
            InnerShadow innerShadow = new InnerShadow(SHADOW_RADIUS, Color.WHITE);
            imageView.setEffect(innerShadow);
            hideTitle();
        }

        drawPatrolRadii();
    }

    /**
     * Draw this base marker's patrol range.
     */
    public void drawPatrolRadii() {

        if (selected) {
            patrolRadii.drawRadii();
        } else {
            patrolRadii.hideRadii();
        }
    }

    /**
     * Hide the base marker.
     */
    public void hide() {
        mapView.remove(imageView);
    }

    /**
     * Set the Base marker clicked handler.
     *
     * @param handler The mouse click event handler.
     */
    public void setBaseClickHandler(final EventHandler<? super MouseEvent> handler) {
        imageView.setOnMouseClicked(handler);
        roundel.setOnMouseClicked(handler);
        flag.setOnMouseClicked(handler);
    }

    /**
     * Set the base airfield menu item handler.
     *
     * @param handler The menu item handler.
     */
    public void setAirfieldMenuHandler(final EventHandler<ActionEvent> handler) {
        airfieldMenuItem.setOnAction(handler);
    }

    /**
     * Set the base patrol radius clicked handler.
     *
     * @param handler The mouse click handler.
     */
    public void setPatrolRadiusClickHandler(final EventHandler<? super MouseEvent> handler) {
        patrolRadii.setRadiusMouseHandler(handler);
    }

    /**
     * Highlight a patrol radius for this base.
     *
     * @param radius The radius to highlight.
     */
    public void highlightRadius(final int radius) {
        patrolRadii.highlightRadius(radius);
    }

    /**
     * Revmove this base's highlighted patrol radius.
     */
    public void unhighlightRadius() {
        patrolRadii.unhighlightRadius();
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

    /**
     * Determine if any task forces are present at this marker's port, if
     * this marker contains a port.
     *
     * @return True if this marker's port contains task forces. False otherwise.
     */
    private boolean areTaskForcesPresent() {
        return baseGrid
                .getPort()
                .map(Port::areTaskForcesPresent)
                .orElse(false);
    }

    /**
     * Build the base's title.
     *
     * @param gridView The grid view of this base.
     * @return A node containing the base's title.
     */
    private Node buildTitle(final GridView gridView) {
        Label label = new Label(baseGrid.getTitle());
        VBox vBox = new VBox(label);
        vBox.setLayoutY(gridView.getY() + gridView.getSize());
        vBox.setLayoutX(gridView.getX());
        vBox.setId("basemarker-title");
        return vBox;
    }

    /**
     * Show this base's title.
     */
    private void showTitle() {
        mapView.add(title);
    }

    /**
     * Hide this base's title.
     */
    private void hideTitle() {
        mapView.remove(title);
    }
}
