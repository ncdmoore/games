package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.map.MainMapView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The main game window's view.
 */
public class MainView {
    private static final String CSS_FILE = "taskForceView.css";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private MainMapView mainMapView;

    /**
     * Constructor called by guice.
     * @param props The view properties.
     * @param cssResourceProvider Utility to provide css files.
     * @param mainMapView The main game map view.
     */
    @Inject
    public MainView(final ViewProps props,
                    final CssResourceProvider cssResourceProvider,
                    final MainMapView mainMapView) {
        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.mainMapView = mainMapView;
    }


    /**
     * Show the task forces summary view.
     * @param stage The stage on which the task force scene is set.
     */
    public void show(final Stage stage) {

        Node map = mainMapView.build();

        VBox popup = new VBox(new Label("popup"));
        popup.setLayoutX(100);
        popup.setLayoutY(100);
        mainMapView.addPopup(popup);

        VBox vBox = new VBox(map);

        int sceneWidth = props.getInt("taskForce.scene.width");
        int sceneHeight = props.getInt("taskForce.scene.height");

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }
}
