package engima.waratsea.presenter.ship;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.ship.ShipDetailsView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * The ship details preview dialog.
 */
@Slf4j
public class ShipDetailsDialog {
    private static final String CSS_FILE = "shipDetails.css";

    private CssResourceProvider cssResourceProvider;
    private Provider<DialogView> dialogProvider;
    private Provider<ShipDetailsView> viewProvider;
    private ViewProps props;

    private Stage stage;
    private ShipDetailsView view;

    /**
     * The constructor called by guice.
     *
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents of the dialog.
     * @param props The view properties.
     */
    @Inject
    public ShipDetailsDialog(final CssResourceProvider cssResourceProvider,
                             final Provider<DialogView> dialogProvider,
                             final Provider<ShipDetailsView> viewProvider,
                             final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.props = props;
    }

    /**
     * Show the dialog.
     *
     * @param ship The id of the ship for which the details are shown.
     */
    public void show(final Ship ship) {
        view = viewProvider.get();    // The ship details view.
        DialogView dialog = dialogProvider.get();     // The dialog view that contains the ship details view.

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ship Details");

        dialog.setWidth(props.getInt("ship.dialog.width"));
        dialog.setHeight(props.getInt("ship.dialog.height"));
        dialog.setContents(view.show(ship));              // Add the ship details to the dialog.
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

        dialog.getOkButton().setOnAction(event -> close());

        selectSquadron();

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Call back for the ok button.
     */
    private void close() {
        stage.close();
    }

    /**
     * Select the first squadron.
     **/
    private void selectSquadron() {
        view.getSquadrons().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> squadronSelected(newValue));
        view.getSquadrons().getSelectionModel().selectFirst();
    }

    /**
     * Callback for when a ship squadron is selected.
     *
     * @param squadron The selected squadron.
     */
    private void squadronSelected(final Squadron squadron) {
        view.selectSquadron(squadron);
    }
}
