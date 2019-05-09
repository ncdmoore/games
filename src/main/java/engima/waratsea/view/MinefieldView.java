package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.CssResourceProvider;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;

/**
 * Represents the minefield view.
 * This allows the user to place minefields in certain sea zones.
 */
public class MinefieldView {
    private static final String CSS_FILE = "minefieldView.css";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;


    @Getter
    private Button continueButton = new Button("Continue");

    @Getter
    private Button backButton = new Button("Back");

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param cssResourceProvider Provides access to the css file.
     */
    @Inject
    public MinefieldView(final ViewProps props,
                         final CssResourceProvider cssResourceProvider) {
        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
    }

    /**
     * Show the minefield view.
     *
     * @param stage The stage on which the minefield scene is set.
     * @param scenario The selected scenario.
     */
    public void show(final Stage stage, final Scenario scenario) {
        Label title = new Label("Minefields: " + scenario.getTitle());
        title.setId("title");
        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Node pushButtons = buildPushButtons();

        //Node map = taskForceMap.draw();

        //HBox mapPane = new HBox(taskForceList, map);
        //mapPane.setId("map-pane");


        VBox vBox = new VBox(titlePane, pushButtons);

        int sceneWidth = props.getInt("taskForce.scene.width");
        int sceneHeight = props.getInt("taskForce.scene.height");

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }

    /**
     * build the mine field push buttons.
     *
     * @return Node containing the push buttons.
     */
    private Node buildPushButtons() {
        HBox hBox =  new HBox(backButton, continueButton);
        hBox.setId("push-buttons");
        return hBox;
    }
}
