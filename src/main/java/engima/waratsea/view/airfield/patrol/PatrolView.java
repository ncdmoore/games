package engima.waratsea.view.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronSummaryView;
import engima.waratsea.view.util.ListViewPair;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PatrolView {
    private final ViewProps props;

    private final Map<PatrolType, ListViewPair<Squadron>> patrolListMap = new HashMap<>();
    private final Map<PatrolType, SquadronSummaryView> patrolSummaryMap = new HashMap<>();
    private final Map<PatrolType, PatrolStatsView> patrolStatsMap = new HashMap<>();

    private final TitledPane titledPane = new TitledPane();


    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     * @param airfieldPatrolStatsViewProvider Provides patrol stats view.
     * @param squadronSummaryViewProvider Provides the squadron summary view.
     */
    @Inject
    public PatrolView(final ImageResourceProvider imageResourceProvider,
                      final ViewProps props,
                      final Provider<PatrolStatsView> airfieldPatrolStatsViewProvider,
                      final Provider<SquadronSummaryView> squadronSummaryViewProvider) {
        this.props = props;

        Stream.of(PatrolType.values()).forEach(patrolType -> {
            patrolListMap.put(patrolType, new ListViewPair<>("patrol", props, imageResourceProvider));
            patrolSummaryMap.put(patrolType, squadronSummaryViewProvider.get());
            patrolStatsMap.put(patrolType, airfieldPatrolStatsViewProvider.get());
        });
    }

    /**
     * Build the patrol details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane containing the patrol details of the airfield.
     */
    public PatrolView build(final Nation nation) {

        titledPane.setText("Patrols");

        TabPane patrolPane = new TabPane();
        patrolPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<Tab> tabs = Stream.of(PatrolType.values())
                .map(patrolType -> buildPatrolTab(nation, patrolType))
                .collect(Collectors.toList());

        patrolPane.getTabs().addAll(tabs);

        titledPane.setContent(patrolPane);

        return this;
    }

    /**
     * Bind this view to the given view model.
     *
     * @param viewModel The airfield view model.
     * @return This airfield ready view.
     */
    public TitledPane bind(final NationAirbaseViewModel viewModel) {
        patrolListMap.forEach((type, list) -> bindList(type, list, viewModel));

        patrolStatsMap.forEach((type, stats) -> stats.bind(viewModel.getPatrolViewModels().get(type)));

        return titledPane;
    }

    /**
     * Get the available list for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @return The given patrol type's available list.
     */
    public ListView<Squadron> getAvailable(final PatrolType patrolType) {
        return patrolListMap.get(patrolType).getAvailable();
    }

    /**
     * Get the assigned list for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @return The given patrol type's assigned list.
     */
    public ListView<Squadron> getAssigned(final PatrolType patrolType) {
        return patrolListMap.get(patrolType).getAssigned();
    }

    /**
     * Get the add button for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @return The given patrol type's add button.
     */
    public Button getAddButton(final PatrolType patrolType) {
        return patrolListMap.get(patrolType).getAdd();
    }

    /**
     * Get the remove button for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @return The given patrol type's remove button.
     */
    public Button getRemoveButton(final PatrolType patrolType) {
        return patrolListMap.get(patrolType).getRemove();
    }

    /**
     * Select a squadron.
     *
     * @param squadron The selected squadron.
     * @param patrolType The patrol type.
     * @param config The selected squadron's configuration.
     */
    public void selectSquadron(final Squadron squadron, final PatrolType patrolType, final SquadronConfig config) {
        patrolSummaryMap
                .get(patrolType)
                .setConfig(config)
                .setSelectedSquadron(squadron);
    }

    /**
     * Build a patrol tab.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param patrolType The type of patrol.
     * @return The tab contain all the patrol information.
     */
    private Tab buildPatrolTab(final Nation nation, final PatrolType patrolType) {
        Tab tab = new Tab(patrolType.getValue());

        ListViewPair<Squadron> lists = patrolListMap.get(patrolType);

        lists.setWidth(props.getInt("airfield.dialog.patrol.list.width"));
        lists.setHeight(props.getInt("airfield.dialog.patrol.list.height"));
        lists.setButtonWidth(props.getInt("airfield.dialog.patrol.button.width"));
        lists.setAvailableTitle(patrolType.getValue() + " Available");
        lists.setAssignedTitle(patrolType.getValue() + " Assigned");

        lists.getAdd().setUserData(patrolType);
        lists.getRemove().setUserData(patrolType);

        Node patrolStatsView = buildPatrolStats(nation, patrolType);

        Node squadronSummaryView = patrolSummaryMap
                .get(patrolType)
                .show(nation);

        Node listNode = lists.build();

        BorderPane borderPane = new BorderPane();

        borderPane.setTop(listNode);
        borderPane.setCenter(patrolStatsView);
        borderPane.setBottom(squadronSummaryView);
        borderPane.setId("patrol-main-pane");

        tab.setContent(borderPane);

        return tab;
    }

    /**
     * Build patrol stats.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param patrolType The type of patrol.
     * @return A node containing the patrol stats.
     */
    private Node buildPatrolStats(final Nation nation, final PatrolType patrolType) {
        return patrolStatsMap
                .get(patrolType)
                .build(nation);
    }

    /**
     * Bind the patrol available and assigned list to the view model.
     *
     * @param type The type of patrol.
     * @param listViewPair The patrol's list view pair.
     * @param viewModel The airbase view model.
     */
    private void bindList(final PatrolType type, final ListViewPair<Squadron> listViewPair, final NationAirbaseViewModel viewModel) {
        listViewPair
                .getAvailable()
                .itemsProperty()
                .bind(viewModel.getAvailablePatrolSquadrons(type));

        listViewPair
                .getAssigned()
                .itemsProperty()
                .bind(viewModel.getAssignedPatrolSquadrons(type));

        listViewPair.getAdd().disableProperty().bind(viewModel.getAvailablePatrolExists(type));
        listViewPair.getRemove().disableProperty().bind(viewModel.getAssignedPatrolExists(type));
    }
}
