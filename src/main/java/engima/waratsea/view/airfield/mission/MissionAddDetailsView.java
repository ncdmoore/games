package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronSummaryView;
import engima.waratsea.view.util.ListViewPair;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Represents the mission add dialog details view.
 */
public class MissionAddDetailsView implements MissionDetailsView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    @Getter
    private final ChoiceBox<AirMissionType> missionType = new ChoiceBox<>();

    @Getter
    private final ChoiceBox<Target> target = new ChoiceBox<>();

    @Getter
    private final TargetView targetView;

    @Getter
    private final TabPane tabPane = new TabPane();

    @Getter
    private final Map<MissionRole, Tab> roleTabs = new HashMap<>();

    private final Map<MissionRole, ListViewPair<Squadron>> squadrons = new HashMap<>();

    @Getter
    private final SquadronSummaryView squadronSummaryView;

    @Getter
    private final ImageView imageView = new ImageView();

    private final Map<MissionRole, StackPane> stackPanes = new HashMap<>();
    private final Map<MissionRole, Label> errorLabel = new HashMap<>();
    private final Map<MissionRole, VBox> errorVBox = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param targetView The target view.
     * @param imageResourceProvider Provides images.
     * @param squadronSummaryViewProvider Provides squadron summaries.
     */
    @Inject
    public MissionAddDetailsView(final ViewProps props,
                                 final TargetView targetView,
                                 final ImageResourceProvider imageResourceProvider,
                                 final Provider<SquadronSummaryView> squadronSummaryViewProvider) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
        this.targetView = targetView;

        missionType.setMinWidth(props.getInt("mission.type.list.width"));
        target.setMinWidth(props.getInt("mission.type.list.width"));

        Stream.of(MissionRole.values())
                .forEach(this::createSquadronList);

        squadronSummaryView = squadronSummaryViewProvider.get();
    }

    /**
     * Show the airbase mission details.
     *
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param missionTypes a collection of mission types.
     * @return A node containing the airbase mission details.
     */
    public Node show(final Nation nation, final AirMissionType... missionTypes) {

        missionType.getItems().addAll(missionTypes);

        Label missionLabel = new Label("Select Mission Type:");
        VBox missionVBox = new VBox(missionLabel, missionType);

        Label targetLabel = new Label("Select Target:");
        VBox targetVBox = new VBox(targetLabel, target);

        VBox choiceBoxes = new VBox(missionVBox, targetVBox);
        choiceBoxes.setId("choices-pane");

        Node targetDetailsBox = targetView.build();

        HBox hBox = new HBox(choiceBoxes, imageView);
        hBox.setId("target-hbox");

        Node squadronsList = buildSquadronLists();

        Node squadronSummaryNode = squadronSummaryView.show(nation);

        VBox leftVBox = new VBox(hBox, squadronsList);
        leftVBox.setId("left-vbox");

        HBox mainHBox = new HBox(leftVBox, targetDetailsBox);
        mainHBox.setId("main-hbox");

        VBox mainVBox = new VBox(mainHBox, squadronSummaryNode);

        mainVBox.setId("main-pane");
        return mainVBox;
    }

    /**
     * Get the mission list.
     *
     * @param role The squadron mission role.
     * @return The available and assigned mission list view pair.
     */
    @Override
    public ListViewPair<Squadron> getSquadronList(final MissionRole role) {
        return squadrons.get(role);
    }

    /**
     * Assign the selected squadron to the mission.
     *
     * @param squadron The squadron that is added.
     * @param role The squadron's mission role.
     */
    @Override
    public void assign(final Squadron squadron, final MissionRole role) {
        squadrons.get(role)
                .add(squadron);

        Stream.of(MissionRole.values())
                .filter(otherRole -> otherRole != role)
                .forEach(otherRole -> squadrons.get(otherRole).removeFromAvailable(squadron));

        targetView.getViewMap()
                .get(missionType.getSelectionModel().getSelectedItem())
                .addSquadron(squadron, target.getSelectionModel().getSelectedItem());
    }

    /**
     * Remove the selected squadron from the mission.
     *
     * @param role The squadron's mission role.
     */
    @Override
    public void remove(final MissionRole role) {
        Squadron squadron = squadrons
                .get(role)
                .getAssigned()
                .getSelectionModel()
                .getSelectedItem();

        AirMissionType selectedMissionType = missionType.getSelectionModel().getSelectedItem();
        Target selectedTarget = target.getSelectionModel().getSelectedItem();

        squadrons
                .get(role)
                .remove(squadron);

        Stream.of(MissionRole.values())
                .filter(otherRole -> otherRole != role)
                .forEach(otherRole -> {
                    if (squadron.canDoRole(otherRole) && squadron.inRange(selectedTarget, selectedMissionType, otherRole)) {
                        squadrons.get(otherRole).addToAvailable(squadron);
                    }
                });

        targetView.getViewMap()
                .get(missionType.getSelectionModel().getSelectedItem())
                .removeSquadron(squadron, selectedTarget);
    }

    /**
     * Show the error text.
     *
     * @param role The squadron's mission role.
     * @param text The error text.
     */
    public void showError(final MissionRole role, final String text) {
        StackPane stackPane = stackPanes.get(role);
        errorLabel.get(role).setText(text);
        stackPane.getChildren().add(errorVBox.get(role));
    }

    /**
     * Hide the error text.
     * @param role The squadron's mission role.
     */
    public void hideError(final MissionRole role) {
        StackPane stackPane = stackPanes.get(role);
        stackPane.getChildren().remove(errorVBox.get(role));
    }

    /**
     * Build the available and selected squadron lists.
     *
     * @return A node containing the available and selected squadron lists.
     */
    private Node buildSquadronLists() {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<Tab> tabs = Stream.of(MissionRole.values())
                .map(this::buildTab)
                .collect(Collectors.toList());

        tabPane.getTabs().addAll(tabs);

        return tabPane;
    }

    /**
     * build a squadron tab.
     *
     * @param role The squadrons role.
     *
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

        Node squadronNode = squadronLists.build();

        StackPane stackPane = stackPanes.get(role);
        stackPane.getChildren().add(squadronNode);

        stackPane.setId("mission-squadron-pane");

        tab.setContent(stackPane);

        roleTabs.put(role, tab);

        return tab;
    }

    /**
     * Create a squadron list for the given role.
     *
     * @param role The squadrons role.
     */
    private void createSquadronList(final MissionRole role) {
        squadrons.put(role, new ListViewPair<>("missions", imageResourceProvider));
        stackPanes.put(role, new StackPane());

        Label label = new Label();
        label.setId("mission-error-text");
        errorLabel.put(role, label);

        VBox vBox = new VBox(label);
        errorVBox.put(role, vBox);
        vBox.setId("mission-error-vbox");
        vBox.setMinWidth(props.getInt("mission.error.box.width"));
        vBox.setMaxWidth(props.getInt("mission.error.box.width"));
        vBox.setMaxHeight(props.getInt("mission.error.box.height"));
        vBox.setMinHeight(props.getInt("mission.error.box.height"));
    }
}
