package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronSummaryView;
import engima.waratsea.view.util.ListViewPair;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the view of a mission being edited.
 */
public class MissionEditView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    @Getter private final ChoiceBox<AirMissionType> missionType = new ChoiceBox<>();
    @Getter private final ChoiceBox<Target> target = new ChoiceBox<>();
    @Getter private final TargetView targetView;

    @Getter private final Map<MissionRole, ListViewPair<Squadron>> squadrons = new HashMap<>();
    private final Map<MissionRole, StackPane> stackPanes = new HashMap<>();

    @Getter private final TabPane tabPane = new TabPane();

    private final Map<MissionRole, Tab> roleTabs = new HashMap<>();

    @Getter private final ImageView imageView = new ImageView();

    @Getter private final SquadronSummaryView squadronSummaryView;

    private final VBox mainVBox = new VBox();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param targetView The target view.
     * @param imageResourceProvider Provides images.
     * @param squadronSummaryView Squadron summary view.
     */
    @Inject
    public MissionEditView(final ViewProps props,
                           final TargetView targetView,
                           final ImageResourceProvider imageResourceProvider,
                           final SquadronSummaryView squadronSummaryView) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
        this.targetView = targetView;
        this.squadronSummaryView = squadronSummaryView;

        missionType.setMinWidth(props.getInt("mission.type.list.width"));
        target.setMinWidth(props.getInt("mission.type.list.width"));

        MissionRole
                .stream()
                .forEach(this::createSquadronList);

    }

    /**
     * Show the airbase mission details.
     *
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A node containing the airbase mission details.
     */
    public MissionEditView build(final Nation nation) {
        Node missionNode = buildMissionNode();
        Node targetNode = buildTargetNode();

        VBox choiceBoxes = new VBox(missionNode, targetNode);
        choiceBoxes.setId("choices-pane");

        Node targetDetailsBox = targetView.build();

        HBox hBox = new HBox(choiceBoxes, imageView);
        hBox.setId("target-hbox");

        Node squadronsList = buildSquadronLists();

        Node squadronSummaryNode = squadronSummaryView.build(nation);

        VBox leftVBox = new VBox(hBox, squadronsList);
        leftVBox.setId("left-vbox");

        HBox mainHBox = new HBox(leftVBox, targetDetailsBox);
        mainHBox.setId("main-hbox");

        mainVBox.getChildren().addAll(mainHBox, squadronSummaryNode);

        mainVBox.setId("main-pane");
        return this;
    }

    /**
     * Bind the view to the view model.
     *
     * @param viewModel The air mission view model.
     * @return The node containing this view.
     */
    public Node bind(final AirMissionViewModel viewModel) {
        missionType.getItems().add(viewModel.getMissionType().getValue());
        target.getItems().add(viewModel.getTarget().getValue());

        ReadOnlyObjectProperty<AirMissionType> selectedMissionType = missionType.getSelectionModel().selectedItemProperty();

        imageView.imageProperty().bind(Bindings.createObjectBinding(() -> getImage(viewModel.getNation(), selectedMissionType), selectedMissionType));

        setRoles();

        MissionRole.stream().forEach(role -> {
            squadrons.get(role).getAvailable().itemsProperty().bind(viewModel.getAvailable().get(role));
            squadrons.get(role).getAssigned().itemsProperty().bind(viewModel.getAssigned().get(role));
            squadrons.get(role).getAdd().disableProperty().bind(viewModel.getAvailableExists().get(role));
            squadrons.get(role).getRemove().disableProperty().bind(viewModel.getAssignedExists().get(role));
        });

        targetView.bind(viewModel);

        // Manually trigger the mission type selection for edits.
        targetView.missionTypeSelected(viewModel.getMissionType().getValue());

        squadronSummaryView.bind(viewModel.getSelectedSquadron());

        return mainVBox;
    }

    /**
     * Get the mission list.
     *
     * @param role The squadron mission role.
     * @return The available and assigned mission list view pair.
     */
    public ListViewPair<Squadron> getSquadronList(final MissionRole role) {
        return squadrons.get(role);
    }

    /**
     * Get the mission image.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param type The selected mission type.
     * @return The corresponding image for the given mission type.
     */
    private Image getImage(final Nation nation, final ReadOnlyObjectProperty<AirMissionType> type) {
        Image image = null;
        if (type.getValue() != null) {
            image = imageResourceProvider.getImage(props.getString(nation.toLower()
                    + "." + type.getValue().toLower() + ".image"));
        }

        return image;
    }

    /**
     * Create a squadron list for the given role.
     *
     * @param role The squadrons role.
     */
    private void createSquadronList(final MissionRole role) {
        squadrons.put(role, new ListViewPair<>("missions", props, imageResourceProvider));
        stackPanes.put(role, new StackPane());
    }

    /**
     * Build the mission node.
     *
     * @return A node containing the mission selection controls.
     */
    private Node buildMissionNode() {
        missionType.setDisable(true);
        Label missionLabel = new Label("Select Mission Type:");
        return new VBox(missionLabel, missionType);
    }

    /**
     * Build the target node.
     *
     * @return A node containing the target selection controls.
     */
    private Node buildTargetNode() {
        target.setDisable(true);
        Label targetLabel = new Label("Select Target:");
        return new VBox(targetLabel, target);
    }

    /**
     * Build the available and selected squadron lists.
     *
     * @return A node containing the available and selected squadron lists.
     */
    private Node buildSquadronLists() {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }

    /**
     * Set the roles.
     */
    private void setRoles() {
        //For Edits there is only one mission type possible. So just grab it.
        List<Tab> tabs = missionType
                .getItems()
                .get(0)
                .getRoles()
                .stream()
                .map(this::buildTab)
                .collect(Collectors.toList());

        roleTabs.get(MissionRole.MAIN).setText(missionType.getItems().get(0).getTitle());

        tabPane.getTabs().addAll(tabs);
    }

    /**
     * build a squadron tab.
     *
     * @param role The squadrons role.
     * @return The tab.
     */
    private Tab buildTab(final MissionRole role) {

        Tab tab = new Tab(role.toString());

        tab.setUserData(role);

        ListViewPair<Squadron> squadronLists = squadrons.get(role);

        squadronLists.setWidth(props.getInt("airfield.dialog.mission.list.width"));
        squadronLists.setHeight(props.getInt("airfield.dialog.mission.list.height"));
        squadronLists.setButtonWidth(props.getInt("airfield.dialog.mission.button.width"));

        squadronLists.clearAll();

        squadronLists.setAvailableTitle("Available Squadrons");
        squadronLists.setAssignedTitle("Assigned Squadrons");
        Node squadronNode = squadronLists.build();

        StackPane stackPane = stackPanes.get(role);
        stackPane.getChildren().add(squadronNode);

        stackPane.setId("mission-squadron-pane");

        tab.setContent(stackPane);

        roleTabs.put(role, tab);

        return tab;
    }
}
