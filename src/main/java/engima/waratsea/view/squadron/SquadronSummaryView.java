package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SquadronSummaryView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private final ObjectProperty<SquadronViewModel> squadron = new SimpleObjectProperty<>();

    private final Label title = new Label();

    private final StringProperty abbreviatedType = new SimpleStringProperty();
    private final StringProperty strength = new SimpleStringProperty();
    private final StringProperty airSummary = new SimpleStringProperty();
    private final StringProperty airProb = new SimpleStringProperty();
    private final StringProperty landSummary = new SimpleStringProperty();
    private final StringProperty landProb = new SimpleStringProperty();
    private final StringProperty navalSummary = new SimpleStringProperty();
    private final StringProperty navalProb = new SimpleStringProperty();

    private final StringProperty landing = new SimpleStringProperty();
    private final StringProperty altitude = new SimpleStringProperty();
    private final StringProperty equipped = new SimpleStringProperty();
    private final StringProperty radius = new SimpleStringProperty();
    private final StringProperty endurance = new SimpleStringProperty();

    private final VBox mainPane =  new VBox();
    private final ImageView aircraftProfile = new ImageView();

    private final BoundTitledGridPane attackPane = new BoundTitledGridPane();
    private final BoundTitledGridPane performancePane = new BoundTitledGridPane();

    @Inject
    public SquadronSummaryView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;

        bind();
    }

    /**
     * Show the selectedSquadron summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     *
     * @return A node containing the selectedSquadron summary.
     */
    public Node build(final Nation nation) {
        Node titleNode = buildTitle(nation);
        Node profilePane = buildProfile();

        buildAttack();
        buildPerformance();

        HBox hBox = new HBox(profilePane, attackPane, performancePane);
        hBox.setId("summary-hbox");

        VBox vBox = new VBox(titleNode, hBox);
        vBox.setId("summary-vbox");

        mainPane.getChildren().add(vBox);
        mainPane.setId("summary-pane");

        return mainPane;
    }

    /**
     * Set this view's to a squadron view model.
     *
     * @param viewModel The squadron view model.
     */
    public void setSquadron(final SquadronViewModel viewModel) {
        squadron.setValue(viewModel);
    }

    private void bind() {
        title.textProperty().bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getTitle().getValue())
                .orElse(""), squadron));

        aircraftProfile.imageProperty().bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getAircraftProfileSummary().getValue())
                .orElse(null), squadron));

        mainPane.visibleProperty().bind(Bindings.createBooleanBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getPresent().getValue())
                .orElse(false), squadron));

        abbreviatedType.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getAbbreviatedType().getValue())
                .orElse(""), squadron));

        strength.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getStrength().getValue())
                .orElse(""), squadron));

        airSummary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getAirSummary().getValue())
                .orElse(""), squadron));

        airProb.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getAirProb().getValue())
                .orElse(""), squadron));

        landSummary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getLandSummary().getValue())
                .orElse(""), squadron));

        landProb.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getLandProb().getValue())
                .orElse(""), squadron));

        navalSummary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getNavalSummary().getValue())
                .orElse(""), squadron));

        navalProb.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getNavalProb().getValue())
                .orElse(""), squadron));

        landing.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getLanding().getValue())
                .orElse(""), squadron));

        altitude.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getAltitude().getValue())
                .orElse(""), squadron));

        equipped.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getEquipped().getValue())
                .orElse(""), squadron));

        radius.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getRadius().getValue())
                .orElse(""), squadron));

        endurance.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(svm -> svm.getEndurance().getValue())
                .orElse(""), squadron));
    }

    private Node buildTitle(final Nation nation) {
        title.setId("summary-title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("summary-title-pane-" + nation.getFileName().toLowerCase());

        return titlePane;
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
        vBox.setId("profile-vbox");

        titledPane.setContent(vBox);
        titledPane.setMinHeight(props.getInt("airfield.dialog.profile.height"));
        titledPane.setMaxHeight(props.getInt("airfield.dialog.profile.height"));
        titledPane.setMinWidth(props.getInt("airfield.dialog.profile.width"));
        titledPane.setMaxWidth(props.getInt("airfield.dialog.profile.width"));
        return titledPane;
    }

    private void buildAttack() {
        ImageView imageView = imageResourceProvider.getImageView(props.getString("info.small.image"));
        HBox hbox = new HBox(imageView);
        hbox.setId("squadron-attack-data-title");

        Tooltip tooltip = new Tooltip("Squadron Attack Data:\n\nFirst number is the attack factor.\nSecond number is the attack (modifier).\nD indicates Defensive Only.\nThird number is the % chance of a single hit.");

        tooltip.setShowDelay(Duration.seconds(props.getDouble("tooltip.delay")));
        tooltip.setShowDuration(Duration.seconds(props.getDouble("tooltip.duration")));

        Tooltip.install(imageView, tooltip);

        buildPane(attackPane);

        attackPane.setTitle("Squadron Attack Data");
        attackPane.setContentDisplay(ContentDisplay.RIGHT);
        attackPane.setGraphic(hbox);
        attackPane.bindListStrings(getAttackSummary());
    }


    private void buildPerformance() {
        ImageView imageView = imageResourceProvider.getImageView(props.getString("info.small.image"));
        HBox hbox = new HBox(imageView);
        hbox.setId("squadron-performance-data-title");

        Tooltip tooltip = new Tooltip("Squadron Performance Data:\n\nRadius is in grid squares.\n"
                + "Endurance is in game turns.\nThe mission type may affect endurance.\n"
                + "If drop tanks are needed,\nthey are automatically equipped.");

        tooltip.setShowDelay(Duration.seconds(props.getDouble("tooltip.delay")));
        tooltip.setShowDuration(Duration.seconds(props.getDouble("tooltip.duration")));

        Tooltip.install(imageView, tooltip);

        buildPane(performancePane);
        performancePane.setTitle("Squadron Performance Data");
        performancePane.setContentDisplay(ContentDisplay.RIGHT);
        performancePane.setGraphic(hbox);
        performancePane.bindStrings(getPerformanceSummary());
    }

    /**
     * Build a component pane.
     *
     * @param pane The pane to build.
     */
    private void buildPane(final BoundTitledGridPane pane) {
         pane.setWidth(props.getInt("airfield.dialog.profile.width"))
                .setGridStyleId("component-grid")
                .build();
    }

    /**
     * Get the squadron's attack summary.
     *
     * @return The squadron's attack summary.
     */
    private Map<String, List<StringProperty>> getAttackSummary() {
        Map<String, List<StringProperty>> summary = new LinkedHashMap<>();
        summary.put("Type:", List.of(abbreviatedType));
        summary.put("Strength:", List.of(strength));
        summary.put(" ", Collections.emptyList());
        summary.put("Air-to-Air Attack:", List.of(airSummary, airProb));
        summary.put("Land Attack:", List.of(landSummary, landProb));
        summary.put("Naval Attack:", List.of(navalSummary, navalProb));
        return summary;
    }

    /**
     * Get the squadron's performance summary.
     *
     * @return The squadron's performance summary.
     */
    private Map<String, StringProperty> getPerformanceSummary() {
        Map<String, StringProperty> summary = new LinkedHashMap<>();
        summary.put("Landing Type:", landing);
        summary.put("Altitude Rating:", altitude);
        summary.put(" ", new SimpleStringProperty(""));
        summary.put("Equipped:", equipped);
        summary.put("Radius:", radius);
        summary.put("Endurance:", endurance);

        return summary;
    }
}
