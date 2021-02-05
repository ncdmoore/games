package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.ship.ShipViewType;
import engima.waratsea.viewmodel.ship.ShipViewModel;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.beans.property.ListProperty;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * Represents the naval operations tab of a given task force view.
 */
public class TaskForceNavalOperations {
    private TaskForceViewModel viewModel;

    private final TaskForceSummaryView summaryView;

    @Getter private final ChoiceBox<SeaMissionType> missionType = new ChoiceBox<>();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param summaryView The task force summary view.
     */
    @Inject
    public TaskForceNavalOperations(final ViewProps props,
                                    final TaskForceSummaryView summaryView) {

        this.summaryView = summaryView;

        missionType.setMinWidth(props.getInt("mission.type.list.width"));
    }

    /**
     * Create the operation tab.
     *
     * @param taskForceVM The task force view model.
     * @return A tab for the given operation.
     */
    public Tab createOperationTab(final TaskForceViewModel taskForceVM) {
        viewModel = taskForceVM;

        Tab tab = new Tab();
        tab.setText("Naval Operations");

        Node summary = buildSummary();


        //Node missionNode = buildMissionNode();
        TitledPane shipsNode = buildShipsNode();

        Accordion accordion = new Accordion();
        accordion.getPanes().add(shipsNode);

        HBox hBox = new HBox(summary, accordion);
        hBox.setId("main-pane");

        tab.setContent(hBox);

        return tab;
    }

    private Node buildSummary() {
        return summaryView
                .build()
                .bind(viewModel);
    }

    /**
     * Build the mission node.
     *
     * @return A node containing the mission selection controls.
     */
    private Node buildMissionNode() {
        Label missionLabel = new Label("Select Mission Type:");

        missionType.getItems().addAll(viewModel.getMissionTypes().getValue());
        missionType.getSelectionModel().select(viewModel.getMission().getValue());

        return new VBox(missionLabel, missionType);
    }

    /**
     * Build the node that contains all of the ships of this task force.
     *
     * @return The ship titled pane - contains all of the ships of this task force.
     */
    private TitledPane buildShipsNode() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ships");
        TabPane tabPane = new TabPane();

        ShipViewType
                .stream()
                .map(this::buildTab)
                .forEach(tab -> tabPane.getTabs().add(tab));

        titledPane.setContent(tabPane);

        return titledPane;
    }

    /**
     * Build a ship type details tab.
     *
     * @param type The ship view type.
     * @return The particular class of ship tab - corresponds to the given ship view type.
     */
    private Tab buildTab(final ShipViewType type) {
        ListProperty<ShipViewModel> ships = viewModel.getShipTypeMap().get(type);

        ListView<ShipViewModel> listView = new ListView<>();

        if (ships != null) {
            listView.itemsProperty().bind(ships);
        }

        Tab tab = new Tab(type.toString());

        tab.setContent(listView);

        return tab;
    }
}
