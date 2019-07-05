package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.presenter.dto.map.MinefieldDTO;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.MinefieldView;
import engima.waratsea.view.map.marker.MineMarker;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


/**
 * This class is the presenter for the minefield placement view. The minefield placement view gives the player an the opportunity
 * to place minefields in certain "zones". The scenario will define in which zones a side may place minefields. Not all scenarios
 * allow for the placement of minefields.
 */
@Slf4j
@Singleton
public class MinefieldPresenter implements Presenter {
    private final Game game;

    private MinefieldView view;
    private Stage stage;

    private Provider<MinefieldView> viewProvider;
    private Navigate navigate;

    private Minefield selectedMinefield;

    /**
     * The constructor called by guice.
     *
     * @param game The game object.
     * @param viewProvider Provides the minefield view.
     * @param navigate Provides screen navigation.
     */
    @Inject
    public MinefieldPresenter(final Game game,
                              final Provider<MinefieldView> viewProvider,
                              final Navigate navigate) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.navigate = navigate;
    }

    /**
     * Creates and shows the minefield view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    @Override
    public void show(final Stage primaryStage) {
        this.stage = primaryStage;

        view = viewProvider.get();

        setMinefields();

        view.show(stage, game.getScenario());


        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());

        view.getMinefields().getSelectionModel().selectFirst();
    }

    /**
     * Re show the minefield view.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void reShow(final Stage primaryStage) {
        show(primaryStage);
    }

    /**
     * Set the human player's minefields.
     */
    private void setMinefields() {
        view.setMinefields(game.getHumanPlayer().getMinefields());
        view.getMinefields().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> selectMinefield(newValue));
    }

    /**
     * Select a minefield.
     *
     * @param minefield The selected minefield.
     */
    private void selectMinefield(final Minefield minefield) {
        selectedMinefield = minefield;
        clearMinefields();
        highlightMinefield(minefield);
        view.setSelectedMinefield(minefield);
    }

    /**
     * Highlight the selected minefield.
     *
     * @param minefield The selected minefield.
     */
    private void highlightMinefield(final Minefield minefield) {
        MinefieldDTO dto = new MinefieldDTO();
        dto.setMinefield(minefield);
        dto.setAddMineHandler(this::addMinefieldClicked);
        view.highlightMinefield(dto);
    }

    /**
     * Clear all minefield highlights.
     */
    private void clearMinefields() {
        game
                .getHumanPlayer()
                .getMinefields()
                .forEach(this::clearMinefield);
    }

    /**
     * Clear a given minefield's highlight.
     *
     * @param minefield The minefield whose highlight is cleared.
     */
    private void clearMinefield(final Minefield minefield) {
        MinefieldDTO dto = new MinefieldDTO();
        dto.setMinefield(minefield);
        view.removeMinefieldHighlight(dto);
    }

    /**
     * Callback for when a minefield grid is clicked to add or activate the minefield.
     *
     * @param event The mouse click event.
     */
    private void addMinefieldClicked(final MouseEvent event) {
        MinefieldDTO dto = new MinefieldDTO();
        dto.setMinefield(selectedMinefield);
        dto.setEvent(event);
        dto.setRemoveMineHandler(this::removeMinefieldClicked);
        view.markMine(dto);
    }

    /**
     * Callback for when a minefield grid is clicked to remove or de-activate the minefield.
     *
     * @param event The mouse click event.
     */
    private void removeMinefieldClicked(final MouseEvent event) {

        MineMarker mineMarker = (MineMarker) ((Node) event.getSource()).getUserData();

        // Make sure that we are clicking on a minefield grid that is for the currently
        // selected minefield. Otherwise, we end up with a mess if we allow non selected
        // minefield clicks. We can easily end up removing the minefield from the wrong
        // minefield model object.
        //
        // We could also disable the marker mouse click events. But, then we have to
        // re-enable them when the minefield is the selected minefield. That would be
        // tricky. Best thing is just to simulate a disabled marker by checking the
        // selected minefield.
        if (mineMarker.getMinefield() == selectedMinefield) {
            MinefieldDTO dto = new MinefieldDTO();
            dto.setMinefield(selectedMinefield);
            dto.setEvent(event);
            view.unMarkMine(dto);
        }
    }

    /**
     * Call back for the continue button.
     */
    private void continueButton() {
        navigate.goNext(this.getClass(), stage);
    }

    /**
     * Call back for the back button.
     */
    private void backButton() {
        navigate.goPrev(this.getClass(), stage);
    }
}
