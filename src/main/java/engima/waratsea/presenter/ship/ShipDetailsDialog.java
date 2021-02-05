package engima.waratsea.presenter.ship;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogOkOnlyView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.ship.ShipDetailsView;
import engima.waratsea.viewmodel.ship.ShipViewModel;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * The ship details preview dialog.
 */
@Slf4j
public class ShipDetailsDialog {
    private static final String CSS_FILE = "shipDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogOkOnlyView> dialogProvider;
    private final Provider<ShipDetailsView> viewProvider;
    private final ViewProps props;

    private Stage stage;

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
                             final Provider<DialogOkOnlyView> dialogProvider,
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
     * @param viewModel The view model of the ship for which the details are shown.
     */
    public void show(final ShipViewModel viewModel) {
        ShipDetailsView view = viewProvider.get();          // The ship details view.
        DialogOkOnlyView dialog = dialogProvider.get();     // The dialog view that contains the ship details view.

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ship Details");

        dialog.setWidth(props.getInt("ship.dialog.width"));
        dialog.setHeight(props.getInt("ship.dialog.height"));
        dialog.setContents(view.build(viewModel));               // Add the ship details to the dialog.
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

        view.bind(viewModel);

        dialog.getOkButton().setOnAction(event -> close());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Call back for the ok button.
     */
    private void close() {
        stage.close();
    }
}
