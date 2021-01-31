package engima.waratsea.presenter.preview;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.presenter.Presenter;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.presenter.squadron.SquadronDetailsDialog;
import engima.waratsea.view.WarnDialog;
import engima.waratsea.view.map.marker.preview.AirfieldMarker;
import engima.waratsea.view.preview.SquadronView;
import engima.waratsea.viewmodel.airfield.AirfieldViewModel;
import engima.waratsea.viewmodel.DeploymentViewModel;
import engima.waratsea.viewmodel.RegionViewModel;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
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

    private final Provider<SquadronView> viewProvider;
    private final Provider<RegionViewModel> regionViewModelProvider;
    private final Provider<AirfieldViewModel> airfieldViewModelProvider;
    private final Provider<SquadronDetailsDialog> squadronDetailsDialogProvider;
    private final Provider<WarnDialog> warnDialogProvider;

    private final Navigate navigate;

    private List<Nation> nations;

    private Airfield selectedAirfield;
    private final Map<Nation, Squadron> selectedAvailableSquadron = new HashMap<>();
    private final Map<Nation, Squadron> selectedAirfieldSquadron = new HashMap<>();

    private final Map<Nation, RegionViewModel> regionViewModelMap = new HashMap<>();
    private final Map<Nation, AirfieldViewModel> airfieldViewModelMap = new HashMap<>();

    private final DeploymentViewModel deploymentViewModel;

    /**
     * This is the constructor.
     *
     * @param game The game object.
     * @param gameMap The game's map.
     * @param viewProvider The corresponding view.
     * @param regionViewModelProvider The region view model provider.
     * @param airfieldViewModelProvider The airfield view model provider.
     * @param deploymentViewModel The deployment view model.
     * @param squadronDetailsDialogProvider The ship details dialog provider.
     * @param warnDialogProvider The warning dialog provider.
     * @param navigate Provides screen navigation.
     */
    //CHECKSTYLE:OFF
    @Inject
    public SquadronPresenter(final Game game,
                             final GameMap gameMap,
                             final Provider<SquadronView> viewProvider,
                             final Provider<RegionViewModel> regionViewModelProvider,
                             final Provider<AirfieldViewModel> airfieldViewModelProvider,
                             final DeploymentViewModel deploymentViewModel,
                             final Provider<SquadronDetailsDialog> squadronDetailsDialogProvider,
                             final Provider<WarnDialog> warnDialogProvider,
                             final Navigate navigate) {
        //CHECKSTYLE:ON
        this.game = game;
        this.gameMap = gameMap;
        this.viewProvider = viewProvider;
        this.regionViewModelProvider = regionViewModelProvider;
        this.airfieldViewModelProvider = airfieldViewModelProvider;
        this.deploymentViewModel = deploymentViewModel;
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
        this.stage = primaryStage;

        view = viewProvider.get();

        nations = game
                .getHumanPlayer()
                .getNations()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        deploymentViewModel.init();

        getAndBindViewModel();

        deploymentViewModel.setModel();

        view.show(stage, game.getScenario());

        markAllAirfields();

        registerCallbacks();
        registerTabChange();
        selectFirstTab();

        view.finish();

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
     * Get the view model and bind it to the view.
     */
    private void getAndBindViewModel() {
        game.getHumanPlayer().getNations().stream().filter(Nation::isSquadronsPresent).forEach(nation -> {
            RegionViewModel regionViewModel = regionViewModelProvider.get();
            regionViewModelMap.put(nation, regionViewModel);

            AirfieldViewModel airfieldViewModel = airfieldViewModelProvider.get();
            airfieldViewModelMap.put(nation, airfieldViewModel);

            view
                    .bind(nation, regionViewModel)
                    .bind(nation, airfieldViewModel)
                    .bind(nation, deploymentViewModel);
        });
    }

    /**
     * Mark each nations airfields.
     */
    private void markAllAirfields() {
        nations.forEach(this::markAirfields);
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

        Nation newNation = determineNation(newTab.getText());

        clearRegionSelection(newNation);
        selectFirstRegion(newNation);
    }

    /**
     * Register callbacks for the region and airfield selection lists for all nations.
     */
    private void registerCallbacks() {
        nations.forEach(this::registerCallback);
    }

    /**
     * Register callbacks for the region and airfield selects for a given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     */
    private void registerCallback(final Nation nation) {
        view
                .getRegions()
                .get(nation)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> regionSelected(newValue));

        view
                .getAirfields()
                .get(nation)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> airfieldSelected(newValue));
        view
                .getAvailableSquadrons()
                .get(nation)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, o, n) -> availableSquadronSelected(nation, n));

        view
                .getAirfieldSquadrons()
                .get(nation)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, o, n) -> airfieldSquadronSelected(nation, n));

        view.getDeployButtons().get(nation).setOnAction(event -> deploySquadron(nation));
        view.getRemoveButtons().get(nation).setOnAction(event -> removeSquadron(nation));
        view.getDetailsButtons().get(nation).setOnAction(event -> showSquadron(nation));
    }

    /**
     * Clear the region selection.
     *
     * @param nation The nation: BRITISH: ITALIAN, etc...
     */
    private void clearRegionSelection(final Nation nation) {
        view.getRegions().get(nation).getSelectionModel().clearSelection();
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
        log.info("Selected region {}", region);

        Nation nation = determineNation();

        regionViewModelMap.get(nation).setModel(region);

        selectFirstAirfield(nation);
    }

    /**
     * An airfield has been selected.
     *
     * @param airfield The selected airfield.
     */
    private void airfieldSelected(final Airfield airfield) {
        log.info("Selected airfield {}", airfield);

        Nation nation = determineNation();

        if (airfield == null) {    // This happens when the airfield choice box is cleared.
            view.clearSquadronRange(nation);
            return;
        }

        clearAllAirfields();

        selectedAirfield = airfield;

        airfieldViewModelMap.get(nation).setModel(nation, airfield);

        view.setSelectedAirfield(nation, airfield);
    }

    /**
     * Callback when an available squadron is selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The selected available squadron.
     */
    private void availableSquadronSelected(final Nation nation, final Squadron squadron) {
        log.debug("Select Squadron {}", squadron);
        selectedAvailableSquadron.put(nation, squadron);

        // Javafx does not call this callback if the squadron does not change.
        // This causes problems when we click between the available squadron list
        // and the airfield squadron list. It's possible for the value to not change.
        // Thus, when the available squadron is selected we clear the selection
        // in the airfield list. That way when we click back into the airfield list
        // the value is guaranteed to change.
        if (squadron != null) {
            view.getAirfieldSquadrons().get(nation).getSelectionModel().clearSelection();
            markSquadronRange(selectedAirfield, squadron);
        }
    }

    /**
     * Callback when an airfield squadron is selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The selected airfield squadron.
     */
    private void airfieldSquadronSelected(final Nation nation, final Squadron squadron) {
        log.debug("Select Squadron {}", squadron);
        selectedAirfieldSquadron.put(nation, squadron);

        // Javafx does not call this callback if the squadron does not change.
        // This causes problems when we click between the available squadron list
        // and the airfield squadron list. It's possible for the value to not change.
        // Thus, when the airfield squadron is selected we clear the selection
        // in the available list. That way when we click back into the available list
        // the value is guaranteed to change.
        if (squadron != null) {
            view.getAvailableSquadrons().get(nation).getSelectionModel().clearSelection();
            markSquadronRange(selectedAirfield, squadron);
        }
    }

    /**
     * Clear all the task force selections.
     */
    private void clearAllAirfields() {
        Nation nation = determineNation();

        game.getHumanPlayer().getAirfields()
                .stream()
                .filter(airfield -> airfield.usedByNation(nation))
                .forEach(airfield -> view.clearAirfield(nation, airfield));
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
     * Mark the given selected squadron's range radius circle from the given airfield.
     *
     * @param airfield The selected airfield.
     * @param squadron The selected squadron.
     */
    private void markSquadronRange(final Airfield airfield, final Squadron squadron) {
        // Temporarily assign the available squadron to the selected airfield so that the range
        // marker is properly displayed.
        boolean isAvailable = squadron.isAvailable();
        if (isAvailable) {
            squadron.setHome(airfield);
        }

        AssetMarkerDTO dto = new AssetMarkerDTO(squadron);
        dto.setNation(squadron.getNation());
        view.markSquadronRangeOnMap(dto);

        // Un-assign the available squadron now that the range has been marked.
        // This is needed to keep from prematurely assigning the squadron.
        // Squadron's are only assigned via the deployment button.
        if (isAvailable) {
            squadron.setHome(null);
        }
    }

    /**
     * Show the squadron's popup.
     *
     * @param event The mouse event click on the squadron marker.
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
        Nation nation = determineNation();
        view.closePopup(nation, event);
    }

    /**
     * Deploy the selected squadron.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void deploySquadron(final Nation nation) {
        log.debug("Deploy {} to {}", selectedAvailableSquadron.get(nation), selectedAirfield);

        if (selectedAvailableSquadron.get(nation) != null) {
            // Save off the squadron, because when we remove the selected available squadron
            // from the list the selected available squadron changes.
            Squadron squadron = selectedAvailableSquadron.get(nation);

            AirfieldOperation result = airfieldViewModelMap.get(nation).deploy(squadron);
            regionViewModelMap.get(nation).refresh();

            if (result == AirfieldOperation.SUCCESS) {
                view.clearSquadronRange(nation);
                view.getAvailableSquadrons().get(nation).getSelectionModel().selectFirst();

            } else {
                warnDialogProvider.get().show("Unable to deploy squadron to " + selectedAirfield.getTitle() + ". " + result + ".");
            }
        }
    }

    /**
     * Remove the selected squadron from the selected airfield.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void removeSquadron(final Nation nation) {
        log.debug("Remove {}", selectedAirfieldSquadron);

        if (selectedAirfieldSquadron.get(nation) != null) {
            Squadron squadron = selectedAirfieldSquadron.get(nation);
            view.clearSquadronRange(nation);
            airfieldViewModelMap.get(nation).remove(squadron);
            regionViewModelMap.get(nation).refresh();
            view.getAirfieldSquadrons().get(nation).getSelectionModel().selectFirst();
        }
    }

    /**
     * Show the selected squadron.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void showSquadron(final Nation nation) {
        getSelected(nation).ifPresent(squadron -> squadronDetailsDialogProvider.get().show(squadron));
    }

    /**
     * Get the current selected squadron.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The selected squadron.
     */
    private Optional<Squadron> getSelected(final Nation nation) {
        Squadron selectedSquadron = null;

        if (selectedAvailableSquadron.get(nation) != null) {
           selectedSquadron = selectedAvailableSquadron.get(nation);
        } else if (selectedAirfieldSquadron.get(nation) != null) {
            selectedSquadron = selectedAirfieldSquadron.get(nation);
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
                .filter(Nation::isSquadronsPresent)
                .flatMap(nation -> game.getHumanPlayer().getSquadrons(nation, SquadronLocationType.LAND).stream())
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
