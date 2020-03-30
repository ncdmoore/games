package engima.waratsea.view.airfield.patrol;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronSummaryView;
import engima.waratsea.view.util.ListViewPair;
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

/**
 * The patrol view of the airfield details dialog.
 */
public class PatrolView {

    private final ViewProps props;

    private Airbase airbase;

    private final Map<PatrolType, ListViewPair<Squadron>> patrolListMap = new HashMap<>();
    private final Map<PatrolType, SquadronSummaryView> patrolSquadronMap = new HashMap<>();
    private final Map<PatrolType, PatrolStatsView> patrolStatsMap = new HashMap<>();

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
            patrolListMap.put(patrolType, new ListViewPair<>("patrol", imageResourceProvider));
            patrolSquadronMap.put(patrolType, squadronSummaryViewProvider.get());
            patrolStatsMap.put(patrolType, airfieldPatrolStatsViewProvider.get());
        });
    }

    /**
     * Set the air base.
     *
     * @param base The air base.
     * @return The airfield patrol view.
     */
    public PatrolView setAirbase(final Airbase base) {
        this.airbase = base;

        patrolStatsMap.forEach((type, view) -> {
            view.setAirbase(airbase);
            view.setPatrolType(type);
        });

        return this;
    }

    /**
     * Build the patrol details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane containing the partol details of the airfield.
     */
    public TitledPane show(final Nation nation) {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Patrols");

        TabPane patrolPane = new TabPane();
        patrolPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<Tab> tabs = Stream.of(PatrolType.values())
                .map(patrolType -> buildPatrolTab(nation, patrolType))
                .collect(Collectors.toList());

        patrolPane.getTabs().addAll(tabs);

        titledPane.setContent(patrolPane);

        return titledPane;
    }

    /**
     * Get the available list for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @return The given patrol type's available list.
     */
    public ListView<Squadron> getAvailableList(final PatrolType patrolType) {
        return patrolListMap.get(patrolType).getAvailable();
    }

    /**
     * Get the assigned list for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @return The given patrol type's assigned list.
     */
    public ListView<Squadron> getAssignedList(final PatrolType patrolType) {
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
     * Assign the currently selected available squadron of the given patrol type to the assigned list and
     * remove it from any available list.
     *
     * @param patrolType The patrol type.
     * @return The assigned squadron.
     */
    public Squadron assignPatrol(final PatrolType patrolType) {
        Squadron squadron = getAvailableSquadron(patrolType);
        patrolListMap.get(patrolType).add(squadron);
        return squadron;
    }

    /**
     * Remove the currently selected assigned squadron of the given patrol type from the assigne list and
     * add it to any available list.
     *
     * @param patrolType The patrol type.
     * @return The removed squadron.
     */
    public Squadron removePatrol(final PatrolType patrolType) {
        Squadron squadron = getAssignedSquadron(patrolType);
        patrolListMap.get(patrolType).remove(squadron);
        return squadron;
    }

    /**
     * Select the assigned squadron.
     *  @param squadron The selected assigned squadron.
     * @param config A squadron configuration.
     * @param patrolType The patrol type.
     */
    public void selectAssignedSquadron(final Squadron squadron, final SquadronConfig config, final PatrolType patrolType) {
        patrolListMap
                .get(patrolType)
                .clearAvailableSelection();

        patrolSquadronMap
                .get(patrolType)
                .setConfig(config)
                .setSelectedSquadron(squadron);
    }

    /**
     * Select the available squadron.
     *  @param squadron The selected available squadron.
     * @param config A squadron configuration.
     * @param patrolType The patrol type.
     */
    public void selectAvailableSquadron(final Squadron squadron, final SquadronConfig config, final PatrolType patrolType) {
        patrolListMap
                .get(patrolType)
                .clearAssignedSelection();

        patrolSquadronMap
                .get(patrolType)
                .setConfig(config)
                .setSelectedSquadron(squadron);
    }

    /**
     * Get the number of squadrons for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @return The number of squadrons on the given patrol.
     */
    public int getNumSquadronsOnPatrol(final PatrolType patrolType) {
        return patrolListMap
                .get(patrolType)
                .getAssigned()
                .getItems()
                .size();
    }

    /**
     * Update the patrol stats.
     *
     * @param nation The nation: BRITSH, ITALIAN, etc...
     * @param patrolType The patrol type.
     */
    public void updatePatrolStats(final Nation nation, final PatrolType patrolType) {
            List<Squadron> assigned = patrolListMap
                    .get(patrolType)
                    .getAssigned()
                    .getItems();

            patrolStatsMap
                    .get(patrolType)
                    .updatePatrolStats(nation, assigned);
    }

    /**
     * Add the given squadron to all of the available patrol type lists, except for the given patrol type.
     *
     * @param patrolType The patrol type.
     * @param squadron The squadron to add.
     */
    public void addSquadronToPatrolAvailableList(final PatrolType patrolType, final Squadron squadron) {
        Stream.of(PatrolType.values())
                .filter(type -> type != patrolType)
                .forEach(type -> {
                    if (squadron.canDoPatrol(type)) {                          // Only add the squadron if it can
                        patrolListMap.get(type).addToAvailable(squadron);      // do the patrol type.
                    }
                });
    }

    /**
     * Add the given squadron to all of the available patrol type lists.
     *
     * @param squadron The squadron to add.
     */
    public void addSquadronToPatrolAvailableList(final Squadron squadron) {
        Stream.of(PatrolType.values())
                .forEach(type -> {
                    if (squadron.canDoPatrol(type)) {                          // Only add the squadron if it can
                        patrolListMap.get(type).addToAvailable(squadron);      // do the patrol type.
                    }
                });
    }

    /**
     * Remove the given squadron from all of the available patrol type lists.
     *
     * @param squadron The squadron that is removed from the patrol available lists.
     */
    public void removeSquadronFromPatrolAvailableList(final Squadron squadron) {
        Stream.of(PatrolType.values())
                .forEach(type -> patrolListMap.get(type).removeFromAvailable(squadron));
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

        lists.clearAll();
        lists.addAllToAvailable(airbase.getReadySquadrons(nation, patrolType));
        lists.addAllToAssigned(airbase.getPatrol(patrolType).getSquadrons(nation));

        lists.getAdd().setUserData(patrolType);
        lists.getRemove().setUserData(patrolType);

        Node patrolStatsView = buildPatrolStats(nation, patrolType);

        Node squadronSummaryView = patrolSquadronMap
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
     * Get the currently selected squadron of the given patrol type's available list.
     *
     * @param patrolType The patrol type.
     * @return The selected squadron of tha available list for the given patrol type.
     */
    private Squadron getAvailableSquadron(final PatrolType patrolType) {
        return patrolListMap
                .get(patrolType)
                .getAvailable()
                .getSelectionModel()
                .getSelectedItem();
    }

    /**
     * Get the currently selected squadron of the given patrol type's assigned list.
     *
     * @param patrolType The patrol type.
     * @return The selected squadron of the assigned list for the given patrol type.
     */
    private Squadron getAssignedSquadron(final PatrolType patrolType) {
        return patrolListMap
                .get(patrolType)
                .getAssigned()
                .getSelectionModel()
                .getSelectedItem();
    }

    /**
     * Build patrol stats.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param patrolType The type of patrol.
     * @return A node containing the patrol stats.
     */
    private Node buildPatrolStats(final Nation nation, final PatrolType patrolType) {
        List<Squadron> assigned = patrolListMap
                .get(patrolType)
                .getAssigned()
                .getItems();

        return patrolStatsMap
                .get(patrolType)
                .buildPatrolStats(nation, assigned);
    }
}
