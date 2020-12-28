package engima.waratsea.viewmodel.squadrons;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.AttackType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronFactor;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.utility.Probability;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    @Getter private final ObjectProperty<SquadronState> state = new SimpleObjectProperty<>();

    private SquadronAction squadronAction;

    @Getter private final BooleanProperty present = new SimpleBooleanProperty();

    @Getter private final StringProperty title = new SimpleStringProperty();
    @Getter private final StringProperty titleId = new SimpleStringProperty();
    @Getter private final StringProperty name = new SimpleStringProperty();
    @Getter private final StringProperty strength = new SimpleStringProperty();
    private final StringProperty model = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    @Getter private final StringProperty abbreviatedType = new SimpleStringProperty();
    private final StringProperty nation = new SimpleStringProperty();
    private final StringProperty service = new SimpleStringProperty();
    @Getter private final StringProperty equipped = new SimpleStringProperty();

    private final StringProperty landFactor = new SimpleStringProperty();
    private final StringProperty landModifier = new SimpleStringProperty();
    @Getter private final StringProperty landProb = new SimpleStringProperty();

    private final StringProperty navalFactor = new SimpleStringProperty();
    private final StringProperty navalModifier = new SimpleStringProperty();
    @Getter private final StringProperty navalProb = new SimpleStringProperty();

    private final StringProperty airFactor = new SimpleStringProperty();
    private final StringProperty airModifier = new SimpleStringProperty();
    @Getter private final StringProperty airProb = new SimpleStringProperty();

    @Getter private final StringProperty airSummary = new SimpleStringProperty();
    @Getter private final StringProperty landSummary = new SimpleStringProperty();
    @Getter private final StringProperty navalSummary = new SimpleStringProperty();

    private final StringProperty range = new SimpleStringProperty();
    @Getter private final StringProperty radius = new SimpleStringProperty();
    @Getter private final StringProperty endurance = new SimpleStringProperty();
    private final StringProperty ferry = new SimpleStringProperty();
    @Getter private final StringProperty altitude = new SimpleStringProperty();
    @Getter private final StringProperty landing = new SimpleStringProperty();

    private final StringProperty frame = new SimpleStringProperty();
    private final StringProperty fragile = new SimpleStringProperty();

    @Getter private final ObjectProperty<Image> aircraftImage = new SimpleObjectProperty<>();
    @Getter private final ObjectProperty<Image> aircraftProfile = new SimpleObjectProperty<>();
    @Getter private final ObjectProperty<Image> aircraftProfileSummary = new SimpleObjectProperty<>();

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
     * Set the backing model for this view model.
     *
     * @param newSquadron The backing squadron model.
     * @return This squadron view model.
     */
    public SquadronViewModel setModel(final Squadron newSquadron) {
        squadron.setValue(newSquadron);
        state.setValue(newSquadron.getState());
        configuration.setValue(newSquadron.getConfig());
        return this;
    }

    /**
     * Place the squadron on a patrol. Note, the configuration of the squadron is set
     * in the dialog handler when it is selected to be placed on patrol. Thus, the
     * configuration is not set here.
     */
    public void setOnPatrol() {
        state.setValue(state.getValue().transition(SquadronAction.ASSIGN_TO_PATROL));
        squadronAction = SquadronAction.ASSIGN_TO_PATROL;
    }

    /**
     * Place the squadron off a patrol.
     */
    public void setOffPatrol() {
        state.setValue(state.getValue().transition(SquadronAction.REMOVE_FROM_PATROL));
        squadronAction = SquadronAction.REMOVE_FROM_PATROL;
    }

    /**
     * Place the squadron on a mission. Note, the configuration is set when the
     * by the dialog handler when the squadron is selected to be added to the
     * mission. Thus, the configuration is not set here.
     */
    public void setOnMission() {
        state.setValue(state.getValue().transition(SquadronAction.ASSIGN_TO_MISSION));
        squadronAction = SquadronAction.ASSIGN_TO_MISSION;
    }

    /**
     * Place the squadron off a mission.
     */
    public void setOffMission() {
        state.setValue(state.getValue().transition(SquadronAction.REMOVE_FROM_MISSION));
        squadronAction = SquadronAction.REMOVE_FROM_MISSION;
    }

    /**
     * Set the squadron configuration based on its mission data.
     *
     * @param selectedTarget The mission's target.
     * @param selectedMissionType The type of mission.
     * @param role The squadron's role on the mission.
     */
    public void setConfig(final Target selectedTarget, final AirMissionType selectedMissionType, final MissionRole role) {
        SquadronConfig config = squadron.getValue().determineConfig(selectedTarget, selectedMissionType, role);
        configuration.setValue(config);
    }

    /**
     * Set the squadron's configuration.
     *
     * @param newConfig The squadron's new configuration.
     */
    public void setConfig(final SquadronConfig newConfig) {
        configuration.setValue(newConfig);
    }

    /**
     * Save the squadron to the model. Currently the only two things that vary for squadrons are:
     *  - configuration
     *  - state
     */
    public void save() {
        Squadron squadronModel = squadron.getValue();

        // Make sure that a ready squadron has a configuration of none. This is needed if a ready
        // squadron is viewed on the patrol or mission views. On those views the configuration is
        // set to reflect what the configuration will be, if the squadron is placed on the mission
        // or patrol. Thus, we reset it to none here to ensure the configuration of a ready squadron
        // has the correct value.
        if (state.getValue() == SquadronState.READY) {
            configuration.setValue(SquadronConfig.NONE);
        }

        squadronModel.setConfig(configuration.getValue());
        squadronModel.setState(squadronAction);
    }

    /**
     * Set the backing data for this view model.
     *
     * @param newSquadron The squadron.
     */
    public void set(final Squadron newSquadron) {
        squadron.setValue(newSquadron);
        state.setValue(SquadronState.READY);
        configuration.setValue(SquadronConfig.NONE);
    }

    /**
     * Set the backing data for this view model.
     *
     * @param newSquadron The squadron.
     * @param newState The squadron state.
     */
    public void set(final Squadron newSquadron, final SquadronState newState) {
        squadron.setValue(newSquadron);
        state.setValue(newState);
        configuration.setValue(SquadronConfig.NONE);
    }

    /**
     * Set the backing data for this view model.
     *
     * @param newSquadron The squadron.
     * @param newConfig The squadron's config.
     */
    public void set(final Squadron newSquadron, final SquadronConfig newConfig) {
        squadron.setValue(newSquadron);
        state.setValue(SquadronState.READY);
        configuration.setValue(newConfig);
    }

    /**
     * Get the backing squadron model.
     *
     * @return The squadron model.
     */
    public Squadron get() {
        return squadron.getValue();
    }

    /**
     *  Get the squadron name as a String.
     *
     * @return The squadron's name.
     */
    public String getNameAsString() {
        return get().getName();
    }

    /**
     * Get the squadron's title as a String.
     *
     * @return The squadron's title.
     */
    public String getTitleAsString() {
        return get().getTitle();
    }

    /**
     * Get the squadron's nation.
     *
     * @return The squadron's nation.
     */
    public Nation getNation() {
        return get().getNation();
    }

    /**
     * Get the squadron's aircraft type.
     *
     * @return The squadron's aircraft type.
     */
    public AircraftType getType() {
        return get().getType();
    }

    /**
     * The number of steps in the squadron.
     *
     * @return The number of steps in the squadron.
     */
    public int getSteps() {
        return get().getSteps().intValue();
    }

    /**
     * Determines if the squadron can perform the given mission type.
     *
     * @param missionType The mission type.
     * @return True if the squadron can perform the given mission type. False otherwise.
     */
    public boolean canDoMission(final AirMissionType missionType) {
        return get().canDoMission(missionType);
    }

    /**
     * Determines if the squadron can perform the given role.
     *
     * @param role The mission role.
     * @return True if the squadron can perform the given mission role. False otherwise.
     */
    public boolean canDoRole(final MissionRole role) {
        return get().canDoRole(role);
    }

    /**
     * Determines if the squadron can perform the given patrol type.
     *
     * @param patrolType The type of patrol.
     * @return True if the squadron can perform the given patrol type. False otherwise.
     */
    public boolean canDoPatrol(final PatrolType patrolType) {
        return get().canDoPatrol(patrolType);
    }

    /**
     * Determines if the squadron is in range of the given target for the given mission type and mission role.
     *
     * @param target The mission target.
     * @param missionType The mission type.
     * @param role The mission role of the squadron.
     * @return True if the squadron is in rage of the given target for the given mission type and mission role.
     */
    public boolean inRange(final Target target, final AirMissionType missionType, final MissionRole role) {
        return get().inRange(target, missionType, role);
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


    /**
     * The String representation of a squadron view model.
     *
     * @return The String representation of a squadron view model.
     */
    @Override
    public String toString() {
        return Optional
                .ofNullable(squadron.getValue())
                .map(Squadron::toString)
                .orElse("");
    }

    private void bindDetails() {
        present.bind(Bindings.createBooleanBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .isPresent(), squadron));

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

        abbreviatedType.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getType().getAbbreviated())
                .orElse(""), squadron));

        nation.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getNation().toString())
                .orElse(""), squadron));

        service.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getService().toString())
                .orElse(""), squadron));

        equipped.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(configuration.getValue())
                .map(SquadronConfig::toString)
                .orElse(""), configuration));
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

        landSummary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.LAND))
                .map(this::getAttackSummary)
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

        navalSummary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.NAVAL))
                .map(this::getAttackSummary)
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

        airSummary.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getFactor(AttackType.AIR))
                .map(this::getAttackSummary)
                .orElse(""), squadron, configuration));
    }

    private void bindPerformance() {
        range.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> s.getAircraft().getRange() + "")
                .orElse(""), squadron));

        radius.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getRadiusAsString())
                .orElse(""), squadron, configuration));

        endurance.bind(Bindings.createStringBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> getEnduranceAsString())
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

        aircraftProfileSummary.bind(Bindings.createObjectBinding(() -> Optional
                .ofNullable(squadron.getValue())
                .map(s -> imageResourceProvider.getAircraftProfileImageView(s.getSide(), s.getAircraft().getModel() + "-240"))
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

    private String getEnduranceAsString() {
        SquadronConfig config = Optional.ofNullable(configuration.getValue()).orElse(SquadronConfig.NONE);
        return squadron.getValue().getAircraft().getEndurance().get(config) + "";
    }

    private String getRadiusAsString() {
        SquadronConfig config = Optional.ofNullable(configuration.getValue()).orElse(SquadronConfig.NONE);
        return squadron.getValue().getAircraft().getRadius().get(config) + "";
    }

    private String getFerry() {
        SquadronConfig config = Optional.ofNullable(configuration.getValue()).orElse(SquadronConfig.NONE);
        return squadron.getValue().getAircraft().getFerryDistance().get(config) + "";
    }

    private String getAttackSummary(final SquadronFactor factor) {
        return factor.getModifier() != 0
                ? factor.getFactor() + " (" + factor.getModifier() + ")"
                : factor.isDefensive() ? factor.getFactor() + " (D)"
                : factor.getFactor() + "";
    }
}
