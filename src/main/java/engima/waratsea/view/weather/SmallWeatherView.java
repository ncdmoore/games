package engima.waratsea.view.weather;

import com.google.inject.Inject;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class SmallWeatherView {

    private final ImageResourceProvider imageProvider;
    private final ViewProps props;
    private final Weather weather;

    @Inject
    public SmallWeatherView(final ImageResourceProvider imageProvider,
                            final ViewProps props,
                            final Weather weather) {
        this.imageProvider = imageProvider;
        this.props = props;
        this.weather = weather;
    }

    /**
     * Build the weather image.
     *
     * @param affectedByWeather Indicates if the weather view should be shown as affecting the parent or not.
     * @return The node containing the weather image.
     */
    public Node build(final boolean affectedByWeather) {
        String text = affectedByWeather ? "Affected by Weather" : "No Weather Affect";
        Label label = new Label(text);
        Paint paint = affectedByWeather ? Color.RED : Color.BLACK;
        label.setTextFill(paint);

        ImageView image = imageProvider.getImageView(props.getString(weather.getCurrent().toLower() + ".small.image"));
        VBox imageBox = new VBox(label, image);

        imageBox.setId("patrol-weather-box");

        return imageBox;
    }
}
