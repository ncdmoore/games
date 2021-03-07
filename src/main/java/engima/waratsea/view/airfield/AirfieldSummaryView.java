package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.view.InfoPane;
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

    private final ImageView imageView = new ImageView();

    private final TitledPane titledPane = new TitledPane();
    private final VBox leftVBox = new VBox();

    /**
     * Constructor called by guice.
     *
     * @param infoProvider The provides airfield information.
     */
    @Inject
    public AirfieldSummaryView(final Provider<InfoPane> infoProvider) {
        this.airfieldSquadronInfo = infoProvider.get();
        this.airfieldMissionInfo = infoProvider.get();
        this.airfieldPatrolInfo = infoProvider.get();
        this.airfieldReadyInfo = infoProvider.get();
    }

    /**
     * Show the airfield summary.
     *
     * @return The node containing the airfield summary.
     */
    public AirfieldSummaryView build() {
        titledPane.getStyleClass().add("title-pane-non-collapsible");

        BoundTitledGridPane squadronSummary = airfieldSquadronInfo.build("Squadron Summary");
        BoundTitledGridPane missionSummary = airfieldMissionInfo.build("Mission Summary");
        BoundTitledGridPane patrolSummary = airfieldPatrolInfo.build("Patrol Summary");
        BoundTitledGridPane readySummary = airfieldReadyInfo.build("Ready Summary");

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
