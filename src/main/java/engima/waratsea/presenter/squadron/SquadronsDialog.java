package engima.waratsea.presenter.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.rules.Rules;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogOkOnlyView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.view.squadron.SquadronsView;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SquadronsDialog {
    private static final String CSS_FILE = "squadrons.css";

    private final Provider<DialogOkOnlyView> dialogProvider;
    private final Provider<SquadronsView> viewProvider;
    private final CssResourceProvider cssResourceProvider;

    private ViewProps props;
    private SquadronsView view;
    private Stage stage;

    private final Game game;
    private final Rules rules;

    /**
     * The constructor called by guice.
     *
     * @param dialogProvider Provides the dialog.
     * @param viewProvider Provides the view.
     * @param cssResourceProvider The CSS resource provider.
     * @param props The view properties.
     * @param game The game.
     * @param rules The game rules.
     */
    @Inject
    public SquadronsDialog(final Provider<DialogOkOnlyView> dialogProvider,
                           final Provider<SquadronsView> viewProvider,
                           final CssResourceProvider cssResourceProvider,
                           final ViewProps props,
                           final Game game,
                           final Rules rules) {
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.cssResourceProvider = cssResourceProvider;
        this.props = props;
        this.game = game;
        this.rules = rules;
    }

    /**
     * Show the squadron's dialog.
     *
     * @param locationType Where the squadron is located on LAND or at SEA.
     */
    public void show(final SquadronLocationType locationType) {
        DialogOkOnlyView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(game.getHumanPlayer().getSide().toString() + " Squadrons");

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(game.getHumanPlayer(), locationType));

        registerCallbacks();
        selectFirstSquadrons();

        dialog.getOkButton().setOnAction(event -> ok());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register the callbacks.
     */
    private void registerCallbacks() {
        game.
                getHumanPlayer()
                .getNations()
                .forEach(this::registerCallbacks);
    }

    /**
     * Select every list view's first squadron.
     */
    private void selectFirstSquadrons() {
        game
                .getHumanPlayer()
                .getNations()
                .forEach(this::selectFirstSquadrons);
    }

    /**
     * Register the callbacks for a given nation.
     *
     * @param nation The nation.
     */
    private void registerCallbacks(final Nation nation) {
        // Call backs for when a squadron list view is selected.
        Stream.of(SquadronViewType.values())
                .map(type -> makePair(nation, type))
                .map(this::getListView)
                .map(this::getListViewProperty)
                .forEach(this::setListViewChangeListener);

        Stream.of(SquadronViewType.values())
                .map(type -> makePair(nation, type))
                .map(this::getChoiceBox)
                .map(this::getChoiceBoxProperty)
                .forEach(this::setChoiceBoxChangeListener);
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
     * Get the squadron configuration choice box for the given nation and type of squadron.
     *
     * @param pair The nation and the squadron view type.
     * @return A triplet of nation, squadron view type and the corresponding squadron configuration choice box.
     */
    private Triplet<Nation, SquadronViewType, ChoiceBox<SquadronConfig>> getChoiceBox(final Pair<Nation, SquadronViewType> pair) {
        Nation nation = pair.getKey();
        SquadronViewType type = pair.getValue();
        ChoiceBox<SquadronConfig> choiceBox = view.getSquadronTypeTabs(nation).get(type).getChoiceBox();
        return new Triplet<>(nation, type, choiceBox);
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
     * Get the squadron choice box property for the given nation and type of squadron.
     *
     * @param triplet A triplet containing the nation, the squadron view type and the corresponding squadron choice box.
     * @return A triplet containing the nation, squadron view type and the corresponding choice box property.
     */
    private Triplet<Nation, SquadronViewType, ReadOnlyObjectProperty<SquadronConfig>> getChoiceBoxProperty(final Triplet<Nation, SquadronViewType, ChoiceBox<SquadronConfig>> triplet) {
        Nation nation = triplet.getValue0();
        SquadronViewType type = triplet.getValue1();
        ReadOnlyObjectProperty<SquadronConfig> property = triplet.getValue2().getSelectionModel().selectedItemProperty();
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
        triplet.getValue2().addListener((v, oldValue, newValue) -> squadronSelected(nation, type, newValue));
    }

    /**
     * Add a change listener to the given squadron view type squadron configuration choice box.
     *
     * @param triplet A triplet containing the nation, the squadron view type and the corresponding choice box property.
     */
    private void setChoiceBoxChangeListener(final Triplet<Nation, SquadronViewType, ReadOnlyObjectProperty<SquadronConfig>> triplet) {
        Nation nation = triplet.getValue0();
        SquadronViewType type = triplet.getValue1();
        triplet.getValue2().addListener((v, oldValue, newValue) -> configSelected(nation, type, newValue));
    }

    /**
     * Select the first squadron in each squadron view list.
     *
     * @param nation The nation.
     */
    private void selectFirstSquadrons(final Nation nation) {
        Stream.of(SquadronViewType.values())
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
     * @param squadron The selected squadron.
     */
    private void squadronSelected(final Nation nation, final SquadronViewType type, final Squadron squadron) {
        ChoiceBox<SquadronConfig> choiceBox = view.getSquadronTypeTabs(nation).get(type).getChoiceBox();

        List<SquadronConfig> configurations = squadron
                .getAircraft()
                .getConfiguration()
                .stream()
                .filter(rules::isSquadronConfigAllowed)
                .sorted()
                .collect(Collectors.toList());

        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(new ArrayList<>(configurations));
        choiceBox.getSelectionModel().selectFirst();

        SquadronConfig configuration = choiceBox.getValue();

        view
                .getSquadronTypeTabs(nation)
                .get(type)
                .setSquadron(squadron, configuration);
    }

    /**
     * Callback for when a squadron configuration is selected.
     *
     * @param nation The nation.
     * @param type The selected squadron view type.
     * @param configuration The selected squadron's configuration.
     */
    private void configSelected(final Nation nation, final SquadronViewType type, final SquadronConfig configuration) {
        if (configuration != null) {

            Squadron squadron = view
                    .getSquadronTypeTabs(nation)
                    .get(type).getListView()
                    .getSelectionModel()
                    .getSelectedItem();

            view
                    .getSquadronTypeTabs(nation)
                    .get(type)
                    .setSquadron(squadron, configuration);
        }
    }

    /**
     * Close this dialog.
     */
    private void ok() {
        stage.close();
    }
}
