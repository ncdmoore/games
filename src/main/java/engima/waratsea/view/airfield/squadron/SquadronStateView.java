package engima.waratsea.view.airfield.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronSummaryView;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.view.util.TitledGridPane;
import engima.waratsea.viewmodel.NationAirbaseViewModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents the squadron state view of the airfield dialog. This can be used to show all squadrons
 * of at a given state. If no state is provided then all squadrons are shown.
 */
public class SquadronStateView {
    private final ViewProps props;

    @Getter private final Map<SquadronViewType, ListView<Squadron>> squadrons = new HashMap<>();
    @Getter private final SquadronSummaryView squadronSummaryView;

    private final TitledPane titledPane = new TitledPane();
    private final TitledGridPane stateLabel = new TitledGridPane();

    private SquadronState squadronState;

    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     * @param squadronSummaryViewProvider Provides the squadron summary view.
     */
    @Inject
    public SquadronStateView(final ViewProps props,
                             final Provider<SquadronSummaryView> squadronSummaryViewProvider) {
        this.props = props;
        this.squadronSummaryView = squadronSummaryViewProvider.get();
    }

    /**
     * Build the ready details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane containing the ready details of the airfield.
     */
    public SquadronStateView build(final Nation nation, final SquadronState state) {
        squadronState = state;
        String title = Optional
                .ofNullable(state)
                .map(SquadronState::toString)
                .orElse("All");

        titledPane.setText(title + " Squadrons");

        TilePane tilePane = new TilePane();
        tilePane.setId("ready-tile-pane");

        Stream
                .of(SquadronViewType.values())
                .map(this::buildReadyList)
                .forEach(node -> tilePane.getChildren().add(node));

        Node summaryNode = squadronSummaryView.show(nation);

        VBox vBox = new VBox(tilePane, summaryNode);

        // Right now the only client of this class that does not pass in a squadron state is the
        // airfield dialog's view of all squadrons. In a sense, null implies all squadrons.
        // The state of squadrons in the all view varies. Thus, anytime a squadron is selected
        // in the all state the squadrons state is displayed. We need a state 'node' to hold the
        // value of the squadron's state for display.
        if (state == null) {
            buildStateNode();
            vBox.getChildren().add(stateLabel);
        }

        titledPane.setContent(vBox);

        return this;
    }

    /**
     * Bind this view to the given view model.
     *
     * @param viewModel The airfield view model.
     * @return This airfield ready view.
     */
    public TitledPane bind(final NationAirbaseViewModel viewModel) {
        squadrons.forEach((type, list) -> list.itemsProperty().bind(viewModel.getSquadronMap(squadronState).get(type)));
        return titledPane;
    }

    /**
     * Set the optional state view of the selected squadron.
     *
     * @param state The squadrons state.
     */
    public void setState(final SquadronState state) {
        Map<String, String> grid = new HashMap<>();
        grid.put("State:", state.toString());
        stateLabel.buildPane(grid);
        stateLabel.setVisible(true);
    }

    /**
     * Build a single ready list for the given type of squadron.
     *
     * @param type The type of squadron.
     * @return A node containing the given squadron type's ready list.
     */
    private Node buildReadyList(final SquadronViewType type) {
        Label title = new Label(type + ":");

        ListView<Squadron> listView = new ListView<>();
        listView.setMaxHeight(props.getInt("airfield.dialog.ready.list.height"));
        listView.setMinHeight(props.getInt("airfield.dialog.ready.list.height"));
        listView.setMaxWidth(props.getInt("airfield.dialog.ready.list.width"));
        listView.setMinWidth(props.getInt("airfield.dialog.ready.list.width"));

        squadrons.put(type, listView);

        return new VBox(title, listView);
    }

    private void buildStateNode() {
        stateLabel.setText("Squadron State");
        stateLabel.setGridStyleId("component-grid");
        stateLabel.setWidth(props.getInt("airfield.dialog.profile.width"));
        stateLabel.setVisible(false);
    }
}
