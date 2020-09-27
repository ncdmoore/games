package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;

public class AirfieldSquadronInfo {
    private final ViewProps props;

    private final BoundTitledGridPane squadronCountsPane = new BoundTitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldSquadronInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the squadron counts node.
     *
     * @return A node that contains the squadron counts for each type of aircraft.
     */
    public BoundTitledGridPane build() {
        return squadronCountsPane
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setGridStyleId("component-grid")
                .setTitle("Squadron Summary")
                .build();
    }

    /**
     * Set the airbase.
     *
     * @param viewModel  The nation's airbase view model.
     */
    public void bind(final NationAirbaseViewModel viewModel) {
        squadronCountsPane.bindIntegers(viewModel.getSquadronCounts());
    }
}
