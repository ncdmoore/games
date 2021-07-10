package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.presenter.asset.AssetPresenter;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.viewmodel.airfield.RealAirbaseViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


/**
 * Represents the airfield details dialog. This is were the airfield missions and patrols are assigned.
 */
@Slf4j
public class AirfieldDialog {
    private static final String CSS_FILE = "airfieldDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<MainMapView> mapViewProvider;
    private final AirbasePresenter airfieldPresenter;
    private final AssetPresenter assetPresenter;

    private final ViewProps props;
    private Stage stage;

    private MainMapView mapView;

    private Airbase airbase;

    private RealAirbaseViewModel viewModel;

    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param mapViewProvider Provides the view of the main game map.
     * @param airfieldPresenter The airfield presenter.
     * @param assetPresenter Provides the asset presenters.
     * @param props The view properties.
     */

    //CHECKSTYLE:OFF
    @Inject
    public AirfieldDialog(final CssResourceProvider cssResourceProvider,
                          final Provider<DialogView> dialogProvider,
                          final Provider<MainMapView> mapViewProvider,
                          final AirbasePresenter airfieldPresenter,
                          final AssetPresenter assetPresenter,
                          final ViewProps props) {
        //CHECKSTYLE:ON
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.mapViewProvider = mapViewProvider;
        this.airfieldPresenter = airfieldPresenter;
        this.assetPresenter = assetPresenter;
        this.props = props;
    }

    /**
     * Show the airfield details dialog.
     *
     * @param base The airfield for which the details are shown.
     * @param showPatrols Set to true to show the patrols pane by default.
     *                    Otherwise, the mission pane is shown.
     */
    public void show(final Airbase base, final boolean showPatrols) {
        airbase = base;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airbase.getTitle() + " " +  airbase.getAirbaseType().getTitle() + " Details");

        DialogView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

        mapView = mapViewProvider.get();

        viewModel = assetPresenter
                .getAirfieldAssetPresenter()
                .getViewModel(airbase);

        Node dialogContents = airfieldPresenter.build(viewModel, showPatrols);

        dialog.setContents(dialogContents);
        registerHandlers(dialog);

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register callback handlers.
     *
     * @param dialog This dialog's view.
     */
    private void registerHandlers(final DialogView dialog) {
        registerNationTabHandler();

        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());
    }

    /**
     * Register the nation tab changed callback handler.
     */
    private void registerNationTabHandler() {
        airfieldPresenter.registerNationTabHandler((ov, oldTab, newTab) -> nationTabChanged(newTab));
    }

    /**
     * The airfield dialog's nation tab has changed. Update the nation tab in the asset summary.
     * This way the airfield's dialog nation tab and the airfield's asset summary nation tab are
     * always in sync.
     *
     * @param newTab The newly selected tab.
     */
    private void nationTabChanged(final Tab newTab) {
        Nation nation = (Nation) (newTab.getUserData());

        assetPresenter
                .getAirfieldAssetPresenter()
                .setNation(nation, airbase);
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        viewModel.save();

        mapView.toggleBaseMarkers(airbase);

        assetPresenter
                .getAirfieldAssetPresenter()
                .hide(airbase, false);

        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        assetPresenter
                .getAirfieldAssetPresenter()
                .hide(airbase, true);

        mapView.toggleBaseMarkers(airbase);

        stage.close();
    }
}
