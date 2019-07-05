package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.presenter.squadron.SquadronDetailsDialog;
import engima.waratsea.view.SquadronView;
import engima.waratsea.view.map.marker.AirfieldMarker;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
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
    private Provider<SquadronDetailsDialog> squadronDetailsDialogProvider;

    private Navigate navigate;

    private Airfield selectedAirfield;
    private Squadron selectedAvailableSquadron;
    private Squadron selectedAirfieldSquadron;

    /**
     * This is the constructor.
     *
     * @param game The game object.
     * @param viewProvider The corresponding view.
     * @param squadronDetailsDialogProvider The ship details dialog provider.
     * @param navigate Provides screen navigation.
     */
    @Inject
    public SquadronPresenter(final Game game,
                             final Provider<SquadronView> viewProvider,
                             final Provider<SquadronDetailsDialog> squadronDetailsDialogProvider,
                             final Navigate navigate) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.squadronDetailsDialogProvider = squadronDetailsDialogProvider;
        this.navigate = navigate;
    }

    /**
     * Creates and shows the squadron view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    @Override
    public void show(final Stage primaryStage) {
        view = viewProvider.get();

        this.stage = primaryStage;

        view.show(stage, game.getScenario());

        registerCallbacks();
        registerTabChange();

        selectFirstTab();

        view.finish();

        view.getDeployButton().setOnAction(event -> deploySquadron());
        view.getRemoveButton().setOnAction(event -> removeSquadron());
        view.getDetailsButton().setOnAction(event -> showSquadron());
        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());
    }

    /**
     * Re show the squadron view.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void reShow(final Stage primaryStage) {
        show(primaryStage);
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
        log.info("Tab changed from {} to {}", oldTab.getText(), newTab.getText());

        Nation oldNation = determineNation(oldTab.getText());
        Nation newNation = determineNation(newTab.getText());

        if (oldNation != newNation) {
            removeAirfields(oldNation);
        }

        markAirfields(newNation);

        Region region = view.getRegions().get(newNation).getSelectionModel().getSelectedItem();
        if (region != null) {
            view.setSelectedRegion(newNation, region);
        }

        Airfield airfield = view.getAirfields().get(newNation).getSelectionModel().getSelectedItem();
        if (airfield != null) {
            view.setSelectedAirfield(newNation, airfield);
            selectedAirfield = airfield;
        }

        selectFirstRegion(newNation);
        selectFirstAirfield(newNation);

        List<Squadron> available = game
                .getHumanPlayer()
                .getSquadrons(newNation)
                .stream()
                .filter(Squadron::isAvailable)
                .collect(Collectors.toList());

        view.getAvailableSquadrons().getItems().clear();
        view.getAvailableSquadrons().getItems().addAll(available);
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

        selectedAirfieldSquadron = null;
        selectedAvailableSquadron = null;

        view.getAvailableSquadrons().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> availableSquadronSelected(newValue));
        view.getAirfieldSquadrons().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> airfieldSquadronSelected(newValue));
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

        view.setSelectedRegion(nation, region);

        selectFirstAirfield(nation);
    }

    /**
     * An airfield has been selected.
     *
     * @param airfield The selected airfield.
     */
    private void airfieldSelected(final Airfield airfield) {
        log.info("Selected airfield {}", airfield);

        if (airfield == null) {    // This happens when the airfield choice box is cleared.
            view.clearSquadronRange();
            return;
        }

        Nation nation = determineNation();

        clearAllAirfields();

        selectedAirfield = airfield;

        view.setSelectedAirfield(nation, airfield);

        markSquadronRange(selectedAirfield, selectedAvailableSquadron);

    }

    /**
     * Callback when an available squadron is selected.
     *
     * @param squadron The selected available squadron.
     */
    private void availableSquadronSelected(final Squadron squadron) {
        log.info("Select Squadron {}", squadron);
        selectedAvailableSquadron = squadron;

        // Javafx does not call this callback if the squadron does not change.
        // This causes problems when we click between the available squadron list
        // and the airfield squadron list. It's possible for the value to not change.
        // Thus, when the available squadron is selected we clear the selection
        // in the airfield list. That way when we click back into the airfield list
        // the value is guaranteed to change.
        if (squadron != null) {
            view.getAirfieldSquadrons().getSelectionModel().clearSelection();
        }

        markSquadronRange(selectedAirfield, squadron);
    }

    /**
     * Callback when an airfield squadron is selected.
     *
     * @param squadron The selected airfield squadron.
     */
    private void airfieldSquadronSelected(final Squadron squadron) {
        log.info("Select Squadron {}", squadron);
        selectedAirfieldSquadron = squadron;

        // Javafx does not call this callback if the squadron does not change.
        // This causes problems when we click between the available squadron list
        // and the airfield squadron list. It's possible for the value to not change.
        // Thus, when the airfield squadron is selected we clear the selection
        // in the available list. That way when we click back into the available list
        // the value is guaranteed to change.
        if (squadron != null) {
            view.getAvailableSquadrons().getSelectionModel().clearSelection();
        }

        markSquadronRange(selectedAirfield, squadron);
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
     * Mark the given selected squadron's range radius circle from the given airfield.
     *
     * @param airfield The selected airfield.
     * @param squadron The selected squadron.
     */
    private void markSquadronRange(final Airfield airfield, final Squadron squadron) {

        if (squadron == null) {
            view.clearSquadronRange();
            return;
        }

        // Temporarily assign the available squadron to the selected airfield so that the range
        // marker is properly displayed.
        boolean isAvailable = squadron.isAvailable();
        if (isAvailable) {
            squadron.setAirfield(airfield);
        }

        TaskForceMarkerDTO dto = new TaskForceMarkerDTO(squadron);
        view.markSquadronRangeOnMap(dto);

        // Un-assign the available squadron now that the range has been marked.
        // This is needed to keep from prematurely assigning the squadron.
        // Squadron's are only assigned via the deployment button.
        if (isAvailable) {
            squadron.setAirfield(null);
        }
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
     *
     * @param event the mouse event.
     */
    private void closePopup(final MouseEvent event) {
        view.closePopup(event);
    }

    /**
     * Deploy the selected squadron.
     */
    private void deploySquadron() {
        log.info("Deploy {} to {}", selectedAvailableSquadron, selectedAirfield);

        if (selectedAvailableSquadron != null) {
            // Save off the squadron, because when we remove the selected avaiable squadron
            // from the list the selected available squadron changes.
            Squadron squadron = selectedAvailableSquadron;

            if (selectedAirfield.addSquadron(squadron)) {                     // Add the squadron to the airfield.
                view.getAvailableSquadrons().getItems().remove(squadron);     // Remove the squadron from the available list.
                view.getAirfieldSquadrons().getItems().add(squadron);         // Add the squadron to the airfield list.

                AircraftBaseType type = squadron.getBaseType();

                selectedAirfield.getNations().forEach(nation -> {
                    view.getAirfieldCurrentValue().get(nation).setText(selectedAirfield.getCurrentSteps() + "");
                    view.getAirfieldSteps().get(nation).get(type).setText(selectedAirfield.getStepsForType(type) + "");
                });
            }
        }
    }

    /**
     * Remove the selected squadron from the selected airfield.
     */
    private void removeSquadron() {
        log.info("Remove {}", selectedAirfieldSquadron);

        if (selectedAirfieldSquadron != null) {
            Squadron squadron = selectedAirfieldSquadron;

            view.getAirfieldSquadrons().getItems().remove(squadron);

            selectedAirfield.removeSquadron(squadron);

            if (squadron.getNation() == determineNation()) {
                view.getAvailableSquadrons().getItems().add(squadron);
            }

            AircraftBaseType type = squadron.getBaseType();

            selectedAirfield.getNations().forEach(nation -> {
                view.getAirfieldCurrentValue().get(nation).setText(selectedAirfield.getCurrentSteps() + "");
                view.getAirfieldSteps().get(nation).get(type).setText(selectedAirfield.getStepsForType(type) + "");
            });
        }
    }

    /**
     * Show the selected squadron.
     */
    private void showSquadron() {
        getSelected()
                .ifPresent(squadron -> squadronDetailsDialogProvider.get().show(squadron));

    }

    /**
     * Get the current selected squadron.
     *
     * @return The selected squadron.
     */
    private Optional<Squadron> getSelected() {
        Squadron selectedSquadron = null;

        if (selectedAvailableSquadron != null) {
           selectedSquadron = selectedAvailableSquadron;
        } else if (selectedAirfieldSquadron != null) {
            selectedSquadron = selectedAirfieldSquadron;
        }

        return Optional.ofNullable(selectedSquadron);
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
