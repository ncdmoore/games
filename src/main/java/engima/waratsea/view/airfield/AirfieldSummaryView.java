package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.view.InfoPane;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Represents the airfield summary information on the left hand side of the airfield
 * details dialog box.
 *
 * CSS styles used
 *
 * - spacing-5
 * - title-pane-non-collapsible
 */
public class AirfieldSummaryView {
    private final InfoPane airfieldSquadronInfo;
    private final InfoPane airfieldMissionInfo;
    private final InfoPane airfieldPatrolInfo;
    private final InfoPane airfieldReadyInfo;

    private final ViewProps props;

    private final ImageView imageView = new ImageView();

    private final TitledPane titledPane = new TitledPane();
    private final VBox leftVBox = new VBox();

    /**
     * Constructor called by guice.
     *
     * @param infoProvider The provides airfield information.
     * @param props The view properties.
     */
    @Inject
    public AirfieldSummaryView(final Provider<InfoPane> infoProvider,
                               final ViewProps props) {
        this.airfieldSquadronInfo = infoProvider.get();
        this.airfieldMissionInfo = infoProvider.get();
        this.airfieldPatrolInfo = infoProvider.get();
        this.airfieldReadyInfo = infoProvider.get();
        this.props = props;
    }

    /**
     * Show the airfield summary.
     *
     * @return The node containing the airfield summary.
     */
    public AirfieldSummaryView build() {
        titledPane.getStyleClass().add("title-pane-non-collapsible");

        BoundTitledGridPane squadronSummary = airfieldSquadronInfo
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .build("Squadron Summary");

        BoundTitledGridPane missionSummary = airfieldMissionInfo
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .build("Mission Summary");

        BoundTitledGridPane patrolSummary = airfieldPatrolInfo
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .build("Patrol Summary");

        BoundTitledGridPane readySummary = airfieldReadyInfo
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .build("Ready Summary");

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(squadronSummary, missionSummary, patrolSummary, readySummary);
        accordion.setExpandedPane(squadronSummary);

        leftVBox.getChildren().addAll(titledPane, imageView, accordion);
        leftVBox.getStyleClass().add("spacing-5");

        return this;
    }

    /**
     * Bind the view model.
     *
     * @param viewModel The view model.
     * @return The node containing the summary view.
     */
    public Node bind(final NationAirbaseViewModel viewModel) {
        titledPane.textProperty().bind(viewModel.getTitle());
        imageView.imageProperty().bind(viewModel.getImage());

        airfieldSquadronInfo.bindIntegers(viewModel.getSquadronCounts());
        airfieldMissionInfo.bindIntegers(viewModel.getMissionCounts());
        airfieldPatrolInfo.bindIntegers(viewModel.getPatrolCounts());
        airfieldReadyInfo.bindIntegers(viewModel.getReadyCounts());

        return leftVBox;
    }
}
