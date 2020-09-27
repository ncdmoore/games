package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.squadrons.SquadronsViewModel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * This class contains the squadron list view for squadrons of a particular squadron view type.
 * It also contains the squadron details view.
 */
public class SquadronsTypeView {
    @Getter private final ListView<Squadron> listView = new ListView<>();
    @Getter private final ChoiceBox<SquadronConfig> choiceBox = new ChoiceBox<>();

    private final SquadronDetailsView squadronDetailsView;
    private final ViewProps props;

    private final BoundTitledGridPane airfieldPane = new BoundTitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param detailsView A detailed view of the selected squadron.
     * @param props View properties.
     */
    @Inject
    public SquadronsTypeView(final SquadronDetailsView detailsView,
                             final ViewProps props) {
        this.squadronDetailsView = detailsView;
        this.props = props;
    }

    /**
     * Build the Squadrons view tab.
     *
     * @param viewModel The squadron view model.
     * @return A tab.
     */
    public Node buildAndBind(final SquadronsViewModel viewModel) {
        Node configBox = buildConfigBox();
        Node squadronBox = squadronDetailsView.build();
        Node airfieldBox = buildAirfieldBox();

        VBox vBox = new VBox(configBox, squadronBox, airfieldBox);
        vBox.setId("squadron-type-vbox");

        HBox hBox = new HBox(listView, vBox);
        hBox.setId("squadron-type-hbox");

        listView.itemsProperty().bind(viewModel.getSquadrons());
        choiceBox.itemsProperty().bind(viewModel.getConfigurations());

        airfieldPane.bindStrings(viewModel.getSquadronViewModel().getAirfield());

        viewModel.getSquadronViewModel().getSquadron().bind(listView.getSelectionModel().selectedItemProperty());
        viewModel.getSquadronViewModel().getConfiguration().bind(choiceBox.getSelectionModel().selectedItemProperty());

        squadronDetailsView.bind(viewModel.getSquadronViewModel());

        return hBox;
    }

    /**
     * Build the config box.
     *
     * @return A node containing the configuration box.
     */
    private Node buildConfigBox() {
        Label label = new Label("Squadron Configuration:");
        HBox hbox = new HBox(label, choiceBox);
        hbox.setId("choice-hbox");
        return hbox;
    }

    /**
     * Build the airfield box.
     *
     * @return The airfield box.
     */
    private Node buildAirfieldBox() {
        return airfieldPane
                .setWidth(props.getInt("ship.dialog.detailsPane.width"))
                .setGridStyleId("component-grid")
                .setTitle("Airfield")
                .build();
    }
}
