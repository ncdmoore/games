package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.presenter.squadron.Deployment;
import engima.waratsea.presenter.squadron.SquadronDetailsDialog;
import engima.waratsea.view.SquadronView;
import engima.waratsea.view.WarnDialog;
import engima.waratsea.view.map.marker.preview.AirfieldMarker;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final GameMap gameMap;
    private SquadronView view;
    private Stage stage;

    private Provider<SquadronView> viewProvider;
    private Provider<SquadronDetailsDialog> squadronDetailsDialogProvider;
    private Provider<WarnDialog> warnDialogProvider;

    private Navigate navigate;

    private List<Nation> nations;

    private Airfield selectedAirfield;
    private Squadron selectedAvailableSquadron;
    private Squadron selectedAirfieldSquadron;

    private Map<Nation, Map<LandingType, Deployment>> deployment = new LinkedHashMap<>();

    /**
     * This is the constructor.
     *
     * @param game The game object.
     * @param gameMap The game's map.
     * @param viewProvider The corresponding view.
     * @param squadronDetailsDialogProvider The ship details dialog provider.
     * @param warnDialogProvider The warning dialog provider.
     * @param navigate Provides screen navigation.
     */
    @Inject
    public SquadronPresenter(final Game game,
                             final GameMap gameMap,
                             final Provider<SquadronView> viewProvider,
                             final Provider<SquadronDetailsDialog> squadronDetailsDialogProvider,
                             final Provider<WarnDialog> warnDialogProvider,
                             final Navigate navigate) {
        this.game = game;
        this.gameMap = gameMap;
        this.viewProvider = viewProvider;
        this.squadronDetailsDialogProvider = squadronDetailsDialogProvider;
        this.warnDialogProvider = warnDialogProvider;
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

        initializeDeployment();

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
     * Initialize the deployment data.
     */
    private void initializeDeployment() {
        nations = game
                .getHumanPlayer()
                .getNations()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        nations.forEach(nation -> {
            Map<LandingType, Deployment> deploymentMap = new HashMap<>();

            deploymentMap.put(LandingType.LAND, new Deployment(LandingType.LAND));
            deploymentMap.put(LandingType.SEAPLANE, new Deployment(LandingType.SEAPLANE));

            deployment.put(nation, deploymentMap);
        });

        nations
                .forEach(nation -> view.bindDeploymentStats(nation,
                        new ArrayList<>(deployment
                                .get(nation)
                                .values())));
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
        log.debug("Tab changed from {} to {}", oldTab.getText(), newTab.getText());

        Nation oldNation = determineNation(oldTab.getText());
        Nation newNation = determineNation(newTab.getText());

        // The tab has changed. Remove the old nation's airfields from the map.
        if (oldNation != newNation) {
            removeAirfields(oldNation);
        }

        markAirfields(newNation);

        // This is needed when the tab changes but the region does not. Meaning that last time this tab
        // was active the same region was selected.
        // The call to select the first region may not trigger any changes. Thus, we need this code.
        Region region = view.getRegions().get(newNation).getSelectionModel().getSelectedItem();
        if (region != null) {
            view.setSelectedRegion(newNation, region);    // Tell the view of the newly selected region.
        }

        // This is needed when the tab changes but the airfield does not. Meaning the last time this tab
        // was active the same airfield was selected.
        // The call to select first airfield may not trigger any changes. Thus, we need this code.
        Airfield airfield = view.getAirfields().get(newNation).getSelectionModel().getSelectedItem();
        if (airfield != null) {
            view.setSelectedAirfield(newNation, airfield);  // Tell the view of the newly selected airfield.
            selectedAirfield = airfield;
        }

        selectFirstRegion(newNation);
        selectFirstAirfield(newNation);

        updateDeployment(newNation, LandingType.LAND);
        updateDeployment(newNation, LandingType.SEAPLANE);

        //If the airfield does not change then the select airfield doesn't trigger a callback.
        //Thus, we set the available squadrons explicitly.
        setAvailableSquadrons(newNation, selectedAirfield);

    }

    /**
     * Register callbacks for the region and airfield selection lists for all nations.
     */
    private void registerCallbacks() {
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
     * Update the given nation's squadron deployment totals.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param landingType The landing type of the squadron deployment.
     */
    private void updateDeployment(final Nation nation, final LandingType landingType) {

        List<Squadron> squadrons = game
                .getHumanPlayer()
                .getSquadrons(nation);

        BigDecimal total = squadrons
                .stream()
                .filter(squadron -> squadron.isLandingTypeCompatible(landingType))
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deployed = squadrons
                .stream()
                .filter(squadron -> squadron.isLandingTypeCompatible(landingType))
                .filter(Squadron::isDeployed)
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        deployment.get(nation).get(landingType).setTotalSteps(total);
        deployment.get(nation).get(landingType).setDeployedSteps(deployed);

        view.getDeploymentStats().get(nation).refresh();

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
        log.debug("Selected region {}", region);

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
        log.debug("Selected airfield {}", airfield);

        if (airfield == null) {    // This happens when the airfield choice box is cleared.
            view.clearSquadronRange();
            return;
        }

        Nation nation = determineNation();

        clearAllAirfields();

        selectedAirfield = airfield;

        view.setSelectedAirfield(nation, airfield);

        setAvailableSquadrons(nation, airfield);

        markSquadronRange(selectedAirfield, selectedAvailableSquadron);

    }

    /**
     * Set the available squadrons based on the type of airfield.
     *
     * @param nation The nation BRITISH, ITALIAN, etc.
     * @param airfield The currently selected airfield.
     */
    private void setAvailableSquadrons(final Nation nation, final Airfield airfield) {
        log.debug("Set the available squadrons for airfield: '{}'", airfield);

        List<Squadron> available = game
                .getHumanPlayer()
                .getSquadrons(nation)
                .stream()
                .filter(Squadron::isAvailable)
                .filter(airfield::canSquadronLand)
                .collect(Collectors.toList());

        view.getAvailableSquadrons().getItems().clear();
        view.getAvailableSquadrons().getItems().addAll(available);
    }

    /**
     * Callback when an available squadron is selected.
     *
     * @param squadron The selected available squadron.
     */
    private void availableSquadronSelected(final Squadron squadron) {
        log.debug("Select Squadron {}", squadron);
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
        log.debug("Select Squadron {}", squadron);
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
        AssetMarkerDTO dto = new AssetMarkerDTO(airfield);
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

        AssetMarkerDTO dto = new AssetMarkerDTO(squadron);
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
        log.debug("Deploy {} to {}", selectedAvailableSquadron, selectedAirfield);

        if (selectedAvailableSquadron != null) {
            // Save off the squadron, because when we remove the selected avaiable squadron
            // from the list the selected available squadron changes.
            Squadron squadron = selectedAvailableSquadron;

            AirfieldOperation result = selectedAirfield.addSquadron(squadron);

            if (result == AirfieldOperation.SUCCESS) {                             // Add the squadron to the airfield.
                view.getAvailableSquadrons().getItems().remove(squadron);          // Remove the squadron from the available list.
                view.getAirfieldSquadrons().getItems().add(squadron);              // Add the squadron to the airfield list.

                AircraftBaseType type = squadron.getBaseType();

                updateDeployment(squadron.getNation(), LandingType.LAND);
                updateDeployment(squadron.getNation(), LandingType.SEAPLANE);

                view.updateRegion(determineNation());

                selectedAirfield.getNations().forEach(nation -> {
                    view.getAirfieldCurrentValue().get(nation).setText(selectedAirfield.getCurrentSteps() + "");
                    view.getAirfieldSteps().get(nation).get(type).setText(selectedAirfield.getStepsForType(type) + "");
                });
            } else {
                warnDialogProvider.get().show("Unable to deploy squadron to " + selectedAirfield.getName() + ". " + result + ".");
            }
        }
    }

    /**
     * Remove the selected squadron from the selected airfield.
     */
    private void removeSquadron() {
        log.debug("Remove {}", selectedAirfieldSquadron);

        if (selectedAirfieldSquadron != null) {
            Squadron squadron = selectedAirfieldSquadron;

            view.getAirfieldSquadrons().getItems().remove(squadron);

            selectedAirfield.removeSquadron(squadron);

            updateDeployment(squadron.getNation(), LandingType.LAND);
            updateDeployment(squadron.getNation(), LandingType.SEAPLANE);

            view.updateRegion(determineNation());

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
        Optional<Squadron> anyAvailable = game
                .getHumanPlayer()
                .getNations()
                .stream()
                .flatMap(nation -> game.getHumanPlayer().getSquadrons(nation).stream())
                .filter(Squadron::isAvailable)
                .findAny();

        List<Region> regionsNotSatisfied = gameMap
                .areAllRegionsSatisfied(game.getHumanSide());

        if (anyAvailable.isPresent()) {
            warnNotAllSquadronsDeployed();
        } else if (regionsNotSatisfied.size() > 0) {
            warnNotAllRegionsSatisfied(regionsNotSatisfied);
        } else {
            navigate.goNext(this.getClass(), stage);
        }
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
        String selectedNation = view.getNationsTabPane()
                .getSelectionModel()
                .getSelectedItem()
                .getText()
                .toUpperCase()
                .replace(" ", "_");

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

    /**
     * Warn that not all of the squadrons are deployed.
     */
    private void warnNotAllSquadronsDeployed() {
        warnDialogProvider.get().show("Not all squadrons have been deployed. Squadrons not deployed are lost.");
    }

    /**
     * Warn that some of the regions minimum squadron requirements are not satisfied.
     *
     * @param regions A list of regions for which the minimum squadron requirement is
     *                not satisfied.
     */
    private void warnNotAllRegionsSatisfied(final List<Region> regions) {
        String regionNames = regions.stream().map(Region::getName).collect(Collectors.joining(", "));
        warnDialogProvider.get().show("The following regions do not have their minimum squadron requirement satisfied: " + regionNames + ".");
    }
}
