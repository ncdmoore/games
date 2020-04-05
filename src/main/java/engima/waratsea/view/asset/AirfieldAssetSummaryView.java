package engima.waratsea.view.asset;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.TitledGridPane;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AirfieldAssetSummaryView {

    private final ViewProps props;
    private final TitledGridPane titledGridPane = new TitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldAssetSummaryView(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the airfield's summary for the game's asset pane.
     *
     * @return The node that contains the airfield's asset summary.
     */
    public Node build() {
        final int firstColumnWidth = 60;
        final int secondColumnWidth = 40;

        titledGridPane
                .setTitle("Airfield Summary")
                .setWidth(props.getInt("asset.pane.component.width"))
                .setGridStyleId("component-grid")
                .buildPane();

        titledGridPane.getStyleClass().add("asset-component-pane");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(firstColumnWidth);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(secondColumnWidth);
        titledGridPane.setColumnConstraints(List.of(col1, col2));

        return titledGridPane;
    }

    /**
     * Show the given airbase in the asset summary view.
     *
     * @param airbase The airbase displayed in the asset summary view.
     */
    public void show(final Airbase airbase) {
        titledGridPane.updatePane(getAirbaseData(airbase));
    }

    /**
     * Get the Airbase's data.
     *
     * @param airbase The airbase's data that is retrieved.
     * @return The given airbase's data.
     */
    private Map<String, String> getAirbaseData(final Airbase airbase) {
        Map<String, String> data = new LinkedHashMap<>();

        String space1 = "";
        String space2 = " ";

        data.put("Name:", airbase.getTitle());
        data.put("Region:", airbase.getRegionTitle());
        data.put(space1, "");
        data.put("Max Capacity (Steps):", airbase.getMaxCapacity() + "");
        data.put("Current Capacity (Steps):", airbase.getCapacity() + "");
        data.put("Current stationed (Steps):", airbase.getCurrentSteps().toBigInteger().toString());
        data.put(space2, "");
        data.put("AA Rating:", airbase.getAntiAirRating() + "");

        return data;
    }

}
