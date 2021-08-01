package engima.waratsea.view.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.weather.WeatherViewModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Defines the main weather view of the game.
 */
@Singleton
public class WeatherView {

    private final ViewProps props;

    private final WeatherViewModel viewModel;

    /**
     * THe constructor called by guice.
     *
     * @param props The view properties.
     * @param viewModel The game's weather.
     */
    @Inject
    public WeatherView(final ViewProps props,
                       final WeatherViewModel viewModel) {
        this.props = props;
        this.viewModel = viewModel;
    }

    /**
     * Build the weather.
     *
     * @return A node that contains the weather.
     */
    public Node build() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Weather");

        Label label = new Label();
        label.textProperty().bind(viewModel.getWeatherValue());

        ImageView icon = new ImageView();
        icon.imageProperty().bind(viewModel.getImage());

        VBox vBox = new VBox(label, icon);

        vBox.setMaxWidth(props.getInt("main.left.side.width"));
        vBox.setMinWidth(props.getInt("main.left.side.width"));

        vBox.setId("weather-pane");

        titledPane.setContent(vBox);

        return titledPane;
    }
}
