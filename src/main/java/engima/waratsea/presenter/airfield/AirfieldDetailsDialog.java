package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.AirfieldDetailsView;
import engima.waratsea.view.squadron.SquadronViewType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

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
     * @param airfield The airfield for which the details are shown.
     */
    public void show(final Airfield airfield) {
        DialogView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airfield.getTitle() + " " +  airfield.getAirfieldType().getTitle() + " Details");

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(airfield));

        registerReadyHandlers(airfield);

        dialog.getOkButton().setOnAction(event -> close());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register handlers for when a squadron in a ready list is selected.
     *
     * @param airfield The airfield for which the details are shown.
     **/
    private void registerReadyHandlers(final Airfield airfield) {
        airfield.getNations().forEach(nation ->
            view
                    .getReadyLists(nation)
                    .forEach((type, listview) -> listview
                            .getSelectionModel()
                            .selectedItemProperty()
                            .addListener((v, oldValue, newValue) -> readySquadronSelected(newValue)))
        );
    }

    /**
     * Call back for the ok button.
     */
    private void close() {
        stage.close();
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
                    .getReadyLists(nation)
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != type)
                    .forEach(entry -> entry.getValue().getSelectionModel().clearSelection());

            //squadronDialogProvider.get().show(readySquadron);

            view.getReadySquadronSummaryView()
                    .get(nation)
                    .setSquadron(readySquadron);
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
