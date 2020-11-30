package engima.waratsea.viewmodel.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionDAO;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A nation's view of an air mission.
 */
@Slf4j
public class AirMissionViewModel {
    @Getter private final SimpleListProperty<AirMissionType> missionTypes = new SimpleListProperty<>();                 // List of all mission types.

    @Getter private final Map<MissionRole, SimpleListProperty<Squadron>> available = new HashMap<>();                   // List of available squadrons for a particular role.
    @Getter private final Map<MissionRole, SimpleListProperty<Squadron>> assigned = new HashMap<>();                    // List of squadrons assigned to this mission for a particular role.

    @Getter private final Map<MissionRole, BooleanProperty> availableExists = new HashMap<>();                          // Indicates if any available squadrons exist for a particular role.
    @Getter private final Map<MissionRole, BooleanProperty> assignedExists = new HashMap<>();                           // Indicates if any assigned squadrons exist for a particular role.

    @Getter private final SimpleListProperty<Squadron> totalAssigned = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
    @Getter private final IntegerProperty totalAssignedCount = new SimpleIntegerProperty(0);                  // Total number of squadrons on the mission. Includes all roles.

    @Getter private final IntegerProperty totalStepsInRouteToTarget = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStepsInRouteToTargetThisMission = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStepsInRouteToTargetOtherMissions = new SimpleIntegerProperty(0);

    @Getter private final IntegerProperty totalStepsInRouteToTargetRegion = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStepsInRouteToTargetRegionThisMission = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStepsInRouteToTargetRegionOtherMissions = new SimpleIntegerProperty(0);

    @Getter private final IntegerProperty totalStepsLeavingRegion = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStepsFromThisMissionLeavingRegion = new SimpleIntegerProperty(0);
    private final IntegerProperty totalStepsFromOtherMissionsLeavingRegion = new SimpleIntegerProperty(0);

    @Getter private final SimpleListProperty<ProbabilityStats> missionStats = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private final Map<MissionRole, BooleanProperty> error = new HashMap<>();
    @Getter private final Map<MissionRole, String> errorText = new HashMap<>();

    @Getter private final BooleanProperty warning = new SimpleBooleanProperty();
    @Getter private String warningText;

    @Getter private NationAirbaseViewModel nationAirbaseViewModel;
    @Getter private Nation nation;    // The nation that performs this mission.
    @Getter private Airbase airbase;  // The airbase from which the mission originates.

    @Getter private final ObjectProperty<AirMissionType> missionType = new SimpleObjectProperty<>();
    @Getter private final ObjectProperty<Target> target = new SimpleObjectProperty<>();

    private final Game game;
    private final MissionDAO missionDAO;
    @Getter private AirMission mission;

    @Getter private final IntegerProperty missionId = new SimpleIntegerProperty(0);
    @Getter private int id;

