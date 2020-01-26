package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.airfield.mission.MissionView;
import engima.waratsea.view.airfield.patrol.PatrolView;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * The airfield detials dialog view.
 */
public class AirfieldDetailsView {
    private static final String ROUNDEL_SIZE = "20x20.png";

    private final ImageResourceProvider imageResourceProvider;
    private final Provider<AirfieldSummaryView> airfieldSummaryViewProvider;
    private final Provider<MissionView> airfieldMissionViewProvider;
    private final Provider<PatrolView> airfieldPatrolViewProvider;
    private final Provider<AirfieldReadyView> airfieldReadyViewProvider;

    private Airbase airbase;

    @Getter
    private final TabPane nationsTabPane = new TabPane();

    @Getter
    private final Map<Nation, AirfieldSummaryView> airfieldSummaryView = new HashMap<>();

    @Getter
    private final Map<Nation, MissionView> airfieldMissionView = new HashMap<>();

    @Getter
    private final Map<Nation, PatrolView> airfieldPatrolView = new HashMap<>();

    @Getter
    private final Map<Nation, AirfieldReadyView> airfieldReadyView = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param airfieldSummaryViewProvider Provides the airfield summary view.
     * @param airfieldMissionViewProvider Provides the airfield mission view.
     * @param airfieldPatrolViewProvider  Provides the airfield patrol view.
     * @param airfieldReadyViewProvider   Provides the airfield ready view.
     */
    @Inject
    public AirfieldDetailsView(final ImageResourceProvider imageResourceProvider,
                               final Provider<AirfieldSummaryView> airfieldSummaryViewProvider,
                               final Provider<MissionView> airfieldMissionViewProvider,
                               final Provider<PatrolView> airfieldPatrolViewProvider,
                               final Provider<AirfieldReadyView> airfieldReadyViewProvider) {
        this.imageResourceProvider = imageResourceProvider;

        this.airfieldSummaryViewProvider = airfieldSummaryViewProvider;
        this.airfieldMissionViewProvider = airfieldMissionViewProvider;
        this.airfieldPatrolViewProvider = airfieldPatrolViewProvider;
        this.airfieldReadyViewProvider = airfieldReadyViewProvider;
    }

    /**
     * Show the airbase details.
     *
     * @param base The airbase whose details are shown.
     * @return A node containing the airbase details.
     */
    public Node show(final Airbase base) {
        airbase = base;

        nationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        airbase
                .getNations()
                .stream()
                .sorted()
                .map(this::createNationTab)
                .forEach(tab -> nationsTabPane.getTabs().add(tab));

        return nationsTabPane;
    }

    /**
     * Create the given nation's tab.
     *
     * @param nation The nation.
     * @return The nation's tab.
     */
    private Tab createNationTab(final Nation nation) {

        AirfieldSummaryView summaryView = airfieldSummaryViewProvider.get();

        airfieldSummaryView.put(nation, summaryView);
        airfieldMissionView.put(nation, airfieldMissionViewProvider.get());
        airfieldPatrolView.put(nation, airfieldPatrolViewProvider.get());
        airfieldReadyView.put(nation, airfieldReadyViewProvider.get());

        Tab tab = new Tab(nation.toString());

        Node summary = summaryView
                .setAirfield(airbase)
                .show(nation);

        TitledPane missions = buildMissionDetails(nation);
        TitledPane patrols = buildPatrolDetails(nation);
        TitledPane ready = buildReadyDetails(nation);

        Accordion accordion = new Accordion();

        accordion.getPanes().addAll(missions, patrols, ready);
        accordion.setExpandedPane(missions);

        HBox hBox = new HBox(summary, accordion);
        hBox.setId("main-pane");

        ImageView roundel = imageResourceProvider.getImageView(nation + ROUNDEL_SIZE);

        tab.setGraphic(roundel);
        tab.setContent(hBox);

        return tab;
    }

    /**
     * Build the mission details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane containing the mission details of the airfield.
     */
    private TitledPane buildMissionDetails(final Nation nation) {
        return airfieldMissionView
                .get(nation)
                .show(nation);
    }

    /**
     * Build the patrol details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane containing the partol details of the airfield.
     */
    private TitledPane buildPatrolDetails(final Nation nation) {
        return airfieldPatrolView
                .get(nation)
                .setAirbase(airbase)
                .show(nation);
    }

    /**
     * Build the ready details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane containing the ready details of the airfield.
     */
    private TitledPane buildReadyDetails(final Nation nation) {
        return airfieldReadyView
                .get(nation)
                .setAirbase(airbase)
                .show(nation);
    }
}
