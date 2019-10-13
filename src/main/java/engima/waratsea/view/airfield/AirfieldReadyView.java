package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronSummaryView;
import engima.waratsea.view.squadron.SquadronViewType;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AirfieldReadyView {
    private final ViewProps props;

    private Airfield airfield;

    @Getter
    private final Map<SquadronViewType, ListView<Squadron>> readyLists = new HashMap<>();

    @Getter
    private SquadronSummaryView squadronSummaryView;

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
     * Set the airfield.
     *
     * @param field The airfield.
     * @return The airfield patrol view.
     */
    public AirfieldReadyView setAirfield(final Airfield field) {
        this.airfield = field;
        return this;
    }

    /**
     * Build the ready details pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane containing the ready details of the airfield.
     */
    public TitledPane show(final Nation nation) {

        TitledPane titledPane = new TitledPane();

        titledPane.setText("Ready");

        Map<SquadronViewType, List<Squadron>> squadrons = airfield.getSquadronMap(nation)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> SquadronViewType.get(e.getKey()),
                        Map.Entry::getValue,
                        ListUtils::union,
                        LinkedHashMap::new));

        TilePane tilePane = new TilePane();
        tilePane.setId("ready-tile-pane");

        squadrons.forEach((type, list) -> {

            Label title = new Label(type + ":");

            ListView<Squadron> listView = new ListView<>();
            listView.getItems().addAll(list);
            listView.setMaxHeight(props.getInt("airfield.dialog.ready.list.height"));
            listView.setMinHeight(props.getInt("airfield.dialog.ready.list.height"));
            listView.setMaxWidth(props.getInt("airfield.dialog.ready.list.width"));
            listView.setMinWidth(props.getInt("airfield.dialog.ready.list.width"));

            readyLists.put(type, listView);

            VBox vBox = new VBox(title, listView);

            tilePane.getChildren().add(vBox);
        });

        Node node = squadronSummaryView.show(nation);

        VBox vBox = new VBox(tilePane, node);

        titledPane.setContent(vBox);

        return titledPane;
    }

    /**
     * Add a squadron to the corresponding ready list.
     *
     * @param squadron The squadron to add.
     */
    public void add(final Squadron squadron) {
        Optional.ofNullable(squadron).ifPresent(s -> {
            SquadronViewType type = SquadronViewType.get(s.getType());
            readyLists.get(type).getItems().add(s);
        });
    }
    /**
     * Remove a squadron from the corresponding ready list.
     *
     * @param squadron The squadron to remove.
     */
    public void remove(final Squadron squadron) {
        Optional.ofNullable(squadron).ifPresent(s -> {
            SquadronViewType type = SquadronViewType.get(s.getType());
            readyLists.get(type).getItems().remove(s);

            // If the squadron that is currently displayed in the review summary
            // then hide the summary, since this squadron is no longer ready.
            // If any remaining ready squadron is selected, then the summary
            // for that squadron will be shown. Therefore, there is no need to
            // clean up the summary.
            if (squadronSummaryView.getSelectedSquadron() == squadron) {
                squadronSummaryView.hide();
            }

        });
    }
}
