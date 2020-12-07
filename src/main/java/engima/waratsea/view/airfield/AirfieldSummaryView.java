package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.view.airfield.info.AirfieldMissionInfo;
import engima.waratsea.view.airfield.info.AirfieldPatrolInfo;
import engima.waratsea.view.airfield.info.AirfieldReadyInfo;
import engima.waratsea.view.airfield.info.AirfieldSquadronInfo;
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
 */
public class AirfieldSummaryView {
    private final AirfieldSquadronInfo airfieldSquadronInfo;
    private final AirfieldMissionInfo airfieldMissionInfo;
    private final AirfieldPatrolInfo airfieldPatrolInfo;
    private final AirfieldReadyInfo airfieldReadyInfo;

    private final ImageView imageView = new ImageView();

    private final TitledPane titledPane = new TitledPane();
    private final VBox leftVBox = new VBox();

    /**
     * Constructor called by guice.
     *
     * @param airfieldSquadronInfo The airfield's squadron information.
     * @param airfieldMissionInfo The airfield's mission information.
     * @param airfieldPatrolInfo The airfield's patrol information.
     * @param airfieldReadyInfo The airfield's ready information.
     */
    @Inject
    public AirfieldSummaryView(final AirfieldSquadronInfo airfieldSquadronInfo,
                               final AirfieldMissionInfo airfieldMissionInfo,
                               final AirfieldPatrolInfo airfieldPatrolInfo,
                               final AirfieldReadyInfo airfieldReadyInfo) {

        this.airfieldSquadronInfo = airfieldSquadronInfo;
        this.airfieldMissionInfo = airfieldMissionInfo;
        this.airfieldPatrolInfo = airfieldPatrolInfo;
        this.airfieldReadyInfo = airfieldReadyInfo;
    }

    /**
     * Show the airfield summary.
     *
     * @return The node containing the airfield summary.
     */
    public AirfieldSummaryView build() {
        titledPane.setId("airfield-title-pane");

        BoundTitledGridPane squadronSummary = airfieldSquadronInfo.build();
        BoundTitledGridPane missionSummary = airfieldMissionInfo.build();
        BoundTitledGridPane patrolSummary = airfieldPatrolInfo.build();
        BoundTitledGridPane readySummary = airfieldReadyInfo.build();

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(squadronSummary, missionSummary, patrolSummary, readySummary);
        accordion.setExpandedPane(squadronSummary);

        leftVBox.getChildren().addAll(titledPane, imageView, accordion);
        leftVBox.setId("airfield-summary-vbox");

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

        airfieldSquadronInfo.bind(viewModel);
        airfieldMissionInfo.bind(viewModel);
        airfieldPatrolInfo.bind(viewModel);
        airfieldReadyInfo.bind(viewModel);

        return leftVBox;
    }
}
