package engima.waratsea.presenter.map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.patrol.PatrolGroup;
import engima.waratsea.presenter.airfield.AirfieldDialog;
import engima.waratsea.presenter.airfield.mission.MissionDialog;
import engima.waratsea.presenter.airfield.patrol.PatrolDialog;
import engima.waratsea.presenter.asset.AssetPresenter;
import engima.waratsea.presenter.taskforce.TaskForceAirDialog;
import engima.waratsea.presenter.taskforce.TaskForceNavalDialog;
import engima.waratsea.view.MainMenu;
import engima.waratsea.view.map.GridView;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.view.map.marker.main.BaseMarker;
import engima.waratsea.view.map.marker.main.RegionMarker;
import engima.waratsea.view.map.marker.main.TaskForceMarker;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * This class handles all interaction with the main game map.
 */
@Slf4j
@Singleton
public class MainMapPresenter {
    private final Game game;
    private final GameMap gameMap;
    private final MainMapView mainMapView;
    private final MainMenu mainMenu;

    private final Provider<AirfieldDialog> airfieldDetailsDialogProvider;
    private final Provider<PatrolDialog> patrolDetailsDialogProvider;
    private final Provider<MissionDialog> missionDetailsDialogProvider;
    private final AssetPresenter assetPresenter;
    private final SelectedMapGrid selectedGrid;

    private final Provider<TaskForceNavalDialog> taskForceNavalDialogProvider;
    private final Provider<TaskForceAirDialog> taskForceAirDialogProvider;

    /**
     * The constructor called by guice.
     *
     * @param game The game.
     * @param gameMap The game map.
     * @param viewProvider Provides the main map view.
     * @param menuProvider Provides the main menu.
     * @param airfieldDetailsDialogProvider Provides airfield details dialog.
     * @param patrolDetailsDialogProvider Provides patrol radius details dialog.
     * @param missionDetailsDialogProvider Provides mission arrow details dialog.
     * @param taskForceNavalDialogProvider Provides task force naval dialogs.
     * @param taskForceAirDialogProvider Provides task force air dialogs.
     * @param assetPresenter The asset presenter.
     * @param selectedGrid The selected game grid.
     */
    //CHECKSTYLE:OFF
    @Inject
    public MainMapPresenter(final Game game,
                            final GameMap gameMap,
                            final Provider<MainMapView> viewProvider,
                            final Provider<MainMenu> menuProvider,
                            final Provider<AirfieldDialog> airfieldDetailsDialogProvider,
                            final Provider<PatrolDialog> patrolDetailsDialogProvider,
                            final Provider<MissionDialog> missionDetailsDialogProvider,
                            final Provider<TaskForceNavalDialog> taskForceNavalDialogProvider,
                            final Provider<TaskForceAirDialog> taskForceAirDialogProvider,
                            final AssetPresenter assetPresenter,
                            final SelectedMapGrid selectedGrid) {
        //CHECKSTYLE:ON
        this.game = game;
        this.gameMap = gameMap;

        mainMapView = viewProvider.get();
        mainMenu = menuProvider.get();

        this.airfieldDetailsDialogProvider = airfieldDetailsDialogProvider;
        this.patrolDetailsDialogProvider = patrolDetailsDialogProvider;
        this.missionDetailsDialogProvider = missionDetailsDialogProvider;

        this.taskForceNavalDialogProvider = taskForceNavalDialogProvider;
        this.taskForceAirDialogProvider = taskForceAirDialogProvider;

        this.assetPresenter = assetPresenter;

        this.selectedGrid = selectedGrid;
    }

