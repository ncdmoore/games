package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;

public class AirfieldReadyInfo {
    private final ViewProps props;

    private final BoundTitledGridPane readyCountsPane = new BoundTitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldReadyInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the squadron counts node.
     *
     * @return A node that contains the squadron counts for each type of aircraft.
     */
    public BoundTitledGridPane build() {
        return readyCountsPane
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setGridStyleId("component-grid")
                .setTitle("Ready Summary")
                .build();
    }

    /**
     * Set the airbase.
     *
     * @param viewModel  The nation's airbase view model.
     */
    public void bind(final NationAirbaseViewModel viewModel) {
        readyCountsPane.bindIntegers(viewModel.getReadyCounts());
    }
}
