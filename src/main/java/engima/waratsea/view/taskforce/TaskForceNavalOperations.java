package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.ship.ShipDetailsView;
import engima.waratsea.view.ship.ShipViewType;
import engima.waratsea.viewmodel.ship.ShipViewModel;
import engima.waratsea.viewmodel.taskforce.naval.TaskForceNavalViewModel;
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
 * Represents the naval operations of a given task force view. Used by the naval operations didalog.
 */
public class TaskForceNavalOperations {
    private final ViewProps props;
    private final Provider<ShipDetailsView> shipsDetailProvider;

    private TaskForceNavalViewModel viewModel;

    private final TaskForceNavalSummaryView summaryView;

    @Getter private final ChoiceBox<SeaMissionType> missionType = new ChoiceBox<>();

    /**
     * Constructor called by guice.
     *
     * @param shipsDetailProvider Provides ship details views.
     * @param props The view properties.
     * @param summaryView The task force summary view.
     */
    @Inject
    public TaskForceNavalOperations(final Provider<ShipDetailsView> shipsDetailProvider,
                                    final ViewProps props,
                                    final TaskForceNavalSummaryView summaryView) {
        this.props  = props;
        this.shipsDetailProvider = shipsDetailProvider;
        this.summaryView = summaryView;

        missionType.setMinWidth(props.getInt("mission.type.list.width"));
    }

    /**
     * Create the operation tab.
     *
     * @param taskForceVM The task force view model.
     * @return A tab for the given operation.
     */
    public Node build(final TaskForceNavalViewModel taskForceVM) {
        viewModel = taskForceVM;

        Node summary = buildSummary();

        //Node missionNode = buildMissionNode();
        TitledPane shipsNode = buildShipsNode();

        Accordion accordion = new Accordion();
        accordion.getPanes().add(shipsNode);

        HBox hBox = new HBox(summary, accordion);
        hBox.setId("main-pane");

        return hBox;
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
        listView.setMaxHeight(props.getInt("taskforce.details.list.height"));
        listView.setMinWidth(props.getInt("taskforce.details.list.width"));
        listView.setMaxWidth(props.getInt("taskforce.details.list.width"));

        Tab tab = new Tab(type.toString());

        tab.disableProperty().bind(viewModel.getShipPresent().get(type));

        if (ships != null) {
            listView.itemsProperty().bind(ships);
            ShipDetailsView detailsView = shipsDetailProvider.get();
            Node detailsNode = detailsView.build();

            listView
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((o, ov, nv) -> detailsView.bind(nv));

            listView.getSelectionModel().selectFirst();

            HBox hBox = new HBox(listView, detailsNode);
            hBox.setId("ship-hbox");
            tab.setContent(hBox);
        }

        return tab;
    }
}
