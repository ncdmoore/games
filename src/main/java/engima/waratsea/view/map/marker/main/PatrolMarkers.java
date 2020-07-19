package engima.waratsea.view.map.marker.main;

import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.map.MarkerGrid;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Draws the patrol radii around a map marker such as a airfield or task force with aircraft carriers.
 */
@Slf4j
public class PatrolMarkers {
    @Getter private List<PatrolMarker> patrolMarkers = Collections.emptyList();
    @Setter private EventHandler<? super MouseEvent> radiusMouseHandler;

    private final MapView mapView;
    private final MarkerGrid markerGrid;
    private final GridView gridView;       // The base's grid view.

    private Circle highlighted;

    /**
     * Constructor.
     *
     * @param mapView The map view.
     * @param markerGrid The base grid of the patrol radii.
     * @param gridView The grid view of the base grid.
     */
    public PatrolMarkers(final MapView mapView, final MarkerGrid markerGrid, final GridView gridView) {
        this.mapView = mapView;
        this.markerGrid = markerGrid;
        this.gridView = gridView;
    }

    /**
     * Draw all the marker's patrol radii.
     */
    public void draw() {
        List<PatrolMarker> newMarkers = markerGrid
                .getPatrols()
                .map(patrolMap -> patrolMap
                        .entrySet()
                        .stream()
                        .filter(this::filterZeroRadius)
                        .map(this::drawMarker)
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);

        // Get any circles that are no longer needed.
        List<PatrolMarker> removed = ListUtils.subtract(patrolMarkers, newMarkers);

        // Remove the unneeded circles.
        removed.forEach(PatrolMarker::remove);

        patrolMarkers = newMarkers;
    }

    /**
     * Remove the circle representing the patrols's radius from the map.
     */
    public void hide() {
        patrolMarkers.forEach(PatrolMarker::remove);
    }

    /**
     * Highlight the base's patrol radius.
     *
     * @param radius The radius to highlight.
     */
    public void highlightRadius(final int radius) {
        drawHighlightedRadius(radius);
    }

    /**
     * Remove the base's highlighted patrol radius.
     */
    public void unhighlightRadius() {
        removeHighlightedRadius();
    }

    /**
     * Don't draw any circles with with zero radius.
     *
     * @param entry An entry in the airbase's patrol map. It contains the max radius -> List of Patrols.
     * @return True if the patrols radius is not zero. False if the patrols radius is zero.
     */
    private boolean filterZeroRadius(final Map.Entry<Integer, List<Patrol>> entry) {
        return entry.getKey() != 0;
    }

    /**
     * Draw the patrol marker's radius circle.
     *
     * @param entry A map entry of circle's radius => list of patrols.
     * @return The circle representing the patrols radius.
     */
    private PatrolMarker drawMarker(final Map.Entry<Integer, List<Patrol>> entry) {

        int radius = entry.getKey() * gridView.getSize();

        // Either get an existing radius or draw a new radius.
        PatrolMarker patrolMarker = patrolMarkers
                .stream()
                .filter(existingPatrolRadius -> existingPatrolRadius.matches(radius))
                .findAny()
                .orElseGet(() -> buildPatrolMarker(entry));

        patrolMarker.add();
        patrolMarker.setData(entry.getValue());
        patrolMarker.setClickHandler(radiusMouseHandler);

        return patrolMarker;
    }

    /**
     * Build a new patrol radius marker for this base.
     *
     * @param entry An entry in the airbase's patrol map. It contains the max radius -> List of Patrols.
     * @return The new patrol radius.
     */
    private PatrolMarker buildPatrolMarker(final Map.Entry<Integer, List<Patrol>> entry) {
        PatrolMarker patrolMarker = new PatrolMarker(mapView, gridView);
        patrolMarker.drawRadius(entry.getKey(), entry.getValue());
        return patrolMarker;
    }

    /**
     * Draw the base's highlighted range circle.
     *
     * @param gridRadius The radius in grids of the highlighted circle.
     */
    private void drawHighlightedRadius(final int gridRadius) {
        int offset = gridView.getSize() / 2;

        int radius = gridRadius * gridView.getSize();

        Circle circle = new Circle(gridView.getX() + offset, gridView.getY() + offset, radius);
        circle.setStroke(Color.RED);
        circle.setFill(null);

        // Clip the circle with the main map rectangle to prevent the circles near the edges from flowing over the map
        // boundaries.
        ImageView mapImageView = mapView.getBackground();
        Image image = mapImageView.getImage();
        circle.setClip(new Rectangle(mapImageView.getX(), mapImageView.getY(), image.getWidth(), image.getHeight()));

        mapView.add(circle);

        highlighted = circle;
    }

    /**
     * Remove the base's highlighted range circle.
     */
    private void removeHighlightedRadius() {
        Optional
                .ofNullable(highlighted)
                .ifPresent(mapView::remove);
    }
}
