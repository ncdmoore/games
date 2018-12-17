package engima.waratsea.view.map;

import engima.waratsea.presenter.dto.map.PopUpDTO;
import javafx.event.EventHandler;
import javafx.scene.Group;
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

    private VBox popUp = new VBox();
    private List<String> names = new ArrayList<>();

    private Map<String, Label> namesMap = new HashMap<>();

    /**
     * Construct a popup for a given marker.
     * @param dto The data for the task force marker and associated popup.
     */
    public PopUp(final PopUpDTO dto) {
        this.names.add(dto.getText());
        this.mapRef = dto.getMapReference();
        this.gridView = dto.getGridView();
        this.xOffset = dto.getXOffset();
        this.style = dto.getStyle();
        this.eventHandler = dto.getPopupEventHandler();
    }

    /**
     * Draw the popup. Keep it hidden at first.
     * @param active indicates if the corresponding marker is active.
     */
    public void draw(final boolean active) {
        log.info("Draw the popup text: {}", names.get(0));

        Text mapRefText = new Text(mapRef);
        mapRefText.getStyleClass().add("popup-mapRef-text");
        VBox mapRefVbox = new VBox(mapRefText);
        mapRefVbox.getStyleClass().add("popup-mapRef");

        Label nameText = new Label(names.get(0));

        namesMap.put(names.get(0), nameText);

        String textStyle = active ? "popup-text" : "popup-text-inactive";

        nameText.getStyleClass().add(textStyle);
        VBox nameVbox = new VBox(nameText);
        nameVbox.getStyleClass().add(style);

        popUp.setOnMouseClicked(eventHandler);

        popUp.getChildren().addAll(mapRefVbox, nameVbox);
        popUp.setLayoutX(gridView.getX() + xOffset);
        popUp.setLayoutY(gridView.getY());
    }

    /** Add text to the popup.
     * @param name The text to add.
     * @param active indicates if the corresponding marker is active.
     */
    public void addText(final String name, final boolean active) {
        log.info("Add text: {} to the popup", name);

        names.add(name);

        Label text = new Label(name);

        namesMap.put(name, text);

        String textStyle = active ? "popup-text" : "popup-text-inactive";
        text.getStyleClass().add(textStyle);
        List<Node> childern = popUp.getChildren();
        ((VBox) childern.get(NAME_VBOX)).getChildren().add(text);
    }

    /**
     * Display this popup.
     * @param map The map that contains this popup.
     */
    public void display(final Group map) {
        map.getChildren().remove(popUp);
        map.getChildren().add(popUp);
    }

    /**
     * Display this popup and highlight the given name.
     * @param map The map that contains this popup.
     * @param name The text in the popup to highlight.
     */
    public void display(final Group map, final String name) {
        namesMap.forEach((key, value) -> value.setBackground(null));

        map.getChildren().remove(popUp);

        if (names.size() > 1) {
            //Unable to use css as this is dynamic and the css never takes.
            namesMap.get(name).setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
        }

        map.getChildren().add(popUp);
    }

    /**
     * Hide this pop up.
     * @param map The map that contains this popup.
     */
    public void hide(final Group map) {
        map.getChildren().remove(popUp);
    }

    /**
     * Move the marker's popup away from the bottom of the map.
     * @param scale How much the y is adjusted per text item in the popup.
     **/
    public void adjustY(final int scale) {
        VBox vBox = (VBox) popUp.getChildren().get(NAME_VBOX);
        int size = vBox.getChildren().size();
        popUp.setLayoutY(popUp.getLayoutY() - (scale * size));
    }

    /**
     * Get the y-coordinate of the popup.
     * @return The y-coordinate of the popup.
     */
    public double getY() {
        return popUp.getLayoutY();
    }
}
