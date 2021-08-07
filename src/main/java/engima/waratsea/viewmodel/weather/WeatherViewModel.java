package engima.waratsea.viewmodel.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.event.scenario.ScenarioEvent;
import engima.waratsea.model.game.event.turn.TurnEvent;
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

        update();

        ScenarioEvent.register(this, this::handleScenarioEvent);
    }

    /**
     * Update the weather due to a scenario event.
     *
     * @param event The scenario event.
     */
    private void handleScenarioEvent(final ScenarioEvent event) {
        log.debug("Update weather view model");
        // The game has started. Register for turn events. This cannot be done in the constructor
        // since this class is a singleton. Every time a game starts the handlers are reset (except
        // for ScenarioEvent handlers). Thus, we need to register here so that we register with
        // every new game started.
        TurnEvent.register(this, this::handleTurnEvent);
        update();
    }

    /**
     * Update the weather.
     *
     * @param event The turn event.
     */
    private void handleTurnEvent(final TurnEvent event) {
        log.debug("Update weather view model");
        update();
    }

    private void update() {
        weatherValue.setValue(weather.getCurrent().toString());
        image.setValue(resourceProvider.getImage(props.getString(weather.getCurrent().toLower() + ".image")));
    }
}
