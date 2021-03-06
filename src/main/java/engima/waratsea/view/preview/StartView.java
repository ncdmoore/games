package engima.waratsea.view.preview;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ResourceProvider;

/**
 * This class contains the start view GUI components.
 */
public class StartView {
    private static final String CSS_FILE = "startView.css";

    @Getter private Button newButton;
    @Getter private Button savedButton;
    @Getter private Button quitButton;
    @Getter private Button optionsButton;

    private final ViewProps props;
    private final ResourceProvider resourceProvider;
    private final CssResourceProvider cssResourceProvider;

    /**
     * This is the constructor for the StartView.
     *
     * @param props the view properties.
     * @param resourceProvider Utility that provides resource images.
     * @param cssResourceProvider Utility that provides CSS files.
     */
    @Inject
    public StartView(final ViewProps props,
                     final ResourceProvider resourceProvider,
                     final CssResourceProvider cssResourceProvider) {

        this.props = props;
        this.resourceProvider = resourceProvider;
        this.cssResourceProvider = cssResourceProvider;
    }

    /**
     * Show the start view. Draw the start view.
     *
     * @param stage The stage that contains the start view.
     */
    public void show(final Stage stage) {
        int paneWidth = props.getInt("start.image.width");

        ImageView alliesFlag = resourceProvider.getImageView(props.getString("allies.flag.large.image"));
        ImageView axisFlag = resourceProvider.getImageView(props.getString("axis.flag.large.image"));

        HBox hBox = new HBox(alliesFlag, axisFlag);
        hBox.setId("flags");

        Label title = new Label(props.getString("title"));
        title.setId("start-title");

        StackPane titlePane = new StackPane(hBox, title);
        titlePane.setId("start-title-pane");
        titlePane.setMaxWidth(paneWidth);

        Node buttons = buildButtons();

        ImageView backGroundImageView = resourceProvider.getImageView(props.getString("start.image"));

        StackPane mainPane = new StackPane(backGroundImageView, buttons);
        mainPane.setId("start-main-pane");
        mainPane.setMaxWidth(paneWidth);

        VBox vBox = new VBox(titlePane, mainPane);

        int sceneWidth = props.getInt("start.scene.width");
        int sceneHeight = props.getInt("start.scene.height");

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        stage.setX(((primaryScreenBounds.getMaxX() - primaryScreenBounds.getMinX()) / 2) - (sceneWidth / 2.0));
        stage.setY(((primaryScreenBounds.getMaxY() - primaryScreenBounds.getMinY()) / 2) - (sceneHeight / 2.0));
        stage.setWidth(sceneWidth);
        stage.setHeight(sceneHeight);

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene
                .getStylesheets()
                .add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Build the game control buttons.
     *
     * @return A node that contains the game control buttons.
     */
    private Node buildButtons() {
        int buttonWidth = props.getInt("start.button.width");

        newButton = new Button("New Game");
        newButton.setMaxWidth(buttonWidth);
        newButton.setMinWidth(buttonWidth);

        savedButton = new Button("Saved Game");
        savedButton.setMaxWidth(buttonWidth);
        savedButton.setMinWidth(buttonWidth);

        optionsButton = new Button("Options");
        optionsButton.setMaxWidth(buttonWidth);
        optionsButton.setMinWidth(buttonWidth);

        quitButton = new Button("Quit Game");
        quitButton.setMaxWidth(buttonWidth);
        quitButton.setMinWidth(buttonWidth);

        VBox vBox = new VBox(newButton, savedButton, optionsButton, quitButton);
        vBox.setId("start-command-buttons");

        return vBox;
    }
}
