package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.info.AirfieldMissionInfo;
import engima.waratsea.view.airfield.info.AirfieldReadyInfo;
import engima.waratsea.view.airfield.info.AirfieldPatrolInfo;
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
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;
    private final AirfieldSquadronInfo airfieldSquadronInfo;
    private final AirfieldMissionInfo airfieldMissionInfo;
    private final AirfieldPatrolInfo airfieldPatrolInfo;
    private final AirfieldReadyInfo airfieldReadyInfo;

    private final ImageView imageView = new ImageView();

    private final TitledPane titledPane = new TitledPane();
    private final VBox leftVBox = new VBox();

    private Nation nation;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props Provides view properties.
     * @param airfieldSquadronInfo The airfield's squadron information.
     * @param airfieldMissionInfo The airfield's mission information.
     * @param airfieldPatrolInfo The airfield's patrol information.
     * @param airfieldReadyInfo The airfield's ready information.
     */
    @Inject
    public AirfieldSummaryView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props,
                               final AirfieldSquadronInfo airfieldSquadronInfo,
                               final AirfieldMissionInfo airfieldMissionInfo,
                               final AirfieldPatrolInfo airfieldPatrolInfo,
                               final AirfieldReadyInfo airfieldReadyInfo) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;

        this.airfieldSquadronInfo = airfieldSquadronInfo;
        this.airfieldMissionInfo = airfieldMissionInfo;
        this.airfieldPatrolInfo = airfieldPatrolInfo;
        this.airfieldReadyInfo = airfieldReadyInfo;
    }

    /**
     * Show the airfield summary.
     *
     * @param selectedNation The nation: BRITISH, ITALIAN, etc.
     * @return The node containing the airfield summary.
     */
    public AirfieldSummaryView build(final Nation selectedNation) {
        nation = selectedNation;

        TitledPane airfieldTitle = buildAirfieldTitle();
        BoundTitledGridPane squadronSummary = airfieldSquadronInfo.build();
        BoundTitledGridPane missionSummary = airfieldMissionInfo.build();
        BoundTitledGridPane patrolSummary = airfieldPatrolInfo.build();
        BoundTitledGridPane readySummary = airfieldReadyInfo.build();

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(squadronSummary, missionSummary, patrolSummary, readySummary);
        accordion.setExpandedPane(squadronSummary);

        leftVBox.getChildren().addAll(airfieldTitle, imageView, accordion);
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
        setTitleAndImage(viewModel);

        airfieldSquadronInfo.bind(viewModel);
        airfieldMissionInfo.bind(viewModel);
        airfieldPatrolInfo.bind(viewModel);
        airfieldReadyInfo.bind(viewModel);

        return leftVBox;
    }

    /**
     * Build the airfield title pane.
     *
     * @return A title pane with the airfield's title.
     */
    private TitledPane buildAirfieldTitle() {
        titledPane.setId("airfield-title-pane");
        return titledPane;
    }

    /**
     * Set the airbase image and title.
     *
     * @param viewModel The nation's airbase view model.
     */
    private void setTitleAndImage(final NationAirbaseViewModel viewModel) {
        Airbase airbase = viewModel.getAirbase();
        AirfieldType airfieldType = airbase.getAirfieldType();

        String imageName = props.getString(nation + ".airfield." + airfieldType + ".image");

        titledPane.setText(airbase.getTitle());
        imageView.setImage(imageResourceProvider.getImage(imageName));
    }
}
