package engima.waratsea.presenter.submarine;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.submarine.Submarine;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogOkOnlyView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.submarine.SubmarineDetailsView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * The submarine details preview dialog.
 */
@Slf4j
public class SubmarineDetailsDialog {
    private static final String CSS_FILE = "subDetails.css";

    private CssResourceProvider cssResourceProvider;
    private Provider<DialogOkOnlyView> dialogProvider;
    private Provider<SubmarineDetailsView> viewProvider;
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
    public SubmarineDetailsDialog(final CssResourceProvider cssResourceProvider,
                                  final Provider<DialogOkOnlyView> dialogProvider,
                                  final Provider<SubmarineDetailsView> viewProvider,
                                  final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.props = props;
    }

    /**
     * Show the dialog.
     *
     * @param submarine The submarine for which the details are shown.
     */
    public void show(final Submarine submarine) {
        SubmarineDetailsView view = viewProvider.get();    // The submarine details view.
        DialogOkOnlyView dialog = dialogProvider.get();     // The dialog view that contains the ship details view.

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Submarine Details");

        dialog.setWidth(props.getInt("ship.dialog.width"));
        dialog.setHeight(props.getInt("ship.dialog.height"));
        dialog.setContents(view.show(submarine));              // Add the ship details to the dialog.
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
