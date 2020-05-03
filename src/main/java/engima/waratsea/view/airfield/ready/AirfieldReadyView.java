package engima.waratsea.view.airfield.ready;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronSummaryView;
import engima.waratsea.view.squadron.SquadronViewType;
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
import java.util.stream.Stream;

public class AirfieldReadyView {
    private final ViewProps props;

    @Getter private final Map<SquadronViewType, ListView<Squadron>> readySquadrons = new HashMap<>();
    @Getter private SquadronSummaryView squadronSummaryView;

    private TitledPane titledPane = new TitledPane();

    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     * @param squadronSummaryViewProvider Provides the squadron summary view.
     */
    @Inject
    public AirfieldReadyView(final ViewProps props,
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
    public AirfieldReadyView build(final Nation nation) {
        titledPane.setText("Ready");

        TilePane tilePane = new TilePane();
        tilePane.setId("ready-tile-pane");

        Stream
                .of(SquadronViewType.values())
                .map(this::buildReadyList)
                .forEach(node -> tilePane.getChildren().add(node));

        Node summaryNode = squadronSummaryView.show(nation);

        VBox vBox = new VBox(tilePane, summaryNode);

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
        readySquadrons.forEach((type, list) -> list.itemsProperty().bind(viewModel.getReadySquadrons().get(type)));
        return titledPane;
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

        readySquadrons.put(type, listView);

        return new VBox(title, listView);
    }
}
