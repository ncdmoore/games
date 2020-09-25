package engima.waratsea.view.victory;

import com.google.inject.Inject;
import engima.waratsea.model.victory.VictoryConditionDetails;
import engima.waratsea.model.victory.VictoryType;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.VictoryViewModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * This class represents the victory conditions view shown in the victory dialog box
 * accessed from the game menu bar.
 */
public class VictoryView {
    private final TabPane victoryTabPane = new TabPane();

    @Getter
    private final Map<VictoryType, ListView<VictoryConditionDetails>> victoryConditions = new HashMap<>();
    private final Map<VictoryType, Map<String, Label>> victoryDetailsGrid = new HashMap<>();

    private final VictoryViewModel viewModel;

    @Inject
    public VictoryView(final VictoryViewModel viewModel,
                       final ViewProps props) {
        this.viewModel = viewModel;

        viewModel.init();

        Stream.of(VictoryType.values())
                .forEach(victoryType -> {
                    ListView<VictoryConditionDetails> listView = new ListView<>();
                    listView.setMinWidth(props.getInt("victory.dialog.list.width"));
                    victoryConditions.put(victoryType, listView);
                    victoryDetailsGrid.put(victoryType, new HashMap<>());
                });
    }


    /**
     * Build the victory conditions view.
     *
     * @return The node containing the victory conditions.
     */
    public Node build() {
        victoryTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Stream.of(VictoryType.values())
                .forEach(this::buildTab);

        return victoryTabPane;
    }

    /**
     * Build a victory condition tab for the given type of victory.
     *
     * @param victoryType The type of victory.
     */
    private void buildTab(final VictoryType victoryType) {
        Tab tab = new Tab(victoryType.toString() + " Victory Conditions");

        Label label = new Label(victoryType.toString() + " Victory Condition:");
        VBox vBox = new VBox(label, victoryConditions.get(victoryType));

        Node grid = buildGrid(victoryType);

        HBox hBox = new HBox(vBox, grid);
        hBox.setId("main-pane");

        tab.setContent(hBox);

        if (!viewModel.getVictoryConditions().get(victoryType).getValue().isEmpty()) {
            victoryTabPane.getTabs().add(tab);
        }

        bindTab(victoryType);
    }

    /**
     * Build the victory grid for an individual victory type.
     *
     * @param victoryType The victory type.
     * @return The node containing the grid.
     */
    private Node buildGrid(final VictoryType victoryType) {
        GridPane gridPane = new GridPane();

        gridPane.setId("victory-details-grid");
        AtomicInteger i = new AtomicInteger(1);

        gridPane.add(new Label(), 0, 0);  // Add a blank row for alignment.

        viewModel.getSelectedConditionDetails().get(victoryType).forEach((key, value) -> {
            int row = i.getAndIncrement();
            Label keyLabel = new Label(key);
            Label valueLabel = new Label();
            gridPane.add(keyLabel, 0, row);
            gridPane.add(valueLabel, 1, row);
            victoryDetailsGrid.get(victoryType).put(key, valueLabel);
        });

        return gridPane;
    }

    /**
     * Bind the victory conditions tab elements for the given victory type to the view model.
     *
     * @param victoryType The victory type.
     */
    private void bindTab(final VictoryType victoryType) {
        victoryConditions
                .get(victoryType)
                .itemsProperty()
                .bind(viewModel.getVictoryConditions().get(victoryType));

        viewModel
                .getSelectedConditions()
                .get(victoryType)
                .bind(victoryConditions.
                        get(victoryType)
                        .getSelectionModel()
                        .selectedItemProperty());

        victoryDetailsGrid
                .get(victoryType)
                .forEach((key, label) -> label.textProperty().bind(viewModel
                        .getSelectedConditionDetails()
                        .get(victoryType)
                        .get(key)));
    }
}
