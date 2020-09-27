package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;

public class AirfieldMissionInfo {
    private final ViewProps props;

    private final BoundTitledGridPane missionCountsPane = new BoundTitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldMissionInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the squadron counts node.
     *
     * @return A node that contains the squadron counts for each type of aircraft.
     */
    public BoundTitledGridPane build() {
        return missionCountsPane
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setGridStyleId("component-grid")
                .setTitle("Mission Summary")
                .build();
    }

    /**
     * Set the airbase.
     *
     * @param viewModel  The nation's airbase view model.
     */
    public void bind(final NationAirbaseViewModel viewModel) {
        missionCountsPane.bindIntegers(viewModel.getMissionCounts());
    }
}
