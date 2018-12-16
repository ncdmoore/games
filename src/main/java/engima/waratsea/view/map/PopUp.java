package engima.waratsea.view.map;

import engima.waratsea.presenter.map.TaskForceMarkerDTO;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a marker popup on a map.
 */
public class PopUp {

    private static final int NAME_VBOX = 1;

    private final String mapRef;
    private final GridView gridView;
    private final int xOffset;
    private final EventHandler<? super MouseEvent> eventHandler;

    private VBox popUp = new VBox();
    private List<String> names = new ArrayList<>();

    /**
     * Construct a popup for a given marker.
     * @param dto The data for the task force marker and associated popup.
     */
    public PopUp(final TaskForceMarkerDTO dto) {
        this.names.add(dto.getName());
        this.mapRef = dto.getMapReference();
        this.gridView = dto.getGridView();
        this.xOffset = dto.getXOffset();
        this.eventHandler = dto.getPopupEventHandler();
    }

    /**
     * Draw the popup. Keep it hidden at first.
     *
     * @param active indicates if the corresponding marker is active.
     */
    public void draw(final boolean active) {

        Text mapRefText = new Text(mapRef);
        mapRefText.getStyleClass().add("popup-mapRef-text");
        VBox mapRefVbox = new VBox(mapRefText);
        mapRefVbox.getStyleClass().add("popup-mapRef");

        Text nameText = new Text(names.get(0));

        String textStyle = active ? "popup-name-text" : "popup-name-text-inactive";

        nameText.getStyleClass().add(textStyle);
        VBox nameVbox = new VBox(nameText);
        nameVbox.getStyleClass().add("popup-name");

        popUp.setOnMouseClicked(eventHandler);

        popUp.getChildren().addAll(mapRefVbox, nameVbox);
        popUp.setLayoutX(gridView.getX() + xOffset);
        popUp.setLayoutY(gridView.getY());
    }

    /** Add text to the popup.
     *
     * @param name The text to add.
     * @param active indicates if the corresponding marker is active.
     */
    public void addText(final String name, final boolean active) {
        names.add(name);

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
     * Move the marker's popup away from the bottom of the map.
     *
     * @param scale How much the y is adjusted per text item in the popup.
     **/
    public void adjustY(final int scale) {
        VBox vBox = (VBox) popUp.getChildren().get(NAME_VBOX);
        int size = vBox.getChildren().size();
        popUp.setLayoutY(popUp.getLayoutY() - (scale * size));
    }

    /**
     * Get the y-coordinate of the popup.
     *
     * @return The y-coordinate of the popup.
     */
    public double getY() {
        return popUp.getLayoutY();
    }
}
