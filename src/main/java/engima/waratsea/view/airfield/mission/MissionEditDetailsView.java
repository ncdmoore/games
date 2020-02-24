package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.Mission;
import engima.waratsea.model.base.airfield.mission.MissionType;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the mission edit dialog view details.
 */
public class MissionEditDetailsView implements MissionDetailsView {

    private final ViewProps props;

    @Getter
    private final ChoiceBox<MissionType> missionType = new ChoiceBox<>();

    @Getter
    private final ChoiceBox<Target> target = new ChoiceBox<>();

    @Getter
    private final TargetView targetView;

    @Getter
    private final ListViewPair<Squadron> missionList;

    @Getter
    private final ImageView imageView = new ImageView();

    @Getter
    private final SquadronSummaryView squadronSummaryView;

    private final StackPane stackPane = new StackPane();

    @Setter
    private Airbase airbase;


    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param targetView The target view.
     * @param imageResourceProvider Provides images.
     * @param squadronSummaryViewProvider Provides squadron summaries.
     */
    @Inject
    public MissionEditDetailsView(final ViewProps props,
                                  final TargetView targetView,
                                  final ImageResourceProvider imageResourceProvider,
                                  final Provider<SquadronSummaryView> squadronSummaryViewProvider) {
        this.props = props;
        this.targetView = targetView;

        missionType.setMinWidth(props.getInt("mission.type.list.width"));
        target.setMinWidth(props.getInt("mission.type.list.width"));

        missionList = new ListViewPair<>("missions", imageResourceProvider);

        squadronSummaryView = squadronSummaryViewProvider.get();
    }

    /**
     * Show the airbase mission details.
     *
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param mission The mission that is edited.
     * @return A node containing the airbase mission details.
     */
    public Node show(final Nation nation, final Mission mission) {
        missionType.getItems().add(mission.getType());
        target.getItems().add(mission.getTarget());

        missionType.setDisable(true);
        target.setDisable(true);

        Label missionLabel = new Label("Select Mission Type:");
        VBox missionVBox = new VBox(missionLabel, missionType);

        Label targetLabel = new Label("Select Target:");
        VBox targetVBox = new VBox(targetLabel, target);

        VBox choiceBoxes = new VBox(missionVBox, targetVBox);
        choiceBoxes.setId("choices-pane");

        Node targetDetailsBox = targetView.build(airbase);

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
     * Assign the selected squadron to the mission.
     *
     * @param squadron The squadron assigned.
     */
    public void assign(final Squadron squadron) {
        missionList.add(squadron);
    }

    /**
     * Remove the selected squadron from the mission.
     */
    public void remove() {
        Squadron squadron = missionList
                .getAssigned()
                .getSelectionModel()
                .getSelectedItem();

        missionList.remove(squadron);
    }

    /**
     * Build the available and selected squadron lists.
     *
     * @return A node containing the available and selected squadron lists.
     */
    private Node buildSquadronLists() {
        missionList.setWidth(props.getInt("airfield.dialog.mission.list.width"));
        missionList.setHeight(props.getInt("airfield.dialog.mission.list.height"));
        missionList.setButtonWidth(props.getInt("airfield.dialog.mission.button.width"));

        missionList.clearAll();

        Node missionsNode = missionList.build();

        stackPane.getChildren().add(missionsNode);

        return stackPane;
    }
}
