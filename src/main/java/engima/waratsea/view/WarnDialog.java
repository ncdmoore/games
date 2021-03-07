package engima.waratsea.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ResourceProvider;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Singleton
public class WarnDialog {
    private static final String CSS_FILE = "warn.css";

    private final CssResourceProvider cssResourceProvider;
    private final ResourceProvider resourceProvider;
    private final ViewProps props;

    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider provides css files.
     * @param resourceProvider provides images.
     * @param props View properties.
     */
    @Inject
    public WarnDialog(final CssResourceProvider cssResourceProvider,
                      final ResourceProvider resourceProvider,
                      final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.resourceProvider = resourceProvider;
        this.props = props;
    }

    /**
     * Shows the fatal error dialog box.
     *
     * @param message The message displayed in the fatal error dialog box.
     */
    public void show(final String message) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Warning");
        stage.setMinWidth(props.getInt("warn.dialog.stage.width"));

        ImageView imageView = resourceProvider.getImageView(props.getString("warn.small.icon"));
        Label label = new Label(message);

        HBox hBox = new HBox(imageView, label);
        hBox.setId("warn-hbox");

        Button ok = new Button("Ok");
        ok.setOnAction(event -> close(stage));

        VBox vBox = new VBox(hBox, ok);
        vBox.setId("warn-main-pane");

        int width = props.getInt("warn.dialog.scene.width");
        int height = props.getInt("warn.dialog.scene.height");
        Scene scene = new Scene(vBox, width, height);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Call back for the ok button.
     *
     * @param stage The dialog box's stage.
     */
    private void close(final Stage stage) {
        stage.close();
    }

}
