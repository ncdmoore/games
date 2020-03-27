package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.List;

/**
 * This class contains the squadron list view for squadrons of a particular squadron view type.
 * It also contains the squadron details view.
 */
public class SquadronsTypeView {
    @Getter private ListView<Squadron> listView = new ListView<>();
    @Getter private ChoiceBox<SquadronConfig> choiceBox = new ChoiceBox<>();

    private SquadronDetailsView squadronDetailsView;

    /**
     * Constructor called by guice.
     *
     * @param detailsView A detailed view of the selected squadron.
     */
    @Inject
    public SquadronsTypeView(final SquadronDetailsView detailsView) {
        this.squadronDetailsView = detailsView;
    }

    /**
     * Build the Squadrons view tab.
     *
     * @param nation The nation.
     * @param type The type of squadrons to display in this tab.
     * @param squadrons The list of squadrons to display in this tab.
     * @return A tab.
     */
    public Node build(final Nation nation, final SquadronViewType type, final List<Squadron> squadrons) {
        listView
                         .getItems()
                         .addAll(squadrons);

        Node configBox = buildConfigBox();
        Node squadronBox = squadronDetailsView.build(nation);

        VBox vBox = new VBox(configBox, squadronBox);
        vBox.setId("squadron-type-vbox");

        HBox hBox = new HBox(listView, vBox);
        hBox.setId("squadron-type-hbox");
        return hBox;
    }

    /**
     * Set the selected squadron.
     *
     * @param squadron The selected squadron.
     * @param config The selected squadron's configuration.
     */
    public void setSquadron(final Squadron squadron, final SquadronConfig config) {
        squadronDetailsView.setSquadron(squadron, config);
    }

    /**
     * Build the config box.
     *
     * @return A node contianing the configuration box.
     */
    private Node buildConfigBox() {
        Label label = new Label("Squadron Configuration:");
        HBox hbox = new HBox(label, choiceBox);
        hbox.setId("choice-hbox");
        return hbox;
    }
}
