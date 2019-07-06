package engima.waratsea.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
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

    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider provides css files.
     * @param imageResourceProvider provides images.
     */
    @Inject
    public WarnDialog(final CssResourceProvider cssResourceProvider,
                      final ImageResourceProvider imageResourceProvider) {
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
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
        stage.setMinWidth(250);

        ImageView imageView = imageResourceProvider.getImageView("warnIcon30x28.png");
        Label label = new Label(message);

        HBox hBox = new HBox(imageView, label);
        hBox.setId("hbox");

        Button ok = new Button("Ok");
        ok.setOnAction(event -> close(stage));

        VBox vBox = new VBox(20, hBox, ok);
        vBox.setId("main-pane");

        Scene scene = new Scene(vBox, 800, 150);

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
