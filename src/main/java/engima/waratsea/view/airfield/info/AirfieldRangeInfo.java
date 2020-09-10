package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.NationAirbaseViewModel;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class AirfieldRangeInfo {
    private final ViewProps props;

    private final TitledPane titledPane = new TitledPane();

    private final ChoiceBox<Aircraft> aircraftModels = new ChoiceBox<>();


    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldRangeInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * build the airfield aircraft range information.
     *
     * @return The titledPane.
     */
    public TitledPane build() {
        titledPane.setText("Range Helper");
        titledPane.setMinWidth(props.getInt("airfield.dialog.airfield.details.width"));
        titledPane.setMaxWidth(props.getInt("airfield.dialog.airfield.details.width"));
        titledPane.getStyleClass().add("component-grid");

        Label label = new Label("Aircraft Models:");

        VBox vBox = new VBox(label, aircraftModels);
        vBox.setId("airfield-range-vbox");

        titledPane.setContent(vBox);

        return titledPane;
    }

    /**
     * Bind the airbase's aircraft models to the aircraft models choice box.
     *
     * @param viewModel The nation's airbase view model.
     */
    public void bind(final NationAirbaseViewModel viewModel) {
        aircraftModels.itemsProperty().bind(viewModel.getAircraftModels());
        aircraftModels.getSelectionModel().selectFirst();
    }
}
