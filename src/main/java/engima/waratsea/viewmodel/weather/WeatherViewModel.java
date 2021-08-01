package engima.waratsea.viewmodel.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class WeatherViewModel {
    private final ResourceProvider resourceProvider;
    private final ViewProps props;

    private final Weather weather;

    @Getter private final StringProperty weatherValue = new SimpleStringProperty();
    @Getter private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    @Inject
    public WeatherViewModel(final Weather weather,
                            final ResourceProvider resourceProvider,
                            final ViewProps props) {
        this.weather = weather;
        this.resourceProvider = resourceProvider;
        this.props = props;

        weatherValue.setValue(weather.getCurrent().toString());
        image.setValue(resourceProvider.getImage(props.getString(weather.getCurrent().toLower() + ".image")));
    }

    /**
     * Update the weather.
     */
    public void update() {

        log.info("Update weather view model");

        weatherValue.setValue(weather.getCurrent().toString());
        image.setValue(resourceProvider.getImage(props.getString(weather.getCurrent().toLower() + ".image")));
    }
}
