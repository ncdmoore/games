package engima.waratsea.presenter.squadron;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogOkOnlyView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronDetailsView;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * The squadron details preview dialog.
 */
@Slf4j
public class SquadronDetailsDialog {
    private static final String CSS_FILE = "squadronDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogOkOnlyView> dialogProvider;
    private final Provider<SquadronViewModel> viewModelProvider;
    private final Provider<SquadronDetailsView> viewProvider;
    private final ViewProps props;

    private Stage stage;

    /**
     * The constructor called by guice.
     *
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewModelProvider Provices the view model for this dialog.
     * @param viewProvider Provides the view contents of the dialog.
     * @param props The view properties.
     */
    @Inject
    public SquadronDetailsDialog(final CssResourceProvider cssResourceProvider,
                                 final Provider<DialogOkOnlyView> dialogProvider,
                                 final Provider<SquadronViewModel> viewModelProvider,
                                 final Provider<SquadronDetailsView> viewProvider,
                                 final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewModelProvider = viewModelProvider;
        this.viewProvider = viewProvider;
        this.props = props;
    }

    /**
     * Show the dialog.
     *
     * @param squadron The squadron for which the details are shown.
     */
    public void show(final Squadron squadron) {
        SquadronViewModel viewModel = viewModelProvider.get();
        SquadronDetailsView view = viewProvider.get();         // The squadron details view.
        DialogOkOnlyView dialog = dialogProvider.get();        // The dialog view that contains the squadron details view.

        viewModel.set(squadron);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Squadron Details");

        dialog.setWidth(props.getInt("ship.dialog.width"));
        dialog.setHeight(props.getInt("ship.dialog.height"));
        dialog.setContents(view.build());                     // Add the squadron details to the dialog.
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
