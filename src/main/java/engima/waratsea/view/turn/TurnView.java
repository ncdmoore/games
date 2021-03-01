package engima.waratsea.view.turn;

import com.google.inject.Inject;
import engima.waratsea.model.game.Turn;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;

public class TurnView {

    private final ResourceProvider resourceProvider;
    private final ViewProps props;

    private final Turn turn;

    /**
     * THe constructor called by guice.
     *
     * @param resourceProvider The image resource provider.
     * @param props The view properties.
     * @param turn The game's weather.
     */
    @Inject
    public TurnView(final ResourceProvider resourceProvider,
                    final ViewProps props,
                    final Turn turn) {
        this.resourceProvider = resourceProvider;
        this.props = props;
        this.turn = turn;
    }

    /**
     * Build the weather.
     *
     * @return A node that contains the weather.
     */
    public Node build() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Turn");

        ImageView imageView = resourceProvider.getImageView(props.getString(turn.getType().toLower() + ".image"));

        HBox hBox = new HBox(imageView);
        hBox.setId("turn-image-pane");

        Label number = new Label("Turn: " + turn.getTurn());

        Label type = new Label("Type: " + turn.getType());
        Label index = new Label("Time: " + turn.getIndex().getTimeRange());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
        String dateString = simpleDateFormat.format(turn.getDate());

        Label date = new Label("Date: " + dateString);

        VBox vBox = new VBox(date, number, type, index);

        StackPane stackPane = new StackPane(vBox, hBox);
        stackPane.setMaxWidth(props.getInt("main.left.side.width"));
        stackPane.setMinWidth(props.getInt("main.left.side.width"));
        stackPane.setId("turn-pane");

        titledPane.setContent(stackPane);

        return titledPane;
    }
}