    @Getter private final SimpleListProperty<Squadron> ready = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));

    private boolean checkCapacity = true;  //For new missions the capacity of the target is checked. For existing mission it is not.

    /**
     * The constructor called by guice.
     *
     * @param game The game.
     * @param missionDAO The mission data access object.
     */
    @Inject
    public AirMissionViewModel(final Game game,
                               final MissionDAO missionDAO) {
        this.game = game;
        this.missionDAO = missionDAO;

        missionTypes.setValue(FXCollections.observableArrayList(AirMissionType.values()));

        Stream.of(MissionRole.values()).forEach(role -> {
            available.put(role, new SimpleListProperty<>());
            availableExists.put(role, new SimpleBooleanProperty());

            assigned.put(role, new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>())));
            assignedExists.put(role, new SimpleBooleanProperty(false));

            error.put(role, new SimpleBooleanProperty());
            errorText.put(role, "");
        });
    }

    /**
     * Set the nation for this mission.
     *
     * @param selectedNation The nation: BRITISH, ITALIAN, etc...
     * @return This air mission view model.
     */
    public AirMissionViewModel setNation(final Nation selectedNation) {
        nation = selectedNation;
        return this;
    }

    /**
     * Set the mission type.
     *
     * @param selectedMissionType The selected mission type.
     * @return This air mission view model.
     */
    public AirMissionViewModel setMissionType(final AirMissionType selectedMissionType) {
        missionType.setValue(selectedMissionType);
        return this;
    }

    /**
     * Set the target.
     *
     * @param selectedTarget The selected target.
     * @return This air mission view model.
     */
    public AirMissionViewModel setTarget(final Target selectedTarget) {
        target.setValue(selectedTarget);
        updateMissionStats();
        return this;
    }

    /**
     * Set the model.
     *
     * @param missionModel The mission model.
     * @return This air mission view model.
     */
    public AirMissionViewModel setModel(final AirMission missionModel) {
        missionType.setValue(missionModel.getType());
        target.setValue(missionModel.getTarget());
        nation = missionModel.getNation();

        mission = buildMission(missionModel);
        id = missionModel.getId();
        missionId.setValue(id);

        log.debug("Initialize view model with mission id: '{}'", id);
        log.debug("Mission object: '{}'", mission);

        checkCapacity = false; // This is an existing mission, so no need to check target capacity.

        Stream.of(MissionRole.values())
                .forEach(role -> {
                    String assignedNames = mission.getSquadrons(role)
                            .stream()
                            .map(Squadron::getTitle)
                            .collect(Collectors.joining(","));

                    log.debug("Assigned squadrons: '{}' for role: '{}'", assignedNames, role);
                    assigned.get(role).set(FXCollections.observableArrayList(mission.getSquadrons(role)));
                });


        totalAssigned.setValue(FXCollections.observableArrayList(mission.getSquadronsAllRoles()));
        totalAssignedCount.setValue(mission.getSquadronsAllRoles().size());

        return this;
    }

    /**
     * Set the nation view so that this mission view model can determine which squadron at the
     * airbase are available (Ready) for missions.
     *
     * @param viewModel The nation's airbase view model.
     * @return This air mission view model.
     */
    public AirMissionViewModel setNationViewModel(final NationAirbaseViewModel viewModel) {
        nationAirbaseViewModel = viewModel;
        airbase = viewModel.getAirbase();

        bindAvailable(viewModel);
        bindTotalStepsInRouteToTargetThisMission();
        bindTotalStepsInRouteToTargetOtherMissions();
        bindTotalStepsInRouteToTarget();
        bindTotalStepsInRouteToTargetRegionThisMission();
        bindTotalStepsInRouteToTargetRegionOtherMissions();
        bindTotalStepsInRouteToTargetRegion();
        bindTotalStepsFromThisMissionLeavingRegion();
        bindTotalStepsFromOtherMissionsLeavingRegion();
        bindTotalStepsLeavingRegion();

        return this;
    }

    /**
     * Get a list of available targets for the given type of mission.
     *
     * @return A list of available targets for the given type of mission.
     */
    public List<Target> getAvailableTargets() {
        return game
                .getHumanPlayer()
                .getTargets(missionType.getValue(), nation)
                .stream()
                .filter(t -> !t.getName().equalsIgnoreCase(airbase.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Add to the mission the given squadron with the given role.
     *
     * @param squadron The squadron added to the mission.
     * @param role The mission role of the added squadron.
     */
    public void addToMission(final Squadron squadron, final MissionRole role) {
        if (canAddSquadron(squadron)) {

            log.debug("Add squadron: '{}' with role: '{}' to mission id: '{}'", new Object[]{squadron.getTitle(), role.toString(), id});

            assigned.get(role).get().add(squadron);
            assignedExists.get(role).set(assigned.get(role).getValue().isEmpty());

            String assignedNames = assigned
                    .get(role)
                    .get()
                    .stream()
                    .map(Squadron::getTitle)
                    .collect(Collectors.joining(","));

            log.debug("Assigned squadrons: '{}'", assignedNames);

            totalAssigned.setValue(FXCollections.observableArrayList(add(totalAssigned.getValue(), squadron)));
            totalAssignedCount.setValue(totalAssigned.getValue().size());

            ready.setValue(FXCollections.observableArrayList(remove(ready.getValue(), squadron)));

            updateMissionStats();
        }
    }

    /**
     * Remove from the mission the given squadron with the given role.
     *
     * @param squadron The squadron removed from the mission.
     * @param role The mission role of the removed squadron.
     */
    public void removeFromMission(final Squadron squadron, final MissionRole role) {
        log.debug("remove squadron: '{}' with role: '{}' to mission id: '{}'", new Object[]{squadron.getTitle(), role.toString(), id});

        assigned.get(role).get().remove(squadron);
        assignedExists.get(role).set(assigned.get(role).getValue().isEmpty());

        String assignedNames = assigned
                .get(role)
                .get()
                .stream()
                .map(Squadron::getTitle)
                .collect(Collectors.joining(","));

        log.debug("Assigned squadrons: '{}'", assignedNames);

        totalAssigned.setValue(FXCollections.observableArrayList(remove(totalAssigned.getValue(), squadron)));
        totalAssignedCount.setValue(totalAssigned.getValue().size());

        ready.setValue(FXCollections.observableArrayList(add(ready.getValue(), squadron)));

        updateMissionStats();
    }

    /**
     * Clear the mission of all squadrons.
     */
    public void clearMission() {
        log.debug("Clear assigned squadrons for id: '{}'", id);

        Stream.of(MissionRole.values()).forEach(role -> {
            assigned.get(role).get().clear();
            assignedExists.get(role).set(false);

            String assignedNames = assigned
                    .get(role)
                    .get()
                    .stream()
                    .map(Squadron::getTitle)
                    .collect(Collectors.joining(","));

            log.debug("Assigned squadrons: '{}' for role: '{}'", assignedNames, role);

        });

        totalAssigned.setValue(FXCollections.observableList(new ArrayList<>()));
        totalAssignedCount.setValue(0);

        ready.set(FXCollections.observableList(new ArrayList<>(nationAirbaseViewModel.getTotalReadySquadrons().getValue())));
    }

    /**
     * Create this mission.
     */
    public void createMission() {
        int newId = game.getAirMissionId();

        log.debug("Create mission. Build mission id: '{}'", newId);

        mission = buildMission(newId);
        id = newId;
        missionId.setValue(id);

        log.debug("Mission object: '{}'", mission);

        checkCapacity = false;
        nationAirbaseViewModel.addMission(this);
    }

    /**
     * Update or edit this mission.
     */
    public void editMission() {
       log.debug("Edit mission. Build mission id: '{}'", id);

        mission = buildMission(id);

        log.debug("Mission object: '{}'", mission);

        checkCapacity = false;
        nationAirbaseViewModel.editMission(this);
    }

    /**
     * Determine if the given squadron is on this mission.
     *
     * @param squadron A squadron to check and see if it is on this mission.
     * @return True if the given squadron is on this mission. False otherwise.
     */
    public boolean isSquadronOnMission(final Squadron squadron) {
        return totalAssigned.getValue().contains(squadron);
    }

    /**
     * Bind the available squadrons.
     *
     * @param viewModel The nation's airbase view model.
     */
    private void bindAvailable(final NationAirbaseViewModel viewModel) {
        // Copy the ready squadrons. This must be done to handle the case where a added mission is canceled.
        ready.set(FXCollections.observableList(new ArrayList<>(viewModel.getTotalReadySquadrons().getValue())));

        Stream.of(MissionRole.values()).forEach(role -> {
            available.get(role).bind(Bindings.createObjectBinding(() -> FXCollections.observableArrayList(filter(missionType, target, role, ready)), missionType, target, ready));
            availableExists.get(role).bind(available.get(role).emptyProperty());
        });
    }

    /**
     * Bind the total steps in route to the target from this mission.
     */
    private void bindTotalStepsInRouteToTargetThisMission() {
        Callable<Integer> bindingFunction = () -> totalAssigned.getValue()
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();

        totalStepsInRouteToTargetThisMission.bind(Bindings.createIntegerBinding(bindingFunction, totalAssigned));
    }

    /**
     * Bind the total steps in route to the target from all other missions originating from this airbase.
     */
    private void bindTotalStepsInRouteToTargetOtherMissions() {
        SimpleListProperty<AirMissionViewModel> totalMissions = nationAirbaseViewModel
                .getAirbaseViewModel()
                .getTotalMissions();

        Callable<Integer> bindingFunction = () -> totalMissions
                    .getValue()
                    .stream()
                    .filter(airMission -> airMission.id != this.id)
                    .filter(airMission -> airMission.getMissionType().getValue() == missionType.getValue())
                    .filter(airMission -> airMission.getTarget().getValue() == target.getValue())
                    .flatMap(airMission -> airMission.getTotalAssigned().getValue().stream())
                    .map(Squadron::getSteps)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .intValue();

        totalStepsInRouteToTargetOtherMissions.bind(Bindings.createIntegerBinding(bindingFunction, totalMissions, target, missionType));
    }

    /**
     * Bind the total steps in route to the target from all airbases.
     */
    private void bindTotalStepsInRouteToTarget() {
        Callable<Integer> bindingFunction = () -> totalStepsInRouteToTargetThisMission.getValue()
                + totalStepsInRouteToTargetOtherMissions.getValue()
                + Optional.ofNullable(target.getValue()).map(t -> t.getMissionSteps(airbase)).orElse(0);

        totalStepsInRouteToTarget.bind(Bindings.createIntegerBinding(bindingFunction,
                totalStepsInRouteToTargetThisMission, totalStepsInRouteToTargetOtherMissions, target));
    }

    /**
     * Bind the total steps in route to the target's region from this mission.
     */
    private void bindTotalStepsInRouteToTargetRegionThisMission() {
        Callable<Integer> bindingFunction = () -> airbase.getRegion(nation) == getTargetRegion()
                ? 0
                : totalStepsInRouteToTargetThisMission.getValue();

        totalStepsInRouteToTargetRegionThisMission.bind(Bindings.createIntegerBinding(bindingFunction, target, totalStepsInRouteToTargetThisMission));
    }

    /**
     * Bind the total steps in route to the target's region from all other missions.
     */
    private void bindTotalStepsInRouteToTargetRegionOtherMissions() {
        SimpleListProperty<AirMissionViewModel> totalMissions = nationAirbaseViewModel
                .getAirbaseViewModel()
                .getTotalMissions();

        Callable<Integer> bindingFunction = () -> totalMissions
                .getValue()
                .stream()
                .filter(airMission -> airMission.id != this.id)
                .filter(airMission -> airMission.getMissionType().getValue() == missionType.getValue())
                .filter(this::filterTargetRegion)
                .flatMap(airMission -> airMission.getTotalAssigned().getValue().stream())
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();

        totalStepsInRouteToTargetRegionOtherMissions.bind(Bindings.createIntegerBinding(bindingFunction, totalMissions, target, missionType));
    }

    /**
     * Bind the total steps in route to the target's region.
     */
    private void bindTotalStepsInRouteToTargetRegion() {
        Callable<Integer> bindingFunction = () -> totalStepsInRouteToTargetRegionThisMission.getValue()
                + totalStepsInRouteToTargetRegionOtherMissions.getValue()
                + Optional.ofNullable(target.getValue())
                    .map(t -> t.getMissionStepsEnteringRegion(missionType.getValue(), nation, airbase))
                    .orElse(0);

        totalStepsInRouteToTargetRegion.bind(Bindings.createIntegerBinding(bindingFunction,
                totalStepsInRouteToTargetRegionThisMission, totalStepsInRouteToTargetRegionOtherMissions, target, missionType));
    }

    /**
     * Bind the total steps leaving this airbase's region from this mission.
     */
    private void bindTotalStepsFromThisMissionLeavingRegion() {
        Callable<Integer> bindingFunction = () -> airbase.getRegion(nation) == getTargetRegion()
                ? 0
                : totalStepsInRouteToTargetThisMission.getValue();

        totalStepsFromThisMissionLeavingRegion.bind(Bindings.createIntegerBinding(bindingFunction, target, totalStepsInRouteToTargetThisMission));
    }

    /**
     * Bind the total steps leaving this airbase's region from all the other missions of this airbase.
     */
    private void bindTotalStepsFromOtherMissionsLeavingRegion() {
        SimpleListProperty<AirMissionViewModel> totalMissions = nationAirbaseViewModel
                .getAirbaseViewModel()
                .getTotalMissions();

        Callable<Integer> bindingFunction = () -> totalMissions
                    .getValue()
                    .stream()
                    .filter(airMission -> airMission.id != this.id)
                    .filter(airMission -> airMission.getMissionType().getValue() == missionType.getValue())
                    .filter(this::filterAirbaseRegion)
                    .flatMap(airMission -> airMission.getTotalAssigned().getValue().stream())
                    .map(Squadron::getSteps)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .intValue();

        totalStepsFromOtherMissionsLeavingRegion.bind(Bindings.createIntegerBinding(bindingFunction, totalMissions, missionType));
    }

    /**
     * Bind the total steps leaving this airbase's region.
     */
    private void bindTotalStepsLeavingRegion() {
        Callable<Integer> bindingFunction = () -> totalStepsFromThisMissionLeavingRegion.getValue()
                + totalStepsFromOtherMissionsLeavingRegion.getValue()
                + Optional.ofNullable(target.getValue())
                    .map(t -> t.getMissionStepsLeavingRegion(missionType.getValue(), nation, airbase))
                    .orElse(0);

        totalStepsLeavingRegion.bind(Bindings.createIntegerBinding(bindingFunction,
                totalStepsFromThisMissionLeavingRegion, totalStepsFromOtherMissionsLeavingRegion, target, missionType));
    }

    /**
     * Get the available squadron for the given role that may perform this mission.
     *
     * @param selectedMissionType The selected mission type.
     * @param selectedTarget The selected target.
     * @param role The mission role.
     * @param allReady The airbase's ready squadrons.
     * @return A list of the airbase's ready squadrons that can do this mission.
     */
    private List<Squadron> filter(final ObjectProperty<AirMissionType> selectedMissionType,
                                  final ObjectProperty<Target> selectedTarget,
                                  final MissionRole role,
                                  final SimpleListProperty<Squadron> allReady) {

        clearError(role);

        if (checkCapacity && targetHasNoCapacity()) {
            setError(role, selectedTarget.getValue().getTitle() + " is at max capacity");
            return Collections.emptyList();
        }

        if (checkCapacity && targetRegionHasNoCapacity()) {
            setError(role, selectedTarget.getValue().getRegionTitle(nation) + " is at max capacity");
            return Collections.emptyList();
        }

        if (checkCapacity && allReady.getValue().isEmpty()) {
            setError(role, "No ready squadrons");
            return Collections.emptyList();
        }

        List<Squadron> capable = allReady
                .getValue()
                .stream()
                .filter(squadron -> mayAttack(squadron, selectedTarget.getValue()))
                .collect(Collectors.toList());

        if (checkCapacity && capable.isEmpty()) {
            setError(role, "No squadrons capable.");
            return Collections.emptyList();
        }

        List<Squadron> canDoMission = capable
                .stream()
                .filter(squadron -> canDoMission(squadron, selectedMissionType.getValue()))
                .collect(Collectors.toList());

        if (checkCapacity && canDoMission.isEmpty()) {
            setError(role, "No squadrons can do mission");
            return Collections.emptyList();
        }

        List<Squadron> canDoRole = canDoMission
                .stream()
                .filter(squadron -> squadron.canDoRole(role))
                .collect(Collectors.toList());

        if (checkCapacity && canDoRole.isEmpty()) {
            setError(role, "No squadrons can do role");
            return Collections.emptyList();
        }

        List<Squadron> inRange = canDoRole
                .stream()
                .filter(squadron -> inRange(squadron, selectedTarget.getValue(), selectedMissionType.getValue(), role))
                .collect(Collectors.toList());

        if (checkCapacity && inRange.isEmpty()) {
            setError(role, "No squadrons in range");
        }

        return inRange;
    }

    /**
     * Determine if the given squadron can be added to the given target.
     *
     * @param squadron A squadron.
     * @return True if the squadron can be added to the target. False otherwise.
     */
    private boolean canAddSquadron(final Squadron squadron) {
        clearWarning();

        if (squadron == null) {
            return false;
        }

        if (targetHasNoCapacity(squadron)) {
            setWarning(target.getValue().getTitle() + " does not have capacity.");
            return false;
        }

        if (targetRegionHasNoCapacity(squadron)) {
            setWarning(target.getValue().getRegionTitle(nation) + " does not have capacity.");
            return false;
        }

        if (regionMinimumNotSatisfied(squadron)) {
            setWarning(airbase.getRegionTitle() + " minimum required steps not satisfied.");
            return false;
        }

        return true;
    }

    /**
     * Determine if the current target has capacity to accept new squadrons.
     *
     * @return True if the current target has no capacity. False otherwise.
     */
    private boolean targetHasNoCapacity() {
        return Optional.ofNullable(target.getValue())
                .map(t -> !t.hasAirbaseCapacity(airbase, totalStepsInRouteToTargetOtherMissions.getValue()))
                .orElse(false);
    }

    /**
     * Determine if the current target has capacity to accept the given squadron.
     *
     * @param squadron A squadron added to a mission with the selected target as its target.
     * @return True if the current target has no capacity. False otherwise.
     */
    private boolean targetHasNoCapacity(final Squadron squadron) {
        if (squadron == null) {
            return false;
        }

        int squadronSteps = squadron.getSteps().intValue();
        return Optional.ofNullable(target.getValue())
                .map(t -> !t.hasAirbaseCapacity(airbase, totalStepsInRouteToTarget.getValue() + squadronSteps))
                .orElse(false);
    }


    /**
     * Determine if the current target's region has capacity to accept new squadrons.
     *
     * @return True if the current target's region has no capacity. False otherwise.
     */
    private boolean targetRegionHasNoCapacity() {
        return Optional.ofNullable(target.getValue())
                .map(t -> !t.hasRegionCapacity(nation, airbase, totalStepsInRouteToTargetRegionOtherMissions.getValue()))
                .orElse(false);
    }

    /**
     * Determine if the current target's region has capacity to accept new squadrons.
     *
     * @param squadron A squadron added to a mission with the selected target as its target.
     * @return True if the current target's region has no capacity. False otherwise.
     */
    private boolean targetRegionHasNoCapacity(final Squadron squadron) {
        if (squadron == null) {
            return false;
        }

        int squadronSteps = squadron.getSteps().intValue();
        return Optional.ofNullable(target.getValue())
                .map(t -> !t.hasRegionCapacity(nation, airbase, totalStepsInRouteToTargetRegion.getValue() + squadronSteps))
                .orElse(false);
    }

    /**
     * Determine if the region minimum squadron requirement is met.
     *
     * @param squadron A squadron that might be removed from the airbase's region.
     * @return True if the origin's airbase minimum region is not satisfied. False otherwise.
     */
    private boolean regionMinimumNotSatisfied(final Squadron squadron) {
        if (squadron == null) {
            return false;
        }

        // All airbase's should have a region.
        Region missionOriginRegion = airbase.getRegion(nation);

        // Not all targets are in a region.
        Region missionDestinationRegion = Optional.ofNullable(target.getValue())
                .map(r -> r.getRegion(nation))
                .orElse(null);

        int totalMissionSquadronSteps = totalStepsLeavingRegion.getValue() + squadron.getSteps().intValue();

        int totalOtherAirbases = Optional.ofNullable(target.getValue())
                .map(t -> t.getMissionStepsLeavingRegion(missionType.getValue(), nation, airbase))
                .orElse(0);

        int totalSteps = totalMissionSquadronSteps + totalOtherAirbases;

        //If the target has no region then this method just returns true.
        return Optional
                .ofNullable(missionDestinationRegion)
                .filter(destRegion -> destRegion != missionOriginRegion)
                .map(dummy -> !missionOriginRegion.minimumSatisfied(totalSteps))
                .orElse(false);
    }

    /**
     * Build the mission.
     *
     * @param airMissionId The air mission id.
     * @return An air mission.
     */
    private AirMission buildMission(final int airMissionId) {
        String targetName = Optional
                .ofNullable(target.getValue())
                .map(Target::getName)
                .orElse("");

        MissionData data = new MissionData();
        data.setId(airMissionId);
        data.setAirbase(airbase);
        data.setNation(nation);
        data.setType(missionType.getValue());
        data.setTarget(targetName);

        Map<MissionRole, List<String>> squadrons = assigned
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry
                                .getValue()
                                .getValue()
                                .stream()
                                .map(Squadron::getName)
                                .collect(Collectors.toList())));

        data.setSquadronMap(squadrons);

        return missionDAO.load(data);
    }

    /**
     * Copy the mission from the model.
     *
     * @param missionModel The mission form the model.
     * @return A newly copied mission.
     */
    private AirMission buildMission(final AirMission missionModel) {
        MissionData data = new MissionData();
        data.setId(missionModel.getId());
        data.setAirbase(missionModel.getAirbase());
        data.setNation(missionModel.getNation());
        data.setType(missionModel.getType());
        data.setTarget(missionModel.getTarget().getName());

        Map<MissionRole, List<String>> squadrons = missionModel
                .getSquadronMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry
                                .getValue()
                                .stream()
                                .map(Squadron::getName)
                                .collect(Collectors.toList())));

        data.setSquadronMap(squadrons);

        return missionDAO.load(data);
    }

    /**
     * Update the mission stats.
     */
    private void updateMissionStats() {
        log.debug("Build temp mission with id 0");

        AirMission tempMission = buildMission(0);

        log.debug("Mission object: '{}'", tempMission);

        List<ProbabilityStats> stats = tempMission.getMissionProbability();
        missionStats.set(FXCollections.observableArrayList(stats));
    }

    /**
     * Determine if the squadron can perform the mission.
     *
     * @param squadron A squadron.
     * @param type The mission type.
     * @return True if the given squadron can perform the mission. False otherwise.
     */
    private boolean canDoMission(final Squadron squadron, final AirMissionType type) {
        return Optional.ofNullable(type).map(squadron::canDoMission).orElse(false);
    }

    /**
     * Determine if the squadron is allowed to attack the given target.
     *
     * @param squadron A squadron.
     * @param selectedTarget The target.
     * @return True if the squadron is allowed to attack the given target.
     */
    private boolean mayAttack(final Squadron squadron, final Target selectedTarget) {
        return Optional.ofNullable(selectedTarget).map(t -> t.mayAttack(squadron)).orElse(false);
    }

    /**
     * Determine if the squadron is in range of the selected target.
     *
     * @param squadron A squadron.
     * @param selectedTarget The selected target.
     * @param type The mission type.
     * @param role The mission role.
     * @return True if the given squadron can perform the mission. False otherwise.
     */
    private boolean inRange(final Squadron squadron, final Target selectedTarget, final AirMissionType type, final MissionRole role) {
        return Optional.ofNullable(selectedTarget).map(t -> squadron.inRange(t, type, role)).orElse(false);
    }

    /**
     * Set the error property and its text.
     *
     * @param role The mission role.
     * @param text The error text.
     */
    private void setError(final MissionRole role, final String text) {
        errorText.put(role, text);
        error.get(role).set(true);
    }

    /**
     * Clear the error property and its text.
     *
     * @param role The mission role.
     */
    private void clearError(final MissionRole role) {
        errorText.put(role, "");
        error.get(role).set(false);
    }

    /**
     * Set the warning property and its text.
     *
     * @param text The warning text.
     */
    private void setWarning(final String text) {
        warningText = text;
        warning.set(true);
    }

    /**
     * Clear the warning and its text.
     */
    private void clearWarning() {
        warningText = "";
        warning.set(false);
    }

    /**
     * Utility function for adding to an observable list.
     *
     * @param list the list
     * @param t an element added to the list.
     * @param <T> The type of elements in the list.
     * @return The list.
     */
    private <T> ObservableList<T> add(final ObservableList<T> list, final T t) {
        list.add(t);
        return list;
    }

    /**
     * Utility function for removing from an observable list.
     *
     * @param list the list
     * @param t an element added to the list.
     * @param <T> The type of elements in the list.
     * @return The list.
     */
    private <T> ObservableList<T> remove(final ObservableList<T> list, final T t) {
        list.remove(t);
        return list;
    }

    /**
     * Filter the target's region. Only return true for other mission targets that match this mission's target.
     * The target's region cannot be the same region of the originating airfield. The mission must be to a different
     * region that the originating airbase.
     *
     * @param viewModel The air mission view model.
     * @return True if the given view model's target region is equal to this air mission view model's target.
     * And the target's region is different than the originating airbase's region. False otherwise.
     */
    private boolean filterTargetRegion(final AirMissionViewModel viewModel) {
        //This air mission's target region.
        Region targetRegion = Optional.ofNullable(target.getValue())
                .map(t -> t.getRegion(nation))
                .orElse(null);

        // The target region of the other mission.
        Region missionRegion = Optional
                .ofNullable(viewModel.getTarget())
                .map(ObjectExpression::getValue)
                .map(t -> t.getRegion(nation))
                .orElse(null);

        return  missionRegion == targetRegion && missionRegion != airbase.getRegion(nation);
    }

    /**
     * Filter the airbase's region.
     *
     * @param viewModel The air mission view model.
     * @return True if the mission's region is different than the originating airbase's region. False otherwise.
     */
    private boolean filterAirbaseRegion(final AirMissionViewModel viewModel) {

        // The target region of the other mission.
        Region missionRegion = Optional
                .ofNullable(viewModel.getTarget())
                .map(ObjectExpression::getValue)
                .map(t -> t.getRegion(nation))
                .orElse(null);

        return missionRegion != airbase.getRegion(nation);
    }

    /**
     * Get the target's region.
     *
     * @return The target's region.
     */
    private Region getTargetRegion() {
        return Optional
                .ofNullable(target.getValue())
                .map(t -> t.getRegion(nation))
                .orElse(null);
    }
}
