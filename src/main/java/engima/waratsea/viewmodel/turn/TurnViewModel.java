package engima.waratsea.viewmodel.turn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Turn;
import engima.waratsea.model.game.event.scenario.ScenarioEvent;
import engima.waratsea.model.game.event.scenario.ScenarioEventTypes;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

/**
 * This class keeps the current state of the game turn.
 */
@Singleton
@Slf4j
public class TurnViewModel {
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

    private final Turn turn;
    private final ResourceProvider resourceProvider;
    private final ViewProps props;

    @Getter private final StringProperty number = new SimpleStringProperty();
    @Getter private final StringProperty type = new SimpleStringProperty();
    @Getter private final StringProperty timeRange = new SimpleStringProperty();
    @Getter private final StringProperty date = new SimpleStringProperty();
    @Getter private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    @Inject
    public TurnViewModel(final Turn turn,
                         final ResourceProvider resourceProvider,
                         final ViewProps props) {
        this.turn = turn;
        this.resourceProvider = resourceProvider;
        this.props = props;

        update();

        ScenarioEvent.register(this, this::handleScenarioEvent, true);
    }

    /**
     * Progress the game turn to the next game turn.
     *
     * @param event The action event.
     */
    public void nextTurn(final ActionEvent event) {
        turn.next();
        update();
    }

    private void handleScenarioEvent(final ScenarioEvent event) {
        if (event.getType() == ScenarioEventTypes.START) {
            update();
        }
    }

    private void update() {
        number.setValue("Number: " + turn.getNumber());
        type.setValue("Type: " + turn.getType());
        timeRange.setValue("Time: " + turn.getIndex().getTimeRange());
        date.setValue("Date: " + simpleDateFormat.format(turn.getDate()));
        image.setValue(resourceProvider.getImage(props.getString(turn.getType().toLower() + ".image")));
    }
}
