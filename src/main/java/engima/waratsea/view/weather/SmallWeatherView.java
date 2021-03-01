package engima.waratsea.view.weather;

import com.google.inject.Inject;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


/**
 * Represents a small weather icon. It also indicates if the current patrol or mission
 * is affected by the current weather.
 *
 * CSS sytle
 *
 *  .affected-weather-box
 *
 */
public class SmallWeatherView {
    private final BooleanProperty isAffected = new SimpleBooleanProperty(false);

    private final ResourceProvider resourceProvider;
    private final ViewProps props;
    private final Weather weather;

    private final Label label = new Label();

    @Inject
    public SmallWeatherView(final ResourceProvider resourceProvider,
                            final ViewProps props,
                            final Weather weather) {
        this.resourceProvider = resourceProvider;
        this.props = props;
        this.weather = weather;

        label.textProperty().bind(Bindings.createStringBinding(() -> isAffected.getValue() ? "Affected by Weather" : "No Weather Affect", isAffected));
        label.textFillProperty().bind(Bindings.createObjectBinding(() -> isAffected.getValue() ? Color.RED : Color.BLACK, isAffected));
    }

    /**
     * Build the weather image.
     *
     * @return The node containing the weather image.
     */
    public Node build() {
        ImageView image = resourceProvider.getImageView(props.getString(weather.getCurrent().toLower() + ".small.image"));
        VBox imageBox = new VBox(label, image);

        imageBox.setId("affected-weather-box");

        return imageBox;
    }

    /**
     * Set the is affected by weather property.
     *
     * @param isAffectedByWeather True if the current patrol or mission is affected by the weather. False otherwise.
     */
    public void bind(final BooleanProperty isAffectedByWeather) {
        isAffected.bind(isAffectedByWeather);
    }
}
