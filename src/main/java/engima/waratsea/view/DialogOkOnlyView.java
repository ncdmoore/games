package engima.waratsea.view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a dialog view.
 */
public class DialogOkOnlyView {
    @Getter
    private final Button okButton = new Button("Ok");

    @Getter
    private Scene scene;

    @Setter
    private Node contents;

    @Setter
    private String css;

    @Setter
    private int width;

    @Setter
    private int height;


    /**
     * Show the dialog.
     *
     * @param stage The stage of the dialog.
     */
    public void show(final Stage stage) {
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(contents);
        borderPane.setBottom(buildControlButtons());

        scene = new Scene(borderPane, width, height);

        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Build the control buttons.
     *
     * @return A node that contains the control buttons.
     */
    private Node buildControlButtons() {
        HBox hBox = new HBox(okButton);
        hBox.setAlignment(Pos.TOP_CENTER);
        hBox.setId("controls-pane");
        return hBox;
    }
}
