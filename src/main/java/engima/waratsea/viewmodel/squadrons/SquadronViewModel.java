package engima.waratsea.viewmodel.squadrons;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AttackType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronFactor;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.utility.Probability;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SquadronViewModel {
    private final Probability probability;

    @Getter private final ObjectProperty<Squadron> squadron = new SimpleObjectProperty<>();

    @Getter private final ObjectProperty<SquadronConfig> configuration = new SimpleObjectProperty<>();

    @Getter private final StringProperty title = new SimpleStringProperty();
    @Getter private final StringProperty titleId = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty strength = new SimpleStringProperty();
    private final StringProperty model = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty nation = new SimpleStringProperty();
    private final StringProperty service = new SimpleStringProperty();

    private final StringProperty landFactor = new SimpleStringProperty();
    private final StringProperty landModifier = new SimpleStringProperty();
    private final StringProperty landProb = new SimpleStringProperty();

    private final StringProperty navalFactor = new SimpleStringProperty();
    private final StringProperty navalModifier = new SimpleStringProperty();
    private final StringProperty navalProb = new SimpleStringProperty();

    private final StringProperty airFactor = new SimpleStringProperty();
    private final StringProperty airModifier = new SimpleStringProperty();
    private final StringProperty airProb = new SimpleStringProperty();

    private final StringProperty range = new SimpleStringProperty();
    private final StringProperty radius = new SimpleStringProperty();
    private final StringProperty endurance = new SimpleStringProperty();
    private final StringProperty ferry = new SimpleStringProperty();
    private final StringProperty altitude = new SimpleStringProperty();
    private final StringProperty landing = new SimpleStringProperty();

    private final StringProperty frame = new SimpleStringProperty();
    private final StringProperty fragile = new SimpleStringProperty();

    @Getter private final ObjectProperty<Image> aircraftImage = new SimpleObjectProperty<>();
    @Getter private final ObjectProperty<Image> aircraftProfile = new SimpleObjectProperty<>();

    private final StringProperty airfield = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    @Inject
    public SquadronViewModel(final ImageResourceProvider imageResourceProvider,
                             final Probability probability) {
        this.probability = probability;

        bindDetails();
        bindLandAttack();
        bindNavalAttack();
        bindAirAttack();
        bindPerformance();
        bindFrame();
        bindImages(imageResourceProvider);
        bindAirfieldProperties();
    }

    /**
     * Set the backing data for this view model.
     *
     * @param newSquadron The squadron.
     */
    public void set(final Squadron newSquadron) {
        squadron.setValue(newSquadron);
        configuration.setValue(SquadronConfig.NONE);
    }

    /**
     * Get the squadron details.
     *
     * @return The squadron's details.
     */
    public Map<String, StringProperty> getSquadronDetails() {
        Map<String, StringProperty> details = new LinkedHashMap<>();
        details.put("Name:", name);
        details.put("Strength:", strength);
        return details;
    }

    /**
     * Get the aircraft details.
     *
     * @return The aircraft's details.
     */
    public Map<String, StringProperty> getAircraftDetails() {
        Map<String, StringProperty> details = new LinkedHashMap<>();
        details.put("Model:", model);
        details.put("Type:", type);
        details.put("Nationality:", nation);
        details.put("Service:", service);
        return details;
    }

    /**
     * Get the squadron's airfield and status data.
     *
     * @return The squadron's airfield and status data.
     */
    public Map<String, StringProperty> getAirfield() {
        Map<String, StringProperty> map = new LinkedHashMap<>();
        map.put("Airfield:", airfield);
        map.put("Squadron Status:", status);
        return map;
    }

    /**
     * Get the squadron's land attack data.
     *
     * @return The squadron's land attack data.
     */
    public Map<String, List<StringProperty>> getLandAttack() {
        Map<String, List<StringProperty>> map = new LinkedHashMap<>();
        map.put("Factor:", List.of(landFactor, landProb));
        map.put("Modifier:", List.of(landModifier));
        return map;
    }

    /**
     * Get the squadron's naval attack data.
     *
     * @return The squadron's naval attack data.
     */
    public Map<String, List<StringProperty>> getNavalAttack() {
        Map<String, List<StringProperty>> map = new LinkedHashMap<>();
        map.put("Factor:", List.of(navalFactor, navalProb));
        map.put("Modifier:", List.of(navalModifier));
        return map;
    }

    /**
     * Get the squadron's air attack data.
     *
     * @return The squadron's air attack data.
     */
    public Map<String, List<StringProperty>> getAirAttack() {
        Map<String, List<StringProperty>> map = new LinkedHashMap<>();
        map.put("Factor:", List.of(airFactor, airProb));
        map.put("Modifier:", List.of(airModifier));
        return map;
    }

    /**
     * Get the squadron's ferryDistance data.
     *

     * @return The squadron's ferryDistance data.
     */
    public Map<String, List<StringProperty>> getPerformance() {
        Map<String, List<StringProperty>> details = new LinkedHashMap<>();
        details.put("Range:", List.of(range, new SimpleStringProperty("Radius:"), radius));
        details.put("Endurance:", List.of(endurance, new SimpleStringProperty("Ferry:"), ferry));
        details.put("Altitude Rating:", List.of(altitude));
        details.put("Landing Type:", List.of(landing));
        return details;
    }

    /**
     * Get the squadron's frame data.
     *
     * @return The squadron's frame data.
     */
    public Map<String, StringProperty> getFrame() {
        Map<String, StringProperty> details = new LinkedHashMap<>();
        details.put("Frame:", frame);
        details.put("Fragile:", fragile);
        return details;
    }

    private void bindDetails() {
        title.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(Squadron::getTitle)
                .orElse(""), squadron));

        titleId.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> "title-pane-" + s.getNation().getFileName().toLowerCase())
                .orElse(""), squadron));

        name.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(Squadron::getName)
                .orElse(""), squadron));

        strength.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getStrength() + "")
                .orElse(""), squadron));

        model.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getModel())
                .orElse(""), squadron));

        type.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getType().toString())
                .orElse(""), squadron));

        nation.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getNation().toString())
                .orElse(""), squadron));

        service.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getService().toString())
                .orElse(""), squadron));
    }

    private void bindLandAttack() {
        landFactor.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.LAND))
                .map(f -> f.getFactor() + (f.isDefensive() ? " (D)" : ""))
                .orElse(""), squadron, configuration));

        landModifier.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.LAND))
                .map(f -> f.getModifier() + "")
                .orElse(""), squadron, configuration));

        landProb.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getProbability(AttackType.LAND))
                .orElse(""), squadron, configuration));
    }

    private void bindNavalAttack() {
        navalFactor.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.NAVAL))
                .map(f -> f.getFactor() + (f.isDefensive() ? " (D)" : ""))
                .orElse(""), squadron, configuration));

        navalModifier.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.NAVAL))
                .map(f -> f.getModifier() + "")
                .orElse(""), squadron, configuration));

        navalProb.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getProbability(AttackType.NAVAL))
                .orElse(""), squadron, configuration));
    }

    private void bindAirAttack() {
        airFactor.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.AIR))
                .map(f -> f.getFactor() + (f.isDefensive() ? " (D)" : ""))
                .orElse(""), squadron, configuration));

        airModifier.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.AIR))
                .map(f -> f.getModifier() + "")
                .orElse(""), squadron, configuration));

        airProb.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getProbability(AttackType.AIR))
                .orElse(""), squadron, configuration));
    }

    private void bindPerformance() {
        range.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getRange() + "")
                .orElse(""), squadron));

        radius.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getRadius())
                .orElse(""), squadron, configuration));

        endurance.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getEndurance())
                .orElse(""), squadron, configuration));

        ferry.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFerry())
                .orElse(""), squadron, configuration));

        altitude.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getAltitude().toString())
                .orElse(""), squadron));

        landing.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getLanding().toString())
                .orElse(""), squadron, configuration));
    }

    private void bindFrame() {
        frame.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getFrame().getFrame() + "")
                .orElse(""), squadron));

        fragile.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getFrame().isFragile() + "")
                .orElse(""), squadron));
    }

    private void bindImages(final ImageResourceProvider imageResourceProvider) {
        aircraftImage.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> imageResourceProvider.getAircraftImageView(s.getAircraft()))
                .orElse(null), squadron));

        aircraftProfile.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> imageResourceProvider.getAircraftProfileImageView(s.getSide(), s.getAircraft().getModel()))
                .orElse(null), squadron));
    }

    private void bindAirfieldProperties() {
        airfield.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(selectedSquadron -> selectedSquadron.getHome().getTitle())
                .orElse(""), squadron));

        status.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(selectedSquadron -> selectedSquadron.getState().toString())
                .orElse(""), squadron));
    }

    private SquadronFactor getFactor(final AttackType attackType) {
        SquadronConfig config = Optional.ofNullable(configuration.getValue()).orElse(SquadronConfig.NONE);
        return squadron.getValue().getFactor(attackType, config);
    }

    private String getProbability(final AttackType attackType) {
        SquadronConfig config = Optional.ofNullable(configuration.getValue()).orElse(SquadronConfig.NONE);
        return probability.percentage(squadron.getValue().getHitProbability(attackType, config)) + "%";
    }

    private String getEndurance() {
        SquadronConfig config = Optional.ofNullable(configuration.getValue()).orElse(SquadronConfig.NONE);
        return squadron.getValue().getAircraft().getEndurance().get(config) + "";
    }

    private String getRadius() {
        SquadronConfig config = Optional.ofNullable(configuration.getValue()).orElse(SquadronConfig.NONE);
        return squadron.getValue().getAircraft().getRadius().get(config) + "";
    }

    private String getFerry() {
        SquadronConfig config = Optional.ofNullable(configuration.getValue()).orElse(SquadronConfig.NONE);
        return squadron.getValue().getAircraft().getFerryDistance().get(config) + "";
    }
}
