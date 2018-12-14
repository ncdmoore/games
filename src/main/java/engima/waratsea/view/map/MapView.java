package engima.waratsea.view.map;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represents a map view.
 */
@Slf4j
public class MapView {

    private int numberOfRows;
    private int numberOfColumns;
    private int gridSize;
    private int offset;
    private int yPopUpAdjust;
    private int yBottomThreshold;

    private Group map = new Group();

    private Map<String, Marker> markerMap = new HashMap<>();                //marker name -> grid.
    private Map<String, Marker> mapRefMarkerMap = new HashMap<>();          //map reference -> grid.
    private Map<String, PopUp> popUpMap = new HashMap<>();                 //map name -> popup.
    private Map<String, PopUp> mapRefPopUpMap = new HashMap<>();           //map reference -> popup.

    /**
     * The map view constructor.
     *
     * @param numColumns number of grid columns for this map.
     * @param numRows number of grid rows for this map.
     * @param gridsize size of the square grid.
     * @param yAdjust the scale by which pop ups are moved if they are close to the bottom of the map.
     * @param yThreshold the y threshold value in which the pop up is moved up to avoid running off the bottom of the map.
     */
    public void init(final int numColumns, final int numRows, final int gridsize, final int yAdjust, final int yThreshold) {

        numberOfColumns = numColumns;
        numberOfRows = numRows;
        gridSize = gridsize;
        yPopUpAdjust = yAdjust;
        yBottomThreshold = yThreshold;

        offset = gridSize / 2;
    }

    /**
     * Draws the map grid.
     *
     * @return The node containing the map grid.
     */
    public Node drawMapGrid() {

        int yOffset = 0;
        int currentNumberOfRows = numberOfRows;

        for (int col = 0; col < numberOfColumns; col++) {
            for (int row = 0; row < currentNumberOfRows; row++) {
                Node r = drawGrid(col, row, yOffset);
                map.getChildren().add(r);
            }

            yOffset = (yOffset == 0) ? offset : 0;
            currentNumberOfRows = (currentNumberOfRows == numberOfRows) ? numberOfRows - 1 : numberOfRows;
        }

        return map;
    }

    /**
     * Place a marker on the grid map.
     *
     * @param marker The marker placed on the map.
     */
    public void addMarker(final Marker marker) {
        log.info("addMarker {} {}, {}", new Object[]{marker.getName(), marker.getX(), marker.getY()});

        if (mapRefMarkerMap.containsKey(marker.getMapRef())) {
            Marker existingMarker = mapRefMarkerMap.get(marker.getMapRef());
            markerMap.put(marker.getName(), existingMarker);
        } else {
            marker.draw(map);
            mapRefMarkerMap.put(marker.getMapRef(), marker);
            markerMap.put(marker.getName(), marker);
        }
    }

    /**
     * Place a popUp on the map.
     *
      * @param popUp The popUp that is placed on the map.
     */
    public void addPopUp(final PopUp popUp) {

        String name = popUp.getMarker().getName();
        String mapRef = popUp.getMarker().getMapRef();
        boolean active = popUp.getMarker().isActive();

        log.info("addPopUp {} {} {},{} {}", new Object[]{name, mapRef, popUp.getMarker().getX(), popUp.getMarker().getY(), active});

        if (mapRefPopUpMap.containsKey(mapRef)) {
            PopUp existingPopUp = mapRefPopUpMap.get(mapRef);
            popUpMap.put(name, existingPopUp);
            existingPopUp.addText(name, active);
        } else {
            popUp.draw(active);
            mapRefPopUpMap.put(mapRef, popUp);
            popUpMap.put(name, popUp);
        }
    }

    /**
     * This method is called to adjust the y coordinate of the popup's that are near the bottom of the map.
     */
    public void finish() {
        popUpMap.values().stream().filter(p -> p.isPopUpNearMapBotton(yBottomThreshold))
                .forEach(p -> p.adjustY(yPopUpAdjust));
    }

    /**
     * Select a marker on the map.
     *
     * @param name specifies the marker to select.
     */
    public void selectMarker(final String name) {
        markerMap.get(name).select();
        popUpMap.get(name).display(map);
    }

    /**
     * Clear a marker selection on the map.
     *
     * @param name specifies the marker to clear.
     */
    public void clearMarker(final String name) {
        markerMap.get(name).clear();
        popUpMap.get(name).hide(map);

    }

    /**
     * Get the name of the marker from the markers grid.
     *
     * @param clickedMarker represents the marker.
     * @return A list of names associated with this marker.
     */
    public List<String> getNameFromMarker(final Object clickedMarker) {
        return markerMap.entrySet().stream().filter(e -> e.getValue().wasClicked(clickedMarker))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Draw a rectangle.
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @param yOffset The y offset.
     * @return A rectangle.
     */
    private Node drawGrid(final int x, final int y, final int yOffset) {

        final double opacity = 0.05;

        Rectangle r = new Rectangle(x * gridSize, y * gridSize + yOffset, gridSize, gridSize);
        r.setStroke(Color.BLACK);
        r.setFill(null);
        r.setOpacity(opacity);
        return r;
    }

    /**
     * Close the popup.
     *
     * @param event the mouse event.
     */
    public void closePopup(final MouseEvent event) {
        VBox o = (VBox) event.getSource();
        map.getChildren().remove(o);
    }

}
