package engima.waratsea.view.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
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

    private final ResourceProvider resourceProvider;
    private final ViewProps props;

    private final Weather weather;

    /**
     * THe constructor called by guice.
     *
     * @param resourceProvider The image resource provider.
     * @param props The view properties.
     * @param weather The game's weather.
     */
    @Inject
    public WeatherView(final ResourceProvider resourceProvider,
                       final ViewProps props,
                       final Weather weather) {
        this.resourceProvider = resourceProvider;
        this.props = props;
        this.weather = weather;
    }

    /**
     * Build the weather.
     *
     * @return A node that contains the weather.
     */
    public Node build() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Weather");

        Label label = new Label(weather.getCurrent().toString());

        ImageView icon = resourceProvider.getImageView(props.getString(weather.getCurrent().toLower() + ".image"));

        VBox vBox = new VBox(label, icon);

        vBox.setMaxWidth(props.getInt("main.left.side.width"));
        vBox.setMinWidth(props.getInt("main.left.side.width"));

        vBox.setId("weather-pane");

        titledPane.setContent(vBox);

        return titledPane;
    }
}
