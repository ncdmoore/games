package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The detail view of a squadron.
 */
public class SquadronDetailsView {
    private final ViewProps props;

    private final Label title = new Label();
    private final Node titlePane = new StackPane(title);
    private final ImageView aircraftImage = new ImageView();
    private final ImageView aircraftProfile = new ImageView();
    private final BoundTitledGridPane squadronDetailsPane = new BoundTitledGridPane();
    private final BoundTitledGridPane aircraftDetailsPane = new BoundTitledGridPane();
    private final BoundTitledGridPane aircraftLandPane = new BoundTitledGridPane();
    private final BoundTitledGridPane aircraftNavalWarshipPane = new BoundTitledGridPane();
    private final BoundTitledGridPane aircraftNavalTransportPane = new BoundTitledGridPane();
    private final BoundTitledGridPane aircraftAirPane = new BoundTitledGridPane();
    private final BoundTitledGridPane aircraftPerformancePane = new BoundTitledGridPane();
    private final BoundTitledGridPane aircraftFramePane = new BoundTitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     */
    @Inject
    public SquadronDetailsView(final ViewProps props) {
        this.props = props;
    }

    /**
     * Show the squadron details.
     *
     * @return A node containing the squadron details.
     */
    public Node build() {
        Node titleBox = buildTitle();
        Node imageBox = buildImages();
        Node weaponsBox = buildWeapons();
        Node performanceBox = buildPerformance();

        HBox hBox = new HBox(imageBox, weaponsBox, performanceBox);
        hBox.setId("main-hbox");

        VBox mainPane = new VBox(titleBox, hBox);

        mainPane.setId("squadron-main-pane");
        return mainPane;
    }

    /**
     * Bind to the view model.
     *
     * @param viewModel The squadron view model.
     */
    public void bind(final SquadronViewModel viewModel) {
        title.textProperty().bind(viewModel.getTitle());
        titlePane.idProperty().bind(viewModel.getTitleId());

        aircraftImage.imageProperty().bind(viewModel.getAircraftImage());
        aircraftProfile.imageProperty().bind(viewModel.getAircraftProfile());

        squadronDetailsPane.bindStrings(viewModel.getSquadronDetails());
        aircraftDetailsPane.bindStrings(viewModel.getAircraftDetails());
        aircraftLandPane.bindListStrings(viewModel.getLandAttack());
        aircraftNavalWarshipPane.bindListStrings(viewModel.getNavalWarshipAttack());
        aircraftNavalTransportPane.bindListStrings(viewModel.getNavalTransportAttack());
        aircraftAirPane.bindListStrings(viewModel.getAirAttack());
        aircraftPerformancePane.bindListStrings(viewModel.getPerformance());
        aircraftFramePane.bindStrings(viewModel.getFrame());
    }

    /**
     * Build the title.
     *
     * @return The title pane.
     */
    private Node buildTitle() {
        title.setId("title");
        return titlePane;
    }

    /**
     * Build the image boxes.
     *
     * @return A node containing the image nodes.
     */
    private Node buildImages() {
        Node imageBox = buildImage();
        Node profileBox = buildProfile();

        VBox vBox = new VBox(imageBox, profileBox);
        vBox.setId("image-vbox");

        return vBox;
    }

    /**
     * Build the squadron aircraft image.
     *
     * @return The node that contains the squadron image.
     */
    private Node buildImage() {
        VBox vBox = new VBox(aircraftImage);
        vBox.setId("squadron-image");
        return vBox;
    }

    /**
     * Build the aircraft profile image.
     *
     * @return The node containing the aircraft profile image.
     */
    private Node buildProfile() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Profile");

        VBox vBox = new VBox(aircraftProfile);
        int height = props.getInt("squadron.detail.profile.height");
        vBox.setMinHeight(height);
        vBox.setMaxHeight(height);
        vBox.getStyleClass().add("profile-vbox");

        titledPane.getStyleClass().add("component-grid");
        titledPane.setContent(vBox);
        return titledPane;
    }

    /**
     * Build the aircraft's weapons boxes.
     *
     * @return The node containing the aircraft's weapons boxes.
     */
    private Node buildWeapons() {
        buildPane(squadronDetailsPane).setTitle("Squadron Details");
        buildPane(aircraftDetailsPane).setTitle("Aircraft Details");
        buildPane(aircraftLandPane).setTitle("Land Attack");
        buildPane(aircraftNavalWarshipPane).setTitle("Naval Warship Attack");

        VBox vBox = new VBox(squadronDetailsPane, aircraftDetailsPane, aircraftLandPane, aircraftNavalWarshipPane);
        vBox.getStyleClass().add("components-pane");

        return vBox;
    }

    /**
     * Build the aircraft's performance boxes.
     *
     * @return The node containing the aircraft's performance boxes.
     */
    private Node buildPerformance() {
        buildPane(aircraftFramePane).setTitle("Frame");
        buildPane(aircraftPerformancePane).setTitle("Range");
        buildPane(aircraftAirPane).setTitle("Air Attack");
        buildPane(aircraftNavalTransportPane).setTitle("Naval Transport Attack");


        VBox vBox = new VBox(aircraftFramePane, aircraftPerformancePane, aircraftAirPane, aircraftNavalTransportPane);
        vBox.getStyleClass().add("components-pane");

        return vBox;
    }

    /**
     * Build a component pane.
     *
     * @param pane The pane to build.
     * @return The built pane.
     */
    private BoundTitledGridPane buildPane(final BoundTitledGridPane pane) {
        return pane.setWidth(props.getInt("ship.dialog.detailsPane.width"))
                .setGridStyleId("component-grid")
                .build();
    }
}
