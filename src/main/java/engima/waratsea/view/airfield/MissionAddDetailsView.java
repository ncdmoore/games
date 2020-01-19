package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.mission.MissionType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.ListViewPair;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;


public class MissionAddDetailsView {

    private final ViewProps props;

    @Getter
    private final ChoiceBox<MissionType> missionType = new ChoiceBox<>();

    @Getter
    private final ChoiceBox<Target> target = new ChoiceBox<>();

    @Getter
    private final ListViewPair<Squadron> missionList;

    private final StackPane stackPane = new StackPane();
    private final Label errorLabel = new Label();
    private final VBox errorVBox = new VBox(errorLabel);

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param imageResourceProvider Provides images.
     */
    @Inject
    public MissionAddDetailsView(final ViewProps props,
                                 final ImageResourceProvider imageResourceProvider) {
        this.props = props;
        missionType.setMinWidth(props.getInt("mission.type.list.width"));
        target.setMinWidth(props.getInt("mission.type.list.width"));

        missionList = new ListViewPair<>("missions", imageResourceProvider);

        stackPane.setId("mission-squadron-pane");
        errorLabel.setId("mission-error-text");
        errorVBox.setId("mission-error-vbox");
        errorVBox.setMinWidth(270);
        errorVBox.setMaxWidth(270);
        errorVBox.setMaxHeight(60);
        errorVBox.setMinHeight(60);
    }

    /**
     * Show the airbase mission details.
     *
     * @param missionTypes a collection of mission types.
     * @return A node containing the airbase mission details.
     */
    public Node show(final MissionType... missionTypes) {
        missionType.getItems().addAll(missionTypes);

        Label missionLabel = new Label("Select Mission Type:");

        VBox missionVBox = new VBox(missionLabel, missionType);
        Label targetLabel = new Label("Select Target:");

        VBox targetVBox = new VBox(targetLabel, target);

        Node squadronsList = buildSquadronLists();

        VBox vBox = new VBox(missionVBox, targetVBox, squadronsList);
        vBox.setId("main-pane");
        return vBox;
    }

    /**
     * Assign the selected squadron to the mission.
     */
    public void assign() {
        Squadron squadron = missionList
                .getAvailable()
                .getSelectionModel()
                .getSelectedItem();

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
     * Show the error text.
     *
     * @param text The error text.
     */
    public void showError(final String text) {
        errorLabel.setText(text);
        stackPane.getChildren().add(errorVBox);
    }

    /**
     * Hide the error text.
     */
    public void hideError() {
        stackPane.getChildren().remove(errorVBox);
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

        Node missions = missionList.build();

        stackPane.getChildren().add(missions);

        return stackPane;
    }
}
