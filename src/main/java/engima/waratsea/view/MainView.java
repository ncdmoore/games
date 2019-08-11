package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.map.MainMapView;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * The main game window's view.
 */
@Slf4j
public class MainView {
    private static final String CSS_FILE = "mainView.css";

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

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());

        Node map = mainMapView.build();

        VBox vBox = new VBox(map);

        Scene scene = new Scene(vBox, primaryScreenBounds.getWidth(), primaryScreenBounds.getHeight());

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }
}
