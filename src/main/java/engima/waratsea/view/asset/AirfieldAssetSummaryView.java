package engima.waratsea.view.asset;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.GridPaneMap;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AirfieldAssetSummaryView {

    private final ViewProps props;
    private final ImageResourceProvider imageResourceProvider;

    private final TitledPane summaryPane = new TitledPane();
    private final GridPaneMap summaryGrid = new GridPaneMap();

    private final TitledPane landingTypesPane = new TitledPane();

    private Airbase airbase;

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param imageResourceProvider Provides images.
     */
    @Inject
    public AirfieldAssetSummaryView(final ViewProps props,
                                    final ImageResourceProvider imageResourceProvider) {
        this.props = props;
        this.imageResourceProvider = imageResourceProvider;
    }

    /**
     * Build the airfield's summary for the game's asset pane.
     *
     * @return The node that contains the airfield's asset summary.
     */
    public Node build() {
        buildSummary();
        buildLandingTypes();

        HBox hBox = new HBox(summaryPane, landingTypesPane);

        hBox.setFillHeight(true);

        summaryPane.setMinHeight(props.getInt("asset.pane.component.height"));
        landingTypesPane.setMinHeight(props.getInt("asset.pane.component.height"));

        return hBox;
    }

    /**
     * Show the given airbase in the asset summary view.
     *
     * @param base The airbase displayed in the asset summary view.
     */
    public void show(final Airbase base) {
        airbase = base;

        showSummary();
        showLandingTypes();
    }

    /**
     * Build the airfield summary.
     **/
    private void buildSummary() {
        summaryPane.setText("Airfield Summary");
        summaryPane.getStyleClass().add("asset-component-pane");

        final int firstColumnWidth = 60;
        final int secondColumnWidth = 40;

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(firstColumnWidth);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(secondColumnWidth);

        summaryGrid.setGridStyleId("component-grid");
        summaryGrid.setWidth(props.getInt("asset.pane.grid.component.width"));
        summaryGrid.setColumnConstraints(List.of(col1, col2));
    }

    /**
     * Build the landing types.
     */
    private void buildLandingTypes() {
        landingTypesPane.setText("Supported Landing Types");
        landingTypesPane.getStyleClass().add("asset-component-pane");
    }

    /**
     * Show the summary for the selected airfield.
     */
    private void showSummary() {
        Image image = imageResourceProvider.getImage(props.getString(airbase.getSide().toLower() + ".airfield.medium.icon"));

        Node imageView = new ImageView(image);
        Node grid = summaryGrid.buildGrid(getAirbaseData());

        HBox hBox = new HBox(imageView, grid);
        hBox.setId("airfield-summary-hbox");

        summaryPane.setContent(hBox);
    }

    /**
     * Show landing types.
     */
    private void showLandingTypes() {
        List<CheckBox> checkBoxes = Stream.of(LandingType.values())
                .map(this::buildCheckBox)
                .collect(Collectors.toList());

        VBox vBox = new VBox();
        vBox.getChildren().addAll(checkBoxes);
        vBox.setId("airfield-landing-type-vbox");

        landingTypesPane.setContent(vBox);
    }

    /**
     * Get the Airbase's data.
     *
     * @return The given airbase's data.
     */
    private Map<String, String> getAirbaseData() {
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

    /**
     * Build the landing type check boxes that indicate which
     * landing types the airfield support.
     *
     * @param landingType An aircraft/squadron landing type.
     * @return A checkbox corresponding to the given landing type.
     */
    private CheckBox buildCheckBox(final LandingType landingType) {
        CheckBox checkBox = new CheckBox(landingType.toString());
        if (airbase.getLandingType().contains(landingType)) {
            checkBox.setSelected(true);
        }
        checkBox.setDisable(true);
        return checkBox;
    }
}
