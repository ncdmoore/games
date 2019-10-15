package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class AirfieldDetailsView {
    private static final String ROUNDEL_SIZE = "20x20.png";

    private final ImageResourceProvider imageResourceProvider;
    private final Provider<AirfieldSummaryView> airfieldSummaryViewProvider;
    private final Provider<AirfieldPatrolView> airfieldPatrolViewProvider;
    private final Provider<AirfieldReadyView> airfieldReadyViewProvider;


    private Airfield airfield;

    @Getter
    private final TabPane nationsTabPane = new TabPane();

    @Getter
    private final Map<Nation, AirfieldSummaryView> airfieldSummaryView = new HashMap<>();

    @Getter
    private final Map<Nation, AirfieldPatrolView> airfieldPatrolView = new HashMap<>();

    @Getter
    private final Map<Nation, AirfieldReadyView> airfieldReadyView = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param airfieldSummaryViewProvider Provides the airfield summary view.
     * @param airfieldPatrolViewProvider  Provides the airfield patrol view.
     * @param airfieldReadyViewProvider   Provides the airfield ready view.
     */
    @Inject
    public AirfieldDetailsView(final ImageResourceProvider imageResourceProvider,
                               final Provider<AirfieldSummaryView> airfieldSummaryViewProvider,
                               final Provider<AirfieldPatrolView> airfieldPatrolViewProvider,
                               final Provider<AirfieldReadyView> airfieldReadyViewProvider) {
        this.imageResourceProvider = imageResourceProvider;

        this.airfieldSummaryViewProvider = airfieldSummaryViewProvider;
        this.airfieldPatrolViewProvider = airfieldPatrolViewProvider;
        this.airfieldReadyViewProvider = airfieldReadyViewProvider;
    }

    /**
     * Show the airfield details.
     *
     * @param field The airfield whose details are shown.
     * @return A node containing the airfield details.
     */
    public Node show(final Airfield field) {
        airfield = field;

        nationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        airfield
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
        airfieldPatrolView.put(nation, airfieldPatrolViewProvider.get());
        airfieldReadyView.put(nation, airfieldReadyViewProvider.get());

        Tab tab = new Tab(nation.toString());

        Node summary = summaryView
                .setAirfield(airfield)
                .show(nation);

        TitledPane missions = buildMissionDetails();
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
     * @return A titled pane containing the mission details of the airfield.
     */
    private TitledPane buildMissionDetails() {
        TitledPane titledPane = new TitledPane();

        titledPane.setText("Missions");

        Label label = new Label("mission data");

        titledPane.setContent(label);

        return titledPane;
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
                .setAirfield(airfield)
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
                .setAirfield(airfield)
                .show(nation);
    }

}