    /**
     * Setup mouse event handlers for when the base markers are clicked.
     */
    public void setMouseEventHandlers() {
        mainMenu.getShowAirfields().setOnAction(event -> toggleBaseMarkers());
        mainMenu.getShowPorts().setOnAction(event -> toggleBaseMarkers());
        mainMenu.getShowRegions().setOnAction(event -> toggleRegionMarkers());
        mainMenu.getShowGrid().setOnAction(this::toggleGrid);

        Side humanSide =  game.getHumanSide();

        mainMapView.setRegionMouseEnterHandler(humanSide, this::regionMouseEnterHandler);
        mainMapView.setRegionMouseExitHandler(humanSide, this::regionMouseExitHandler);

        mainMapView.setBaseMouseEnterHandler(humanSide, this::baseMouseEnterHandler);
        mainMapView.setBaseMouseExitHandler(humanSide, this::baseMouseExitHandler);
        mainMapView.setBaseMouseEnterHandler(humanSide.opposite(), this::baseMouseEnterHandler);
        mainMapView.setBaseMouseExitHandler(humanSide.opposite(), this::baseMouseExitHandler);
        mainMapView.setBaseMouseEnterHandler(Side.NEUTRAL, this::baseMouseEnterHandler);
        mainMapView.setBaseMouseExitHandler(Side.NEUTRAL, this::baseMouseExitHandler);

        mainMapView.setBaseClickHandler(humanSide, this::humanBaseClickHandler);
        mainMapView.setBaseClickHandler(humanSide.opposite(), this::computerBaseClickHandler);
        mainMapView.setBaseClickHandler(Side.NEUTRAL, this::neutralBaseClickHandler);

        mainMapView.setPatrolRadiusClickHandler(humanSide, this::patrolRadiusClickHandler);
        mainMapView.setMissionArrowClickHandler(humanSide, this::missionArrowClickHandler);

        mainMapView.setAirfieldMenuHandler(humanSide, this::airfieldHandler);
        mainMapView.setTaskForceNavalOperationsMenuHandler(humanSide, this::taskForceNavalOperationsHandler);
        mainMapView.setTaskForceAirOperationsMenuHandler(humanSide, this::taskForceAirOperationsHandler);
        mainMapView.setTaskForceDetachMenuHandler(humanSide, this::taskForceDetachHandler);
        mainMapView.setTaskForceJoinMenuHandler(humanSide, this::taskForceJoinHandler);

        mainMapView.setTaskForceClickHandler(humanSide, this::humanTaskForceClickHandler);

        mainMapView.setGridClickHandler(this::gridClickHandler);
    }

    /**
     * Callback for when the show regions menu item is clicked.
     */
    private void toggleRegionMarkers() {
        mainMapView.toggleRegionMarkers();
    }

    private void toggleGrid(final ActionEvent event) {
        CheckMenuItem item = (CheckMenuItem) event.getSource();
        mainMapView.toggleGrid(item.isSelected());
    }

    /**
     * Call back for when the mouse enters the region label.
     *
     * @param event The mouse event.
     */
    private void regionMouseEnterHandler(final MouseEvent event) {
        VBox regionTitle = (VBox) event.getSource();
        RegionMarker regionMarker = (RegionMarker) regionTitle.getUserData();
        mainMapView.highlightRegion(regionMarker);
    }

    /**
     * Callback for when the mouse exits the region label.
     *
     * @param event The mouse event.
     */
    private void regionMouseExitHandler(final MouseEvent event) {
        VBox regionTitle = (VBox) event.getSource();
        RegionMarker regionMarker = (RegionMarker) regionTitle.getUserData();
        mainMapView.unHighlightRegion(regionMarker);
    }

    /**
     * Callback for when the show airfields map menu item is clicked.
     */
    private void toggleBaseMarkers() {
        Side.stream().forEach(mainMapView::toggleBaseMarkers);
    }

    /**
     * Callback for when a human base grid has a mouse entered event.
     *
     * @param event The mouse event.
     */
    private void baseMouseEnterHandler(final MouseEvent event) {
        VBox imageView = (VBox) event.getSource();
        BaseMarker baseMarker = (BaseMarker) imageView.getUserData();
        mainMapView.highlightBaseMarker(baseMarker);
    }

    /**
     * Callback for when a human base grid has the mouse exit event.
     *
     * @param event The mouse event.
     */
    private void baseMouseExitHandler(final MouseEvent event) {
        VBox imageView = (VBox) event.getSource();
        BaseMarker baseMarker = (BaseMarker) imageView.getUserData();
        mainMapView.unHighlightBaseMarker(baseMarker);
    }

