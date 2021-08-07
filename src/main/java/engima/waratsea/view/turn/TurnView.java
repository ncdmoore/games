package engima.waratsea.view.turn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.viewmodel.turn.TurnViewModel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

@Singleton
public class TurnView {
    @Getter private Button button;

    private final TurnViewModel viewModel;

    /**
     * THe constructor called by guice.
     *
     * @param viewModel The turn view model.
     */
    @Inject
    public TurnView(final TurnViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Build the weather.
     *
     * @return A node that contains the weather.
     */
    public Node build() {
        button = new Button("Next Turn");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Turn");

        ImageView imageView = new ImageView();
        imageView.imageProperty().bind(viewModel.getImage());

        Label number = new Label();
        number.textProperty().bind(viewModel.getNumber());

        Label type = new Label();
        type.textProperty().bind(viewModel.getType());

        Label index = new Label();
        index.textProperty().bind(viewModel.getTimeRange());

        Label date = new Label();
        date.textProperty().bind(viewModel.getDate());

        VBox imageBox = new VBox(imageView);
        VBox textBox = new VBox(date, number, type, index, button);
        textBox.getStyleClass().add("spacing-5");

        HBox mainBox = new HBox(textBox, imageBox);
        mainBox.setId("turn-pane");

        titledPane.setContent(mainBox);

        button.setOnAction(viewModel::nextTurn);

        return titledPane;
    }
}
