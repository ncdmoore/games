package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.SquadronView;
import engima.waratsea.view.map.AirfieldMarker;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;


/**
 * This class is the presenter for the squadron deployment. The squadron deployment gives the player an overview
 * of all squadrons and the ability to deploy the squadrons to airfields.
 */
@Slf4j
@Singleton
public class SquadronPresenter implements Presenter {
    private final Game game;
    private SquadronView view;
    private Stage stage;

    private Provider<SquadronView> viewProvider;
    private Navigate navigate;

    /**
     * This is the constructor.
     *
     * @param game The game object.
     * @param viewProvider The corresponding view,
     * @param navigate Provides screen navigation.
     */
    @Inject
    public SquadronPresenter(final Game game,
                             final Provider<SquadronView> viewProvider,
                             final Navigate navigate) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.navigate = navigate;
    }

    /**
     * Creates and shows the task force view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    public void show(final Stage primaryStage) {
        view = viewProvider.get();

        this.stage = primaryStage;

        view.show(stage, game.getScenario());

        registerCallbacks();
        registerTabChange();

        selectFirstTab();

        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());
    }

    /**
     * Register callbacks when a nation tab is clicked.
     */
    private void registerTabChange() {
        view
                .getNationsTabPane()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> tabChanged(oldValue, newValue));
    }

    /**
     * Select the first tab. This is need to ensure that a tab is initially selected
     * when the squadron view is presented.
     */
    private void selectFirstTab() {
        Tab tab = view
                .getNationsTabPane()
                .getTabs().get(0);

        tabChanged(tab, tab);
    }

    /**
     * This method is called anytime a tab is changed.
     *
     * @param oldTab The tab that did have focus. The old tab.
     * @param newTab The tab that now contains the focus. The new tab.
     */
    private void tabChanged(final Tab oldTab, final Tab newTab) {
        Nation oldNation = determineNation(oldTab.getText());
        Nation newNation = determineNation(newTab.getText());

        if (oldNation != newNation) {
            removeAirfields(oldNation);
        }

        markAirfields(newNation);


        Airfield airfield = view.getAirfields().get(newNation).getSelectionModel().getSelectedItem();
        if (airfield != null) {
            view.setSelectedAirfield(airfield);
        }

        selectFirstRegion(newNation);
        selectFirstAirfield(newNation);
    }

    /**
     * Register callbacks for the region and airfield selection lists for all nations.
     */
    private void registerCallbacks() {
        List<Nation> nations = game
                .getHumanPlayer()
                .getNations()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        nations.forEach(this::registerSelections);
    }

    /**
     * Register callbacks for the region and airfield selects for a given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     */
    private void registerSelections(final Nation nation) {
        registerRegionSelection(nation);
        registerAirfieldSelection(nation);
    }

    /**
     * Register region selection callback for a given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     */
    private void registerRegionSelection(final Nation nation) {
        view
                .getRegions()
                .get(nation)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> regionSelected(newValue));
    }

    /**
     * Register airfield selection callback for a given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     */
    private void registerAirfieldSelection(final Nation nation) {
        view
                .getAirfields()
                .get(nation)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> airfieldSelected(newValue));
    }

    /**
     * Select the first region for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     */
    private void selectFirstRegion(final Nation nation) {
        view.getRegions().get(nation).getSelectionModel().selectFirst();
    }

    /**
     * Select the first airfield for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     */
    private void selectFirstAirfield(final Nation nation) {
        view.getAirfields().get(nation).getSelectionModel().selectFirst();
    }

    /**
     * A region has been selected.
     *
     * @param region The selected region.
     */
    private void regionSelected(final Region region) {
        Nation nation = determineNation();

        view.getAirfields().get(nation).getItems().clear();
        view.getAirfields().get(nation).getItems().addAll(region.getAirfields());

        selectFirstAirfield(nation);
    }

    /**
     * An airfield has been selected.
     *
     * @param airfield The selected airfield.
     */
    private void airfieldSelected(final Airfield airfield) {
        if (airfield == null) {    // This happens when the airfield choice box is cleared.
            return;                // We do nothing when the airfield choice box is cleared.
        }

        clearAllAirfields();

        view.setSelectedAirfield(airfield);
    }

    /**
     * Clear all the task force selections.
     */
    private void clearAllAirfields() {
        Nation nation = determineNation();

        game.getHumanPlayer().getAirfields()
                .stream()
                .filter(airfield -> airfield.usedByNation(nation))
                .forEach(airfield -> view.clearAirfield(airfield));
    }

    /**
     * Mark the airfields on the preview map.
     *
     * @param nation The nation whose airfields are marked.
     */
    private void markAirfields(final Nation nation) {
        game
                .getHumanPlayer()
                .getAirfields()
                .stream()
                .filter(airfield -> airfield.usedByNation(nation))
                .forEach(airfield -> markAirfield(nation, airfield));
    }

    /**
     * Mark the given airfield on the preview map.
     *
     * @param nation The nation of the airfield marker.
     * @param airfield The airfield that is marked.
     */
    private void markAirfield(final Nation nation, final Airfield airfield) {
        TaskForceMarkerDTO dto = new TaskForceMarkerDTO(airfield);
        dto.setNation(nation);
        dto.setMarkerEventHandler(this::showPopup);
        dto.setPopupEventHandler(this::closePopup);
        view.markAirfieldOnMap(dto);
    }

    /**
     * Remove all the airfield markers of the given nation from the preview map.
     *
     * @param nation The nation of the airfield markers.
     */
    private void removeAirfields(final Nation nation) {
        game
                .getHumanPlayer()
                .getAirfields()
                .stream()
                .filter(airfield -> airfield.usedByNation(nation))
                .forEach(this::removeAirfield);
    }

    /**
     * Remove an airfield marker from the preview map.
     *
     * @param airfield The airfield whose marker is removed.
     */
    private void removeAirfield(final Airfield airfield) {
        view.removeAirfield(airfield);
    }

    /**
     * Show the flotilla's popup.
     *
     * @param event The mouse event click on the flotilla marker.
     */
    private void showPopup(final MouseEvent event) {

        Nation nation = determineNation();

        Shape shape = (Shape) event.getSource();
        AirfieldMarker marker = (AirfieldMarker) shape.getUserData();
        Airfield selected = (Airfield) marker.getAirfield();

        Region region = selected.getRegion(nation);

        view.getRegions().get(nation).getSelectionModel().select(region);

        // Notify view that the airfield has been selected.
        // This keeps the view list in sync with the grid clicks.
        view.getAirfields().get(nation).getSelectionModel().select(selected);

        // Select the airfield. This is needed for clicks that don't change the
        // airfield, but redisplay the popup.
        airfieldSelected(selected);
    }

    /**
     * Close the popup.
     * @param event the mouse event.
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
     * Determine the active nation from the active tab.
     *
     * @return The active nation.
     */
    private Nation determineNation() {
        String selectedNation = view.getNationsTabPane().getSelectionModel().getSelectedItem().getText().toUpperCase().replace(" ", "_");
        return Nation.valueOf(selectedNation);
    }

    /**
     * Convert a string into a Nation enum.
     *
     * @param nation The nation string.
     * @return The nation enum.
     */
    private Nation determineNation(final String nation) {
        return Nation.valueOf(nation.replace(" ", "_").toUpperCase());
    }
}
