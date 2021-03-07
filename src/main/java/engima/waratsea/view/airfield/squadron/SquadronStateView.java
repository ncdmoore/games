package engima.waratsea.view.airfield.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronSummaryView;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.view.util.TitledGridPane;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
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
 *
 * CSS Styles used.
 *
 * - component-grid
 * - ready-tile-pane
 */
public class SquadronStateView {
    private final ViewProps props;

    @Getter private final Map<SquadronViewType, ListView<SquadronViewModel>> squadrons = new HashMap<>();
    @Getter private final SquadronSummaryView squadronSummaryView;

    private final TitledPane titledPane = new TitledPane();
    private final TitledGridPane stateLabel = new TitledGridPane();

    private SquadronState squadronState;

    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     * @param squadronSummaryView The squadron summary view.
     */
    @Inject
    public SquadronStateView(final ViewProps props,
                             final SquadronSummaryView squadronSummaryView) {
        this.props = props;
        this.squadronSummaryView = squadronSummaryView;
    }

    /**
     * Build the ready details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron's state.
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
                .map(this::buildSquadronList)
                .forEach(node -> tilePane.getChildren().add(node));

        Node summaryNode = squadronSummaryView.build(nation);


        VBox vBox = new VBox(tilePane, summaryNode);

        // Anytime a squadron is selected in the all states view the squadrons state is displayed.
        // We need a state 'node' to hold the value of the squadron's state for display.
        if (state == SquadronState.ALL) {
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
        squadrons.forEach((type, list) -> list
                .itemsProperty()
                .bind(viewModel.getSquadronMap(squadronState).get(type)));

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
    private Node buildSquadronList(final SquadronViewType type) {
        Label title = new Label(type + ":");

        ListView<SquadronViewModel> listView = new ListView<>();
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
