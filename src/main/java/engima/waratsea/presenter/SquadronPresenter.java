package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.SquadronView;
import javafx.scene.control.Tab;
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

    private Region selectedRegion;

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


    private void registerTabChange() {
        view
                .getNationsTabPane()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> tabChanged(newValue));
    }

    private void selectFirstTab() {
        Tab tab = view
                .getNationsTabPane()
                .getTabs().get(0);

        tabChanged(tab);
    }

    private void tabChanged(final Tab tab) {
        log.info("Tab changed. Tab: {}", tab.getText());
        Nation nation = Nation.valueOf(tab.getText().toUpperCase());
        selectFirstRegion(nation);
        selectFirstAirfield(nation);
    }

    private void registerCallbacks() {
        List<Nation> nations = game
                .getHumanPlayer()
                .getNations()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        nations.forEach(this::registerSelections);

    }

    private void registerSelections(final Nation nation) {
        registerRegionSelection(nation);
        registerAirfieldSelection(nation);
    }

    private void registerRegionSelection(final Nation nation) {
        view
                .getRegions()
                .get(nation)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> regionSelected(newValue));
    }

    private void registerAirfieldSelection(final Nation nation) {
        view
                .getAirfields()
                .get(nation)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> airfieldSelected(newValue));
    }

    private void selectFirstRegion(final Nation nation) {
        view.getRegions().get(nation).getSelectionModel().selectFirst();
    }

    private void selectFirstAirfield(final Nation nation) {
        view.getAirfields().get(nation).getSelectionModel().selectFirst();
    }

    private void regionSelected(final Region region) {
        String selectedNation = view.getNationsTabPane().getSelectionModel().getSelectedItem().getText().toUpperCase();

        log.info("Nation {} region selected {}", selectedNation, region.getName());

        Nation nation = Nation.valueOf(selectedNation);

        view.getAirfields().get(nation).getItems().clear();
        view.getAirfields().get(nation).getItems().addAll(region.getAirfields());

        selectFirstAirfield(nation);
    }

    private void airfieldSelected(final Airfield airfield) {

        if (airfield == null) {    // This happens when the airfield choice box is cleared.
            return;                // We do nothing when the airfield choice box is cleared.
        }

        String selectedNation = view.getNationsTabPane().getSelectionModel().getSelectedItem().getText().toUpperCase();

        log.info("Nation {} Airfield selected {}", selectedNation, airfield.getName());
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