    /**
     * Callback for when a human base grid is clicked.
     *
     * @param event The mouse event.
     */
    private void humanBaseClickHandler(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            VBox imageView = (VBox) event.getSource();
            BaseMarker baseMarker = (BaseMarker) imageView.getUserData();
            boolean selected = mainMapView.selectBaseMarker(baseMarker);

            if (selected) {
                assetPresenter.humanBaseSelected(baseMarker);
            } else {
                assetPresenter.humanBaseUnSelected(baseMarker);
            }

            selectedGrid.set(baseMarker.getBaseGrid().getGameGrid());
        }
    }

    /**
     * Callback for when a human task force grid is clicked.
     *
     * @param event The mouse event.
     */
    private void humanTaskForceClickHandler(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            VBox imageView = (VBox) event.getSource();
            TaskForceMarker taskForceMarker = (TaskForceMarker) imageView.getUserData();

            boolean selected = mainMapView.selectTaskForceMarker(taskForceMarker);
        }
    }

    /**
     * Callback for when the a computer base grid is clicked.
     *
     * @param event The mouse event.
     */
    private void computerBaseClickHandler(final MouseEvent event) {
        VBox imageView = (VBox) event.getSource();

        BaseMarker baseMarker = (BaseMarker) imageView.getUserData();

        selectedGrid.set(baseMarker.getBaseGrid().getGameGrid());
    }

    /**
     * Callback for when the a neutral base grid is clicked.
     *
     * @param event The mouse event.
     */
    private void neutralBaseClickHandler(final MouseEvent event) {
        VBox imageView = (VBox) event.getSource();

        BaseMarker baseMarker = (BaseMarker) imageView.getUserData();

        selectedGrid.set(baseMarker.getBaseGrid().getGameGrid());
    }

    /**
     * Callback for when the airfield menu item of the base marker's context menu is selected.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void airfieldHandler(final ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        Optional<Airfield> airfield = (Optional<Airfield>) item.getUserData();
        airfield.ifPresent(a -> airfieldDetailsDialogProvider.get().show(a, false));
    }

    /**
     * Callback for when the task force marker's naval operations menu item is selected.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void taskForceNavalOperationsHandler(final ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        List<TaskForce> taskForces = (List<TaskForce>) item.getUserData();
        taskForceNavalDialogProvider.get().show(taskForces);
    }

    /**
     * Callback for when the task force marker's air operations menu item is selected.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void taskForceAirOperationsHandler(final ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        List<TaskForce> taskForces = (List<TaskForce>) item.getUserData();
        taskForceAirDialogProvider.get().show(taskForces);
    }

    /**
     * Callback for when the task force marker's detach menu item is selected.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void taskForceDetachHandler(final ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        List<TaskForce> taskForces = (List<TaskForce>) item.getUserData();

        taskForces.forEach(taskForce -> log.info("marker selected: {} - detach", taskForce.getTitle()));
    }

    /**
     * Callback for when the task force marker's detach menu item is selected.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void taskForceJoinHandler(final ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        List<TaskForce> taskForces = (List<TaskForce>) item.getUserData();

        taskForces.forEach(taskForce -> log.info("marker selected: {} - join", taskForce.getTitle()));
    }


    /**
     * Callback for when an airfield's patrol radius is clicked.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void patrolRadiusClickHandler(final MouseEvent event) {
        Circle circle = (Circle) event.getSource();
        List<PatrolGroup> patrols = (List<PatrolGroup>) circle.getUserData();
        patrolDetailsDialogProvider.get().show(patrols);
    }

    /**
     * Callback for when an airfield's mission arrow is clicked.
     *
     * @param event The click event.
     */
    @SuppressWarnings("unchecked")
    private void missionArrowClickHandler(final MouseEvent event) {
        Path arrow = (Path) event.getSource();
        List<AirMission> missions = (List<AirMission>) arrow.getUserData();
        missionDetailsDialogProvider.get().show(missions);
    }

    private void gridClickHandler(final MouseEvent event) {
        GridView gv = mainMapView.getGridView(event);
        GameGrid gameGrid = gameMap.getGrid(gv.getRow(), gv.getColumn());
        selectedGrid.set(gameGrid);
    }
}