package engima.waratsea.presenter.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronDetailsView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * The squadron details preview dialog.
 */
@Slf4j
public class SquadronDetailsDialog {
    private static final String CSS_FILE = "squadronDetails.css";

    private CssResourceProvider cssResourceProvider;
    private Provider<DialogView> dialogProvider;
    private Provider<SquadronDetailsView> viewProvider;
    private ViewProps props;

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
    public SquadronDetailsDialog(final CssResourceProvider cssResourceProvider,
                                 final Provider<DialogView> dialogProvider,
                                 final Provider<SquadronDetailsView> viewProvider,
                                 final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.props = props;
    }

    /**
     * Show the dialog.
     *
     * @param squadron The squadron for which the details are shown.
     */
    public void show(final Squadron squadron) {
        SquadronDetailsView view = viewProvider.get();    // The squadron details view.
        DialogView dialog = dialogProvider.get();     // The dialog view that contains the squadron details view.

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Squadron Details");

        dialog.setWidth(props.getInt("ship.dialog.width"));
        dialog.setHeight(props.getInt("ship.dialog.height"));
        dialog.setContents(view.show(squadron));              // Add the squadron details to the dialog.
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

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
