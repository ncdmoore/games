package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.AirfieldDetailsView;
import engima.waratsea.view.airfield.AirfieldPatrolView;
import engima.waratsea.view.squadron.SquadronViewType;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The presenter for the airfield details dialog.
 */
@Slf4j
public class AirfieldDetailsDialog {
    private static final String CSS_FILE = "airfieldDetails.css";

    private CssResourceProvider cssResourceProvider;
    private Provider<DialogView> dialogProvider;
    private Provider<AirfieldDetailsView> viewProvider;
    private ViewProps props;

    private Stage stage;

    private AirfieldDetailsView view;

    private Airfield airfield;

    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param props The view properties.
     */

    @Inject
    public AirfieldDetailsDialog(final CssResourceProvider cssResourceProvider,
                                 final Provider<DialogView> dialogProvider,
                                 final Provider<AirfieldDetailsView> viewProvider,
                                 final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.props = props;
    }

    /**
     * Show the airfield details dialog.
     *
     * @param field The airfield for which the details are shown.
     */
    public void show(final Airfield field) {
        airfield = field;

        DialogView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airfield.getTitle() + " " +  airfield.getAirfieldType().getTitle() + " Details");

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(airfield));

        registerHandlers();

        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register all handlers.
     */
    private void registerHandlers() {
        registerPatrolHandlers();
        registerReadyHandlers();
    }

    /**
     * Register the patrol handlers.
     **/
    private void registerPatrolHandlers() {
        airfield.getNations().forEach(nation -> {

            AirfieldPatrolView patrolView = view
                    .getAirfieldPatrolView()
                    .get(nation);

            Stream.of(PatrolType.values()).forEach(patrolType -> {
                patrolView
                        .getAvailableList(patrolType)
                        .getSelectionModel()
                        .selectedItemProperty()
                        .addListener((v, oldValue, newValue) -> patrolAvailableSquadronSelected(newValue, patrolType));

                patrolView
                        .getAssignedList(patrolType)
                        .getSelectionModel()
                        .selectedItemProperty()
                        .addListener((v, oldValue, newValue) -> patrolAssignedSquadronSelected(newValue, patrolType));

                patrolView
                        .getAddButton(patrolType)
                        .setOnAction(this::patrolAddSquadron);

                patrolView
                        .getRemoveButton(patrolType)
                        .setOnAction(this::patrolRemoveSquadron);
            });
        });
    }

    /**
     * Register handlers for when a squadron in a ready list is selected.
     ***/
    private void registerReadyHandlers() {
        airfield.getNations().forEach(nation ->
            view
                    .getAirfieldReadyView()
                    .get(nation)
                    .getReadyLists()
                    .forEach((type, listview) -> listview
                            .getSelectionModel()
                            .selectedItemProperty()
                            .addListener((v, oldValue, newValue) -> readySquadronSelected(newValue)))
        );
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        airfield.getNations().forEach(nation ->
            Stream.of(PatrolType.values()).forEach(patrolType -> {
                List<Squadron> currentAssigned = view.getAirfieldPatrolView()
                        .get(nation)
                        .getAssignedList(patrolType)
                        .getItems();

                List<Squadron> previousAssigned = airfield
                        .getPatrol(patrolType)
                        .getSquadrons(nation);

                List<Squadron> added = ListUtils.subtract(currentAssigned, previousAssigned);
                List<Squadron> removed = ListUtils.subtract(previousAssigned, currentAssigned);

                added.forEach(squadron -> airfield
                        .getPatrol(patrolType)
                        .addSquadron(squadron));

                removed.forEach(squadron -> airfield
                        .getPatrol(patrolType)
                        .removeSquadron(squadron));
            })
        );

        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        stage.close();
    }

    /**
     * Add a squadron to the corresponding patrol which is determined from the add button.
     *
     * @param event The button action event.
     */
    private void patrolAddSquadron(final ActionEvent event) {
        Button button = (Button) event.getSource();
        PatrolType type = (PatrolType) button.getUserData();

        Nation nation = determineNation();

        Squadron squadron = view
                .getAirfieldPatrolView()
                .get(nation)
                .assignPatrol(type);

        view.getAirfieldReadyView()
                .get(nation)
                .remove(squadron);
    }

    /**
     * Remove a squadron from the corresponding patrol which is determined from the remove button.
     *
     * @param event The button action event.
     */
    private void patrolRemoveSquadron(final ActionEvent event) {
        Button button = (Button) event.getSource();
        PatrolType type = (PatrolType) button.getUserData();

        Nation nation = determineNation();

        Squadron squadron = view
                .getAirfieldPatrolView()
                .get(nation)
                .removePatrol(type);

        view.getAirfieldReadyView()
                .get(nation)
                .add(squadron);
    }

    /**
     * A squadron from the given patrol type's available list has been selected.
     *
     * @param patrolSquadron The selected available squadron.
     * @param patrolType The given patrol type.
     */
    private void patrolAvailableSquadronSelected(final Squadron patrolSquadron, final PatrolType patrolType) {
        Optional.ofNullable(patrolSquadron).ifPresent(squadron -> {
            Nation nation = determineNation();

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .selectAvailableSquadron(patrolSquadron, patrolType);
        });
    }

    /**
     * A squadron from the given patrol type's assigned list has been selected.
     *
     * @param patrolSquadron The selected assigned squadron.
     * @param patrolType The given patrol type.
     */
    private void patrolAssignedSquadronSelected(final Squadron patrolSquadron, final PatrolType patrolType) {
        Optional.ofNullable(patrolSquadron).ifPresent(squadron -> {
            Nation nation = determineNation();

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .selectAssignedSquadron(patrolSquadron, patrolType);
        });
    }

    /**
     * Call back for a ready squadron selected.
     *
     * @param readySquadron The selected ready squadron.
     */
    private void readySquadronSelected(final Squadron readySquadron) {

        Optional.ofNullable(readySquadron).ifPresent(squadron -> {
            SquadronViewType type = SquadronViewType.get(readySquadron.getType());

            Nation nation = determineNation();

            //Clear all the other ready listview selections. If on clicking a listview
            //that already has a squadron selected and the same squadron is selected,
            //then no notification is sent. To avoid this we clear all other listviews
            //anytime a ready squadron is selected. This way when a listview is selected
            //a notification is guaranteed to be sent.
            view
                    .getAirfieldReadyView()
                    .get(nation)
                    .getReadyLists()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != type)
                    .forEach(entry -> entry.getValue().getSelectionModel().clearSelection());

            view
                    .getAirfieldReadyView()
                    .get(nation)
                    .getSquadronSummaryView()
                    .setSelectedSquadron(readySquadron);
        });
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
}
