package engima.waratsea.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import engima.waratsea.utility.CssResourceProvider;

@Singleton
public class FatalErrorDialog {
    private static final String CSS_FILE = "fatalError.css";

    private CssResourceProvider cssResourceProvider;

    /**
     * Constructor.
     * @param cssResourceProvider provides css files.
     */
    @Inject
    public FatalErrorDialog(final CssResourceProvider cssResourceProvider) {
        this.cssResourceProvider = cssResourceProvider;
    }

    /**
     * Shows the fatal error dialog box.
     * @param message The message displayed in the fatal error dialog box.
     */
    public void show(final String message) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Fatal Error");
        stage.setMinWidth(250);

        Label label = new Label(message);


        Button ok = new Button("Ok");
        ok.setOnAction(event -> close(stage));

        VBox vBox = new VBox(20, label, ok);
        vBox.setId("main-pane");

        Scene scene = new Scene(vBox, 800, 200);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Call back for the ok button.
     * @param stage The dialog box's stage.
     */
    private void close(final Stage stage) {
        stage.close();
        Platform.exit();
    }

}
