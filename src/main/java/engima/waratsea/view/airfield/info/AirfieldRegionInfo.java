package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class AirfieldRegionInfo {
    private final ViewProps props;

    private final BoundTitledGridPane regionCountsPane = new BoundTitledGridPane();
    private final Map<String, IntegerProperty> regionCounts = new LinkedHashMap<>();
    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldRegionInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the squadron counts node.
     *
     * @return A node that contains the squadron counts for each type of aircraft.
     */
    public BoundTitledGridPane build() {
        return regionCountsPane
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setGridStyleId("component-grid")
                .setTitle("Region Step Summary")
                .build();
    }

    /**
     * Set the airbase.
     *
     * @param viewModel  The nation's airbase view model.
     */
    public void bind(final NationAirbaseViewModel viewModel) {

        IntegerProperty minimum = new SimpleIntegerProperty();
        IntegerProperty maximum = new SimpleIntegerProperty();
        IntegerProperty current = new SimpleIntegerProperty();

        minimum.bind(Bindings.createIntegerBinding(() -> Integer.parseInt(viewModel.getRegionMinimum().getValue()), viewModel.getRegionMinimum()));
        maximum.bind(Bindings.createIntegerBinding(() -> Integer.parseInt(viewModel.getRegionMaximum().getValue()), viewModel.getRegionMaximum()));
        current.bind(Bindings.createIntegerBinding(() -> Integer.parseInt(viewModel.getRegionCurrent().getValue()), viewModel.getRegionCurrent()));

        regionCounts.put("Minimum (Steps):", minimum);
        regionCounts.put("Maximum (Steps):", maximum);
        regionCounts.put("Current (Steps):", current);

        regionCountsPane.bindIntegers(regionCounts);
    }
}
