package engima.waratsea.view.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.presenter.map.SelectedMapGrid;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Defines the game's selected grid details view. Shown in the main game window.
 */
@Singleton
public class SelectedGridView {
    private final SelectedMapGrid selectedMapGrid;

    private final Label mapReferenceValue = new Label();
    private final Label nameValue = new Label();
    private final Label typeValue = new Label();
    private final CheckBox isAirfield = new CheckBox();
    private final CheckBox isPort = new CheckBox();
    private final ImageView airfieldImage;
    private final ImageView portImage;

    @Inject
    public SelectedGridView(final SelectedMapGrid selectedMapGrid,
                            final ViewProps props,
                            final ResourceProvider imageResourceProvider) {
        this.selectedMapGrid = selectedMapGrid;

        airfieldImage = imageResourceProvider.getImageView(props.getString("airfield.tiny.image"));
        portImage = imageResourceProvider.getImageView(props.getString("anchor.tiny.image"));

    }

    /**
     * Build the grid details view.
     *
     * @return The node containing the grid details view.
     */
    public Node build() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Selected Grid Details");

        isAirfield.setDisable(true);  // The checkbox is read only.
        isPort.setDisable(true);      // The checkbox is read only.

        Node content = buildGridPane();
        titledPane.setContent(content);

        bind();

        return titledPane;
    }

    private Node buildGridPane() {
        Node topPane = buildTopPane();
        Node bottomPane = buildBottomPane();
        VBox vBox = new VBox(topPane, bottomPane);
        vBox.setId("selected-grid-vbox");
        return vBox;
    }

    private Node buildTopPane() {
        Label mapReferenceTitle = new Label("Map:");
        Label nameTitle = new Label("Name:");
        Label typeTitle = new Label("Type:");

        GridPane pane = new GridPane();

        //CHECKSTYLE:OFF
        pane.add(mapReferenceTitle, 0, 0);
        pane.add(mapReferenceValue, 1, 0);
        pane.add(nameTitle, 0, 1);
        pane.add(nameValue, 1, 1);
        pane.add(typeTitle, 0, 2);
        pane.add(typeValue, 1, 2);
        //CHECKSTYPE:ON

        pane.setId("selected-grid-top-pane");

        return pane;
    }

    private Node buildBottomPane() {
        Label airfieldTitle = new Label("Airfield");
        Label portTitle = new Label("Port");

        GridPane pane = new GridPane();

        //CHECKSTYLE:OFF
        pane.add(airfieldTitle, 1, 3);
        pane.add(isAirfield, 0, 3);
        pane.add(airfieldImage, 2, 3);
        pane.add(portTitle, 1, 4);
        pane.add(isPort, 0, 4);
        pane.add(portImage, 2, 4);
        //CHECKSTYPE:ON

        pane.setId("selected-grid-bottom-pane");

        return pane;
    }

    /**
     * bind to the selected game grid's details.
     */
    public void bind() {
        mapReferenceValue.textProperty().bind(selectedMapGrid.getMapReference());
        nameValue.textProperty().bind(selectedMapGrid.getLocationName());
        typeValue.textProperty().bind(selectedMapGrid.getType());
        isAirfield.selectedProperty().bind(selectedMapGrid.getIsAirfield());
        isPort.selectedProperty().bind(selectedMapGrid.getIsPort());
    }
}
