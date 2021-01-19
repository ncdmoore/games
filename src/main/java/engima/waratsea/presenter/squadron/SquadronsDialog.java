package engima.waratsea.presenter.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogOkOnlyView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.view.squadron.SquadronsView;
import engima.waratsea.viewmodel.squadrons.SideSquadronsViewModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.javatuples.Triplet;

/**
 * Used from the Forces menu to show a player's squadrons.
 */
public class SquadronsDialog {
    private static final String CSS_FILE = "squadrons.css";

    private final Provider<DialogOkOnlyView> dialogProvider;
    private final Provider<SquadronsView> viewProvider;
    private final Provider<SideSquadronsViewModel> viewModelProvider;
    private final CssResourceProvider cssResourceProvider;

    private final ViewProps props;
    private SquadronsView view;
    private Stage stage;

    private final Game game;

    /**
     * The constructor called by guice.
     *
     * @param dialogProvider Provides the dialog.
     * @param viewProvider Provides the view.
     * @param viewModelProvider Provies the view model.
     * @param cssResourceProvider The CSS resource provider.
     * @param props The view properties.
     * @param game The game.
     */
    @Inject
    public SquadronsDialog(final Provider<DialogOkOnlyView> dialogProvider,
                           final Provider<SquadronsView> viewProvider,
                           final Provider<SideSquadronsViewModel> viewModelProvider,
                           final CssResourceProvider cssResourceProvider,
                           final ViewProps props,
                           final Game game) {
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.viewModelProvider = viewModelProvider;
        this.cssResourceProvider = cssResourceProvider;
        this.props = props;
        this.game = game;
    }

    /**
     * Show the squadron's dialog.
     *
     * @param locationType Where the squadron is located on LAND or at SEA.
     */
    public void show(final SquadronLocationType locationType) {
        DialogOkOnlyView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();

        SideSquadronsViewModel viewModel = viewModelProvider.get();

        viewModel.set(game.getHumanSide(), locationType);

        Node viewNode = view.buildAndBind(viewModel);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(game.getHumanPlayer().getSide().toString() + " Squadrons");

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(viewNode);

        registerCallbacks(locationType);
        selectFirstSquadrons(locationType);

        dialog.getOkButton().setOnAction(event -> ok());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register the callbacks.
     *
     * @param locationType The type of squadron: LAND or SEA.
     */
    private void registerCallbacks(final SquadronLocationType locationType) {
        game.
                getHumanPlayer()
                .getSquadronNations(locationType)
                .forEach(this::registerCallbacks);
    }

    /**
     * Select every list view's first squadron.
     *
     * @param locationType The type of squadron: LAND or SEA.
     */
    private void selectFirstSquadrons(final SquadronLocationType locationType) {
        game
                .getHumanPlayer()
                .getSquadronNations(locationType)
                .forEach(this::selectFirstSquadrons);
    }

    /**
     * Register the callbacks for a given nation.
     *
     * @param nation The nation.
     */
    private void registerCallbacks(final Nation nation) {
        // Call backs for when a squadron list view is selected.
        SquadronViewType
                .stream()
                .map(type -> makePair(nation, type))
                .map(this::getListView)
                .map(this::getListViewProperty)
                .forEach(this::setListViewChangeListener);
    }

    /**
     * Make a pair of nation and squadron view type. This is just a utility function.
     *
     * @param nation The nation
     * @param type The squadron view type.
     * @return A pair of nation and squadron view type.
     */
    private Pair<Nation, SquadronViewType> makePair(final Nation nation, final SquadronViewType type) {
        return new Pair<>(nation, type);
    }

    /**
     * Get the squadron list view for the given nation and type of squadron.
     *
     * @param pair The nation and the squadron view type.
     * @return A triplet of nation, squadron view type and the corresponding squadron list view.
     */
    private Triplet<Nation, SquadronViewType, ListView<Squadron>> getListView(final Pair<Nation, SquadronViewType> pair) {
        Nation nation = pair.getKey();
        SquadronViewType type = pair.getValue();
        ListView<Squadron> listView = view.getSquadronTypeTabs(nation).get(type).getListView();
        return new Triplet<>(nation, type, listView);
    }

    /**
     * Get the squadron list view property for the given nation and type of squadron.
     *
     * @param triplet A triplet containing the nation, the squadron view type and the corresponding squadron list view.
     * @return A triplet containing the nation, squadron view type and the corresponding list view property.
     */
    private Triplet<Nation, SquadronViewType, ReadOnlyObjectProperty<Squadron>> getListViewProperty(final Triplet<Nation, SquadronViewType, ListView<Squadron>> triplet) {
        Nation nation = triplet.getValue0();
        SquadronViewType type = triplet.getValue1();
        ReadOnlyObjectProperty<Squadron> property = triplet.getValue2().getSelectionModel().selectedItemProperty();
        return new Triplet<>(nation, type, property);
    }

    /**
     * Add a change listener to the given squadron view type list view.
     *
     * @param triplet A triplet containing the nation, the squadron view type and the corresponding list view property.
     */
    private void setListViewChangeListener(final Triplet<Nation, SquadronViewType, ReadOnlyObjectProperty<Squadron>> triplet) {
        Nation nation = triplet.getValue0();
        SquadronViewType type = triplet.getValue1();
        triplet.getValue2().addListener((v, oldValue, newValue) -> squadronSelected(nation, type));
    }

    /**
     * Select the first squadron in each squadron view list.
     *
     * @param nation The nation.
     */
    private void selectFirstSquadrons(final Nation nation) {
        SquadronViewType
                .stream()
                .map(type -> makePair(nation, type))
                .map(this::getSquadronView)
                .forEach(viewList -> viewList.getSelectionModel().selectFirst());
    }

    /**
     * Get the squadron list view for the given type of squadron.
     *
     * @param pair A pair of nation and squadron view type.
     * @return The squadron list view.
     */
    private ListView<Squadron> getSquadronView(final Pair<Nation, SquadronViewType> pair) {
        Nation nation = pair.getKey();
        SquadronViewType type = pair.getValue();
        return view.getSquadronTypeTabs(nation).get(type).getListView();
    }

    /**
     * Callback for when a squadron is selected.
     *
     * @param nation The nation.
     * @param type The selected squadron view type.
     */
    private void squadronSelected(final Nation nation, final SquadronViewType type) {
        ChoiceBox<SquadronConfig> choiceBox = view.getSquadronTypeTabs(nation).get(type).getChoiceBox();
        choiceBox.getSelectionModel().selectFirst();
    }

    /**
     * Close this dialog.
     */
    private void ok() {
        stage.close();
    }
}
