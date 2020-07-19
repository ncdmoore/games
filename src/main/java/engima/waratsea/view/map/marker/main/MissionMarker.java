package engima.waratsea.view.map.marker.main;

import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import lombok.Getter;

import java.util.List;

/**
 * Represents an individual mission's marker.
 */
public class MissionMarker {
    private final MapView mapView;
    private final GridView originationGridView;        // The base or task force grid view.
    private final GridView targetGridView;

    private Path arrow;
    private VBox vBox;

    @Getter
    private Label label;

    /**
     * Constructor.
     *
     * @param mapView The map view.
     * @param originationGridView The mission's origination grid view.
     * @param targetGridView The target's gird view.
     */
    public MissionMarker(final MapView mapView, final GridView originationGridView, final GridView targetGridView) {
        this.mapView = mapView;
        this.originationGridView = originationGridView;
        this.targetGridView = targetGridView;
    }

    /**
     * Draw the mission marker.
     *
     * @param missions The missions that are included in the marker. A base or task force may have missions
     *                 multiple missions that target the same destination grid.
     */
    public void draw(final List<AirMission> missions) {
        arrow = new Path();

        final double arrowAngle = 15.4;
        final double arrowLength = 15.0;

        double offset = originationGridView.getSize() / 2.0;

        double startX = originationGridView.getX() + offset;
        double startY = originationGridView.getY() + offset;

        double endX = targetGridView.getX() + offset;
        double endY = targetGridView.getY() + offset;

        double xDelta = endX - startX;
        double yDelta = endY - startY;

        double lineLength = Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
        double l = arrowLength / lineLength;

        double x3 = endX + l * (xDelta * Math.cos(arrowAngle) + yDelta * Math.sin(arrowAngle));
        double y3 = endY + l * (yDelta * Math.cos(arrowAngle) - xDelta * Math.sin(arrowAngle));

        double x4 = endX + l * (xDelta * Math.cos(arrowAngle) - yDelta * Math.sin(arrowAngle));
        double y4 = endY + l * (yDelta * Math.cos(arrowAngle) + xDelta * Math.sin(arrowAngle));

        MoveTo orig = new MoveTo();
        orig.setX(startX);
        orig.setY(startY);

        LineTo dest = new LineTo();
        dest.setX(endX);
        dest.setY(endY);

        arrow.getElements().add(orig);
        arrow.getElements().add(dest);

        arrow.getElements().add(new LineTo(x3, y3));
        arrow.getElements().add(new LineTo(x4, y4));
        arrow.getElements().add(new LineTo(endX, endY));

        arrow.setId("mission-arrow");
        arrow.setUserData(missions);
    }

    /**
     * Add the arrow to the map.
     */
    public void add() {
        mapView.add(arrow);
    }

    /**
     * Remove the arrow from the map.
     */
    public void remove() {
        mapView.remove(arrow);
    }

    /**
     * Set the radius's click handler.
     *
     * @param handler The handler called when the radius is clicked.
     */
    public void setClickHandler(final EventHandler<? super MouseEvent> handler) {
        arrow.setOnMouseClicked(handler);
    }

}
