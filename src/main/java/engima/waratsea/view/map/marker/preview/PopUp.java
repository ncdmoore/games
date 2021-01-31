package engima.waratsea.view.map.marker.preview;

import engima.waratsea.presenter.dto.map.PopUpDTO;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MapView;
import engima.waratsea.view.map.ViewOrder;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a marker popup on a map.
 */
@Slf4j
public class PopUp {

    private static final int NAME_VBOX = 1;

    private final String mapRef;
    private final GridView gridView;
    private final int xOffset;
    private final String style;
    private final EventHandler<? super MouseEvent> eventHandler;

    private final VBox popUp = new VBox();
    private final List<String> names = new ArrayList<>();

    private final Map<String, Label> namesMap = new HashMap<>();

    /**
     * Construct a popup for a given marker.
     *
     * @param dto The data for the task force marker and associated popup.
     */
    public PopUp(final PopUpDTO dto) {
        this.names.add(dto.getName());
        this.mapRef = dto.getReference();
        this.gridView = dto.getGridView();
        this.xOffset = dto.getXOffset();
        this.style = dto.getStyle();
        this.eventHandler = dto.getPopupEventHandler();
    }

    /**
     * Draw the popup. Keep it hidden at first.
     * @param dto The popup data transfer object.
     */
    public void draw(final PopUpDTO dto) {
        log.debug("Draw the popup text: {}", names.get(0));

        Text mapRefText = new Text(mapRef);
        mapRefText.getStyleClass().add("popup-mapRef-text");
        VBox mapRefVbox = new VBox(mapRefText);
        mapRefVbox.getStyleClass().add("popup-mapRef");

        Label nameText = new Label(dto.getTitle());

        namesMap.put(names.get(0), nameText);

        String textStyle = dto.isActive() ? "popup-text" : "popup-text-inactive";

        nameText.getStyleClass().add(textStyle);
        VBox nameVbox = new VBox(nameText);
        nameVbox.getStyleClass().add(style);

        popUp.setOnMouseClicked(eventHandler);

        popUp.getChildren().addAll(mapRefVbox, nameVbox);
        popUp.setLayoutX(gridView.getX() + xOffset);
        popUp.setLayoutY(gridView.getY());
        popUp.setViewOrder(ViewOrder.POPUP.getValue());
    }

    /**
     * Add text to the popup.
     *
     * @param dto The popup data transfer object.
     */
    public void addText(final PopUpDTO dto) {
        String name = dto.getName();

        log.debug("Add text: {} to the popup", name);

        names.add(name);

        Label text = new Label(dto.getTitle());

        namesMap.put(name, text);  // Keep a reference to the label.

        String textStyle = dto.isActive() ? "popup-text" : "popup-text-inactive";
        text.getStyleClass().add(textStyle);
        List<Node> children = popUp.getChildren();

        VBox nameVBox = (VBox) children.get(NAME_VBOX);

        List<Node> sortedLabels = getSortedLabels();

        nameVBox.getChildren().clear();              // Remove all labels so they can be re-added sorted, including the current label.
        nameVBox.getChildren().addAll(sortedLabels); // Note, the current label was already stored in the namesMap.
    }

    /**
     * Remove text from the popup.
     *
     * @param dto The popup data transfer object.
     */
    public void removeText(final PopUpDTO dto) {
        String name = dto.getName();

        Label text = namesMap.get(name);

        List<Node> children = popUp.getChildren();

        VBox nameVBox = (VBox) children.get(NAME_VBOX);

        names.remove(name);
        namesMap.remove(name);
        nameVBox.getChildren().remove(text);
    }

    /**
     * Store user data on the popup.
     *
     * @param data The user data to store.
     */
    public void setUserData(final Object data) {
        popUp.setUserData(data);
    }

    /**
     * Display this popup.
     * @param map The map that contains this popup.
     */
    public void display(final MapView map) {
        map.remove(popUp);
        map.add(popUp);
    }

    /**
     * Display this popup and highlight the given name.
     * @param map The map that contains this popup.
     * @param name The text in the popup to highlight.
     */
    public void display(final MapView map, final String name) {
        namesMap.forEach((key, value) -> value.setBackground(null));

        map.remove(popUp);

        if (names.size() > 1) {
            //Unable to use css as this is dynamic and the css never takes.
            namesMap.get(name).setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
        }

        map.add(popUp);
    }

    /**
     * Hide this pop up.
     * @param map The map that contains this popup.
     */
    public void hide(final MapView map) {
        map.remove(popUp);
    }

    /**
     * Move the marker's popup away from the bottom of the map.
     *
     * @param yOffset How much the y is adjusted once.
     * @param yThreshold Determines if the popup is too close to the bottom and needs to be moved up.
     **/
    public void adjustY(final int yOffset, final int yThreshold) {
        if (getY() > yThreshold) {
            popUp.setLayoutY(getY() -  yOffset);
        }
    }

    /**
     * Returns the size of the popup.
     *
     * @return The size of the popup.
     */
    public int size() {
        return names.size();
    }

    /**
     * Get the y-coordinate of the popup.
     *
     * @return The y-coordinate of the popup.
     */
    public double getY() {
        return popUp.getLayoutY();
    }

    /**
     * Get a sorted list of label nodes contained in the popup.
     *
     * @return A sorted list of label nodes. Sorted by the text in the label.
     */
    private List<Node> getSortedLabels() {
        List<String> sortedLabelNames = namesMap
                .keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        return sortedLabelNames
                .stream()
                .map(namesMap::get)
                .collect(Collectors.toList());
    }
}
