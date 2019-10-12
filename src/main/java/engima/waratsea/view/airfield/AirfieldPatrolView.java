package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.PatrolType;
import engima.waratsea.model.squadron.Squadron;
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
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AirfieldPatrolView {

    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private Airfield airfield;

    private final Map<PatrolType, ListViewPair<Squadron>> patrolListMap = new HashMap<>();
    private final Map<PatrolType, SquadronSummaryView> patrolSquadronMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     * @param squadronSummaryViewProvider Provides the squadron summary view.
     */
    @Inject
    public AirfieldPatrolView(final ImageResourceProvider imageResourceProvider,
                              final ViewProps props,
                              final Provider<SquadronSummaryView> squadronSummaryViewProvider) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;

        Stream.of(PatrolType.values()).forEach(patrolType -> {
            patrolListMap.put(patrolType, new ListViewPair<>("patrol"));
            patrolSquadronMap.put(patrolType, squadronSummaryViewProvider.get());
        });
    }

    /**
     * Set the airfield.
     *
     * @param field The airfield.
     * @return The airfield patrol view.
     */
    public AirfieldPatrolView setAirfield(final Airfield field) {
        this.airfield = field;
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

        Stream.of(PatrolType.values())
                .filter(type -> type != patrolType)
                .forEach(type -> patrolListMap.get(type).removeFromAvailable(squadron));

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

        Stream.of(PatrolType.values())
                .filter(type -> type != patrolType)
                .forEach(type -> {
                    if (squadron.canDoPatrol(type)) {                          // Only add the squadron if it can
                        patrolListMap.get(type).addToAvailable(squadron);      // do the patrol type.
                    }
                });

        return squadron;
    }

    /**
     * Select the assigned squadron.
     *
     * @param squadron The selected assigned squadron.
     * @param patrolType The patrol type.
     */
    public void selectAssignedSquadron(final Squadron squadron, final PatrolType patrolType) {
        patrolListMap.get(patrolType)
                .clearAvailableSelection();

        patrolSquadronMap.get(patrolType)
                .setSelectedSquadron(squadron);
    }

    /**
     * Select the available squadron.
     *
     * @param squadron The selected available squadron.
     * @param patrolType The patrol type.
     */
    public void selectAvailableSquadron(final Squadron squadron, final PatrolType patrolType) {
        patrolListMap.get(patrolType)
                .clearAssignedSelection();

        patrolSquadronMap.get(patrolType)
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

        lists.setImageResourceProvider(imageResourceProvider);

        lists.setWidth(props.getInt("airfield.dialog.patrol.list.width"));
        lists.setHeight(props.getInt("airfield.dialog.patrol.list.height"));
        lists.setButtonWidth(props.getInt("airfield.dialog.patrol.button.width"));
        lists.setAvailableTitle(patrolType.getValue() + " Available");
        lists.setAssignedTitle(patrolType.getValue() + " Assigned");

        lists
                .getAvailable()
                .getItems()
                .addAll(airfield.getCapableSquadrons(nation, patrolType));

        lists.getAdd().setUserData(patrolType);
        lists.getRemove().setUserData(patrolType);

        Node squadronSummaryView = patrolSquadronMap
                .get(patrolType)
                .show(nation);

        Node listNode = lists.build();

        VBox vBox = new VBox(listNode, squadronSummaryView);
        vBox.setId("patrol-main-pane");

        tab.setContent(vBox);

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
}
