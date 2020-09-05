package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.mission.MissionView;
import engima.waratsea.view.airfield.patrol.PatrolView;
import engima.waratsea.view.airfield.ready.AirfieldReadyView;
import engima.waratsea.viewmodel.NationAirbaseViewModel;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the airfield dialog details view.
 */
public class AirfieldView {
    private static final String ROUNDEL = ".roundel.image";

    private final ImageResourceProvider imageResourceProvider;
    private final Provider<AirfieldSummaryView> airfieldSummaryViewProvider;
    private final Provider<MissionView> airfieldMissionViewProvider;
    private final Provider<PatrolView> airfieldPatrolViewProvider;
    private final Provider<AirfieldReadyView> airfieldReadyViewProvider;
    private final ViewProps props;

    private Map<Nation, NationAirbaseViewModel> viewModelMap;

    private final TabPane nationsTabPane = new TabPane();

    @Getter private final Map<Nation, AirfieldSummaryView> airfieldSummaryView = new HashMap<>();
    @Getter private final Map<Nation, MissionView> airfieldMissionView = new HashMap<>();
    @Getter private final Map<Nation, PatrolView> airfieldPatrolView = new HashMap<>();
    @Getter private final Map<Nation, AirfieldReadyView> airfieldReadyView = new HashMap<>();

    @Setter private boolean showPatrolPane = false;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param airfieldSummaryViewProvider Provides the airfield summary view.
     * @param airfieldMissionViewProvider Provides the airfield mission view.
     * @param airfieldPatrolViewProvider  Provides the airfield patrol view.
     * @param airfieldReadyViewProvider   Provides the airfield ready view.
     * @param props The view properties.
     */
    @Inject
    public AirfieldView(final ImageResourceProvider imageResourceProvider,
                        final Provider<AirfieldSummaryView> airfieldSummaryViewProvider,
                        final Provider<MissionView> airfieldMissionViewProvider,
                        final Provider<PatrolView> airfieldPatrolViewProvider,
                        final Provider<AirfieldReadyView> airfieldReadyViewProvider,
                        final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.airfieldSummaryViewProvider = airfieldSummaryViewProvider;
        this.airfieldMissionViewProvider = airfieldMissionViewProvider;
        this.airfieldPatrolViewProvider = airfieldPatrolViewProvider;
        this.airfieldReadyViewProvider = airfieldReadyViewProvider;
        this.props = props;
    }

    /**
     * Show the airbase details.
     *
     * @param map The airbase view model map.
     * @return A node containing the airbase details.
     */
    public Node build(final Map<Nation, NationAirbaseViewModel> map) {
        viewModelMap = map;

        nationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        viewModelMap
                .keySet()
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
        NationAirbaseViewModel viewModel = viewModelMap.get(nation);

        Node summary = buildSummary(nation, viewModel);
        TitledPane missions = buildMissionPane(nation, viewModel);
        TitledPane patrols = buildPatrolPane(nation, viewModel);
        TitledPane ready = buildReadyPane(nation, viewModel);

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(missions, patrols, ready);

        if (showPatrolPane) {
            accordion.setExpandedPane(patrols);
        } else {
            accordion.setExpandedPane(missions);
        }

        HBox hBox = new HBox(summary, accordion);
        hBox.setId("main-pane");

        Tab tab = new Tab(nation.toString());
        ImageView roundel = getRoundel(nation);

        tab.setGraphic(roundel);
        tab.setContent(hBox);

        return tab;
    }

    /**
     * Build the airfield summary pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param viewModel The airbase view model.
     * @return A node containing the airfield summary.
     */
    private Node buildSummary(final Nation nation, final NationAirbaseViewModel viewModel) {
        airfieldSummaryView.put(nation, airfieldSummaryViewProvider.get());

        return airfieldSummaryView
                .get(nation)
                .build(nation)
                .bind(viewModel);
    }

    /**
     * Build the mission details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param viewModel The airbase view model.
     * @return A titled pane containing the mission details of the airfield.
     */
    private TitledPane buildMissionPane(final Nation nation, final NationAirbaseViewModel viewModel) {
        airfieldMissionView.put(nation, airfieldMissionViewProvider.get());

        return airfieldMissionView
                .get(nation)
                .build(nation)
                .bind(viewModel);
    }

    /**
     * Build the patrol details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param viewModel The airbase view model.
     * @return A titled pane containing the patrol details of the airfield.
     */
    private TitledPane buildPatrolPane(final Nation nation, final NationAirbaseViewModel viewModel) {
        airfieldPatrolView.put(nation, airfieldPatrolViewProvider.get());

        return airfieldPatrolView
                .get(nation)
                .build(nation)
                .bind(viewModel);
    }

    /**
     * Build the ready details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param viewModel The airbase view model.
     * @return A titled pane containing the ready details of the airfield.
     */
    private TitledPane buildReadyPane(final Nation nation, final NationAirbaseViewModel viewModel) {
        airfieldReadyView.put(nation, airfieldReadyViewProvider.get());

        return airfieldReadyView
                .get(nation)
                .build(nation)
                .bind(viewModel);
    }

    /**
     * Get the nation's roundel image.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The nation's roundel image view.
     */
    private ImageView getRoundel(final Nation nation) {
        return imageResourceProvider.getImageView(props.getString(nation.toString() + ROUNDEL));
    }
}
