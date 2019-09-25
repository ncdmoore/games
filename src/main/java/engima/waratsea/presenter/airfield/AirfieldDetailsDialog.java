package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.AirfieldDetailsView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

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
     * Show the arifield details dialog.
     *
     * @param airfield The airfield for which the details are shown.
     */
    public void show(final Airfield airfield) {
        DialogView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        AirfieldDetailsView view = viewProvider.get();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airfield.getTitle() + " " +  airfield.getAirfieldType().getTitle() + " Details");

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(airfield));
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
