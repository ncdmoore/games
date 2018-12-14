package engima.waratsea.view.map;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;

import java.util.List;

/**
 * Represents a marker popup on a map.
 */
public class PopUp {

    private static final int NAME_VBOX = 1;

    @Getter
    private final Marker marker;

    private final int xOffset;
    private EventHandler<? super MouseEvent> eventHandler;
    private VBox popUp;

    /**
     * Construct a popup for a given marker.
     * @param marker The marker that corresponds to this popUp.
     * @param xOffset The popups x offset from its marker.
     * @param eventHandler Handles mouse clicks for this popUp.
     */
    public PopUp(final Marker marker, final int xOffset, final EventHandler<? super MouseEvent> eventHandler) {
        this.marker = marker;
        this.xOffset = xOffset;
        this.eventHandler = eventHandler;
    }

    /**
     * Draw the popup. Keep it hidden at first.
     *
     * @param active indicates if the corresponding marker is active.
     */
    public void draw(final boolean active) {
        popUp = new VBox();

        Text mapRefText = new Text(marker.getMapRef());
        mapRefText.getStyleClass().add("popup-mapRef-text");
        VBox mapRefVbox = new VBox(mapRefText);
        mapRefVbox.getStyleClass().add("popup-mapRef");

        Text nameText = new Text(marker.getName());

        String textStyle = active ? "popup-name-text" : "popup-name-text-inactive";

        nameText.getStyleClass().add(textStyle);
        VBox nameVbox = new VBox(nameText);
        nameVbox.getStyleClass().add("popup-name");

        popUp.setOnMouseClicked(eventHandler);

        popUp.getChildren().addAll(mapRefVbox, nameVbox);
        popUp.setLayoutX(marker.getX() + xOffset);
        popUp.setLayoutY(marker.getY());
    }

    /** Add text to the popup.
     *
     * @param name The text to add.
     * @param active indicates if the corresponding marker is active.
     */
    public void addText(final String name, final boolean active) {
        Text text = new Text(name);

        String textStyle = active ? "popup-name-text" : "popup-name-text-inactive";

        text.getStyleClass().add(textStyle);

        List<Node> childern = popUp.getChildren();

        ((VBox) childern.get(NAME_VBOX)).getChildren().add(text);
    }

    /**
     * Display this popup.
     *
     * @param map The map that contains this pop up.
     */
    public void display(final Group map) {
        map.getChildren().add(popUp);
    }

    /**
     * Hide this pop up.
     *
     * @param map The map that contains this popup.
     */
    public void hide(final Group map) {
        map.getChildren().remove(popUp);
    }

    /**
     * Determine if the pop up is near the bottom of the map.
     *
     * @param yThreshold The y threshold for which popups are moved upward to avoid running off the bottom of the map.
     * @return True if the popup is near the bottom of the map.
     */
    public boolean isPopUpNearMapBotton(final int yThreshold) {
        return popUp.getLayoutY() > yThreshold;
    }

    /**
     * Move the marker's popup away from the bottom of the map.
     *
     * @param scale How much the y is adjusted per text item in the popup.
     **/
    public void adjustY(final int scale) {
        VBox vBox = (VBox) popUp.getChildren().get(NAME_VBOX);
        int size = vBox.getChildren().size();
        popUp.setLayoutY(popUp.getLayoutY() - (scale * size));
    }
}
