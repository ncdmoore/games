package engima.waratsea.view.map.marker.main;

import engima.waratsea.model.base.airfield.patrol.AswPatrol;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.SearchPatrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PatrolMarker {

    private final MapView mapView;
    private final GridView gridView;

    private Circle circle;

    @Getter
    private Label label;

    private VBox vBox;

    /**
     * Constructor.
     *
     * @param mapView The map view.
     * @param gridView The grid view.
     */
    public PatrolMarker(final MapView mapView, final GridView gridView) {
        this.mapView = mapView;
        this.gridView = gridView;
    }

    /**
     * Draw an individual Patrol's radius.
     *
     * @param gridRadius The radius of the patrol.
     * @param patrols A list of Patrols.
     */
    public void drawRadius(final int gridRadius, final List<Patrol> patrols) {
        int offset = gridView.getSize() / 2;

        int radius = gridRadius * gridView.getSize();

        circle = new Circle(gridView.getX() + offset, gridView.getY() + offset, radius);
        circle.setStroke(Color.BLACK);

        // Clip the circle with the main map rectangle to prevent the circles near the edges from flowing over the map
        // boundaries.
        ImageView mapImageView = mapView.getBackground();
        Image image = mapImageView.getImage();
        circle.setClip(new Rectangle(mapImageView.getX(), mapImageView.getY(), image.getWidth(), image.getHeight()));

        String id = getStyle(patrols);

        circle.setId(id);
        circle.setViewOrder(ViewOrder.RADIUS.getValue() + gridRadius);


        drawLabel(patrols);
    }

    /**
     * Add the patrol radius to the map.
     */
    public void add() {
        mapView.add(circle);
        mapView.add(vBox);
    }

    /**
     * Remove the patrol radius from the map.
     */
    public void remove() {
        mapView.remove(circle);
        mapView.remove(vBox);
    }

    /**
     * Determine if a patrol radius with the given radius exists.
     *
     * @param radius a given patrol radius.
     * @return True if this patrol's radius is equal to the given radius. False otherwise.
     */
    public boolean matches(final int radius) {
        return circle.getRadius() == radius;
    }

    /**
     * Set the patrol radius data.
     *
     * @param patrols The patrols that correspond to this patrol radius.
     */
    public void setData(final List<Patrol> patrols) {
        circle.setUserData(patrols);

        String text = patrols
                .stream()
                .map(PatrolType::getTitle)
                .collect(Collectors.joining("\n"));

        label.setText(text);
    }

    /**
     * Set the radius's click handler.
     *
     * @param handler The handler called when the radius is clicked.
     */
    public void setClickHandler(final EventHandler<? super MouseEvent> handler) {
        circle.setOnMouseClicked(handler);
    }

    /**
     * Determine the style of the circle. Since more than one patrol may be associated with a given circle.
     * We define a precedence order of styles. If a search patrol is present then search style is used. If
     * a ASW patrol is present (no search), then ASW style is used. If only a CAP patrol is present, then
     * a CAP patrol style is used.
     *
     * @param patrols A list of patrols that map to a given circle.
     * @return The name of the style for the circle that corresponds to the list of patrols.
     */
    private String getStyle(final List<Patrol> patrols) {
        String id = "patrol-radius-";
        if (patrols.stream().anyMatch(patrol -> patrol.getClass() == SearchPatrol.class)) {
            id += "search";
        } else if (patrols.stream().anyMatch(patrol -> patrol.getClass() == AswPatrol.class)) {
            id += "asw";
        } else {
            id += "cap";
        }

        return id;
    }

    /**
     * Draw the given circle's label.
     *
     * @param patrols The base's patrols.
     */
    private void drawLabel(final List<Patrol> patrols) {
        String text = patrols
                .stream()
                .map(PatrolType::getTitle)
                .collect(Collectors.joining("\n"));

        label = new Label(text);

        vBox = new VBox(label);
        vBox.setViewOrder(ViewOrder.POPUP.getValue());

        double offset = gridView.getSize() / 2.0;

        vBox.setLayoutX(circle.getCenterX() - offset);

        double y = circle.getCenterY() - circle.getRadius() + mapView.getGridSize() / 2.0;

        //Make sure the y coordinate is not off the map.
        if (y < 0) {
            y = mapView.getGridSize();
        }

        vBox.setLayoutY(y);
        vBox.setId("patrol-radius-label");
    }
}
