package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import engima.waratsea.viewmodel.airfield.RangeViewModel;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class AirfieldRangeInfo {
    private final ViewProps props;

    private final TitledPane titledPane = new TitledPane();

    @Getter private final ChoiceBox<Aircraft> aircraftModels = new ChoiceBox<>();
    @Getter private final ChoiceBox<SquadronConfig> config = new ChoiceBox<>();
    @Getter private final CheckBox showRangeOnMap = new CheckBox("Show Radius");

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
        titledPane.setMinWidth(props.getInt("asset.range.pane.width"));
        titledPane.setMaxWidth(props.getInt("asset.range.pane.width"));
        titledPane.getStyleClass().add("component-grid");

        aircraftModels.setMinWidth(props.getInt("asset.range.choice.width"));
        config.setMinWidth(props.getInt("asset.range.choice.width"));

        Label modelLabel = new Label("Aircraft Model:");
        VBox modelVbox = new VBox(modelLabel, aircraftModels);

        Label configLabel = new Label("Squadron Config:");
        VBox configVbox = new VBox(configLabel, config);

        VBox choicesVbox = new VBox(modelVbox, configVbox);
        choicesVbox.setId("airfield-range-choices-vbox");

        Label blank = new Label();
        VBox checksVbox = new VBox(blank, showRangeOnMap);
        checksVbox.setId("airfield-range-checks-vbox");

        HBox mainHbox = new HBox(choicesVbox, checksVbox);
        mainHbox.setId("airfield-range-hbox");

        titledPane.setContent(mainHbox);

        return titledPane;
    }

    /**
     * Bind the airbase's aircraft models to the aircraft models choice box.
     *
     * @param viewModel The nation's airbase view model.
     */
    public void bind(final NationAirbaseViewModel viewModel) {
        RangeViewModel rangeViewModel = viewModel.getRangeViewModel();

        aircraftModels.itemsProperty().bind(rangeViewModel.getAircraftModels());
        rangeViewModel
                .getSelectedAircraft()
                .bind(aircraftModels.getSelectionModel().selectedItemProperty());

        config.itemsProperty().bind(rangeViewModel.getSquadronConfigs());
        rangeViewModel
                .getSelectedConfig()
                .bind(config.getSelectionModel().selectedItemProperty());
    }
}
