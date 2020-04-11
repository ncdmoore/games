package engima.waratsea.presenter.preview;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaType;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.motorTorpedoBoat.MotorTorpedoBoat;
import engima.waratsea.model.submarine.Submarine;
import engima.waratsea.presenter.Presenter;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.presenter.motorTorpedoBoat.MotorTorpedoDetailsDialog;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.presenter.submarine.SubmarineDetailsDialog;
import engima.waratsea.viewmodel.FlotillaViewModel;
import engima.waratsea.view.preview.FlotillaView;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class is the presenter for the flotilla summary view. The flotilla summary gives the player an overview
 * of all flotillas before the game is started.
 */
@Slf4j
@Singleton
public class FlotillaPresenter implements Presenter {
    private final Provider<FlotillaView> viewProvider;
    private final Provider<FlotillaViewModel> viewModelProvider;
    private final Provider<SubmarineDetailsDialog> subDetailsDialogProvider;
    private final Provider<MotorTorpedoDetailsDialog> mtbDetailsDialogProvider;

    private final Map<FlotillaType, FlotillaViewModel> viewModelMap = new HashMap<>();

    private final Game game;
    private FlotillaView view;
    private Stage stage;

    private final Navigate navigate;

    /**
     * This is the constructor.
     *
     * @param game The game object.
     * @param viewProvider The corresponding view,
     * @param viewModelProvider The view model provider.
     * @param navigate Provides screen navigation.
     * @param subDetailsDialogProvider The submarine details dialog provider.
     * @param mtbDetailsDialogProvider The MTB details dialog provider.
     */
    @Inject
    public FlotillaPresenter(final Game game,
                             final Provider<FlotillaView> viewProvider,
                             final Provider<FlotillaViewModel> viewModelProvider,
                             final Navigate navigate,
                             final Provider<SubmarineDetailsDialog> subDetailsDialogProvider,
                             final Provider<MotorTorpedoDetailsDialog> mtbDetailsDialogProvider) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.viewModelProvider = viewModelProvider;
        this.navigate = navigate;
        this.subDetailsDialogProvider = subDetailsDialogProvider;
        this.mtbDetailsDialogProvider = mtbDetailsDialogProvider;
    }

    /**
     * Show the primary stage.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void show(final Stage primaryStage) {
        this.stage = primaryStage;

        this.view = viewProvider.get();

        Stream.of(FlotillaType.values()).forEach(type -> {
            FlotillaViewModel flotillaViewModel = viewModelProvider.get();
            viewModelMap.put(type, flotillaViewModel);
            view.bind(type, flotillaViewModel);
        });

        view.show(stage, game.getScenario());

        setFlotillas();
        registerCallbacks();
        registerTabChange();
        selectFirstTab();

        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());
    }

    /**
     * Re show the primary stage.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void reShow(final Stage primaryStage) {
        show(primaryStage);
    }

    /**
     * Set the flotillas in the view.
     */
    private void setFlotillas() {
        Stream
                .of(FlotillaType.values())
                .filter(this::hasFlotilla)
                .forEach(type -> view
                        .setFlotillas(type, game.getHumanPlayer()
                        .getFlotillas(type)));
    }

    /**
     * Register callbacks for the flotilla selection.
     */
    private void registerCallbacks() {
        Stream
                .of(FlotillaType.values())
                .filter(this::hasFlotilla)
                .forEach(type -> view
                        .getFlotillas()
                        .get(type)
                        .getSelectionModel()
                        .selectedItemProperty()
                        .addListener((v, oldValue, newValue) -> flotillaSelected(newValue)));
    }

    /**
     * Register callbacks when a flotilla tab is clicked.
     */
    private void registerTabChange() {
        view
                .getFlotillaTabPane()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> tabChanged(oldValue, newValue));
    }

    /**
     * Select the first tab. This is need to ensure that a tab is initially selected
     * when the squadron view is presented.
     */
    private void selectFirstTab() {
        ObservableList<Tab> tabs = view
                .getFlotillaTabPane()
                .getTabs();

        if (!tabs.isEmpty()) {
            Tab tab = tabs.get(0);
            tabChanged(tab, tab);
        }
    }

    /**
     * This method is called anytime a tab is changed.
     *
     * @param oldTab The tab that did have focus. The old tab.
     * @param newTab The tab that now contains the focus. The new tab.
     */
    private void tabChanged(final Tab oldTab, final Tab newTab) {
        log.debug("Tab changed from {} to {}", oldTab.getText(), newTab.getText());

        FlotillaType oldFlotillaType = determineFlotillaType(oldTab.getText());
        FlotillaType newFlotillaType = determineFlotillaType(newTab.getText());

        if (oldFlotillaType != newFlotillaType) {
            removeFlotillas(oldFlotillaType);
        }

        markFlotillas(newFlotillaType);

        Flotilla flotilla = view.getFlotillas().get(newFlotillaType).getSelectionModel().getSelectedItem();
        if (flotilla != null) {
            log.info("Set selected flotilla {}", flotilla);
            view.setSelectedFlotilla(flotilla);
            view.getVesselButtons()
                    .forEach(button -> button.setOnAction(this::displayVesselDialog));
        }

        selectFirstFlotilla(newFlotillaType);
    }



    /**
     * Mark the flotillas on the preview map.
     *
     * @param type The flotilla type: SUBMARINE or MTB.
     */
    private void markFlotillas(final FlotillaType type) {
        game
                .getHumanPlayer()
                .getFlotillas(type)
                .forEach(this::markFlotilla);
    }

    /**
     * Mark the given flotilla on the preview map.
     *
     * @param flotilla The selected flotilla.
     */
    private void markFlotilla(final Flotilla flotilla) {
        AssetMarkerDTO dto = new AssetMarkerDTO(flotilla);
        dto.setMarkerEventHandler(this::showPopup);
        dto.setPopupEventHandler(this::closePopup);
        view.markFlotillaOnMap(dto);
    }

    /**
     * Select the first flotilla for the given flotilla type.
     *
     * @param flotillaType The flotilla type: SUBMARINE or MTB.
     */
    private void selectFirstFlotilla(final FlotillaType flotillaType) {
        view.getFlotillas().get(flotillaType).getSelectionModel().selectFirst();
    }

    /**
     * Select a flotilla.
     *
     * @param flotilla The selected flotilla.
     */
    private void flotillaSelected(final Flotilla flotilla) {
        clearAllFlotillas();

        FlotillaType flotillaType = determineFlotillaType();

        viewModelMap.get(flotillaType).setModel(flotilla);

        view.setSelectedFlotilla(flotilla);

        view.getVesselButtons()
                .forEach(button -> button.setOnAction(this::displayVesselDialog));
    }

    /**
     * Clear all the flotilla selections.
     */
    private void clearAllFlotillas() {
        FlotillaType flotillaType = determineFlotillaType();

        game.getHumanPlayer().getFlotillas(flotillaType)
                .forEach(flotilla -> view.clearFlotilla(flotilla));
    }

    /**
     * Remove all the flotilla's of the given type.
     *
     * @param flotillaType The flotilla type: SUBMARINE or MTB.
     */
    private void removeFlotillas(final FlotillaType flotillaType) {
        game.getHumanPlayer().getFlotillas(flotillaType)
                .forEach(flotilla -> view.removeFlotilla(flotilla));
    }

    /**
     * Show the flotilla's popup.
     *
     * @param event The mouse event click on the flotilla marker.
     */
    @SuppressWarnings("unchecked")
    private void showPopup(final MouseEvent event) {
        Shape shape = (Shape) event.getSource();
        List<Flotilla> selected = (List<Flotilla>) shape.getUserData();

        Flotilla flotilla = selected.get(0);

        // Notify view that the flotilla has been selected.
        // This keeps the view list in sync with the grid clicks.
        view.getFlotillas().get(FlotillaType.SUBMARINE).getSelectionModel().select(flotilla);

        // Select the flotilla. This is needed for clicks that don't change the
        // flotilla, but redisplay the popup.
        flotillaSelected(flotilla);
    }

    /**
     * Hide the flotilla's popup.
     *
     * @param event The mouse event click on the popup.
     */
    private void closePopup(final MouseEvent event) {
        view.closePopup(event);
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

    /**
     * Display the submarine details dialog.
     *
     * @param event The mouse click event.
     */
    private void displayVesselDialog(final ActionEvent event) {
        log.info("Show vessel dialog");

        Button button = (Button) event.getSource();

        Class<?> c = button.getUserData().getClass();

        log.info("sub button class {}", c);

        if (c == Submarine.class) {
            Submarine sub = (Submarine) button.getUserData();
            subDetailsDialogProvider.get().show(sub);
        } else {
            MotorTorpedoBoat mtb = (MotorTorpedoBoat) button.getUserData();
            mtbDetailsDialogProvider.get().show(mtb);
        }
    }

    /**
     * Determine if the player has flotilla's of the given type.
     *
     * @param type The type of flotilla: SUBMARINE or MTB.
     * @return True if the player has the given type of flotilla. False otherwise.
     */
    private boolean hasFlotilla(final FlotillaType type) {
        return game.getHumanPlayer().hasFlotilla(type);
    }


    /**
     * Convert a string into a FlotillaType enum.
     *
     * @param type The flotillaType string.
     * @return The nation enum.
     */
    private FlotillaType determineFlotillaType(final String type) {
        return FlotillaType.valueOf(type.replace(" ", "_").toUpperCase());
    }

    /**
     * Determine the active flotilla from the active tab.
     *
     * @return The active flotilla.
     */
    private FlotillaType determineFlotillaType() {
        String selectedNation = view.getFlotillaTabPane().getSelectionModel().getSelectedItem().getText().toUpperCase().replace(" ", "_");
        return FlotillaType.valueOf(selectedNation);
    }
}
