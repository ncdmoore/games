package engima.waratsea.model.base.airfield.mission;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.target.Target;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for handling mission squadrons.
 */
public class Squadrons {
    @Getter private Map<MissionRole, List<Squadron>> squadronMap;

    /**
     * Set the squadrons from squadron data.
     *
     * @param airbase The home airbase of the squadrons
     * @param data The squadron data.
     */
    public void setSquadrons(final Airbase airbase, final Map<MissionRole, List<String>> data) {
        squadronMap = Optional
                .ofNullable(data)
                .orElseGet(Collections::emptyMap)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry
                        .getValue()
                        .stream()
                        .map(airbase::getSquadron)
                        .collect(Collectors.toList())));
    }

    /**
     * Get the squadron data.
     *
     * @return The squadron data that is written to a JSON file for permanent storage.
     */
    public Map<MissionRole, List<String>> getData() {
        return squadronMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry
                        .getValue()
                        .stream()
                        .map(Squadron::getName)
                        .collect(Collectors.toList())));
    }

    /**
     * Get the sqaudron on this mission for the given role.
     *
     * @param role Air mission role.
     * @return A list of squadrons performing the given role on this mission.
     */
    public List<Squadron> get(final MissionRole role) {
        return squadronMap.getOrDefault(role, Collections.emptyList());
    }

    /**
     * Get both the squadrons on the mission and the squadrons on escort duty.
     * For this type of mission there are no escorts. So just the squadrons
     * are returned.
     *
     * @return All of the squadrons involved with this mission.
     */
    public List<Squadron> getAll() {
        return Stream
                .of(MissionRole.values())
                .flatMap(role -> squadronMap.get(role).stream())
                .collect(Collectors.toList());
    }

    /**
     * Get the number of steps assigned to this mission.
     *
     * @return the total number of steps assigned to this mission.
     */
    public int getSteps() {
        return MissionRole.stream()
                .flatMap(role -> squadronMap.get(role).stream())
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }

    /**
     * Get the minimum radius of all the squadrons on this mission.
     *
     * @return The minimum squadron radius.
     */
    public int getMinimumRange() {
        return MissionRole.stream()
                .flatMap(role -> squadronMap.get(role).stream())
                .map(Squadron::getRange)
                .mapToInt(v -> v)
                .min()
                .orElse(0);
    }

    /**
     * Set the squadrons to the correct state.
     *
     * @param target The target of this mission.
     * @param type The type of mission.
     */
    public void add(final Target target, final AirMissionType type) {
        squadronMap
                .forEach((role, squadrons) -> squadrons
                        .forEach(squadron -> {
                            squadron.setState(SquadronAction.ASSIGN_TO_MISSION);
                            squadron.equip(target, type, role);
                        }));
    }

    /**
     * Remove all the squadrons from the mission.
     */
    public void remove() {
        squadronMap
                .forEach((role, squadrons) -> squadrons
                    .forEach(squadron -> {
                        squadron.setState(SquadronAction.REMOVE_FROM_MISSION);
                        squadron.unEquip();
                }));

        squadronMap.clear();
    }

    /**
     * Get the number os squadrons on this mission.
     *
     * @return The number of squadrons on this mission.
     */
    public int getNumber() {
        return MissionRole
                .stream()
                .map(role -> squadronMap.get(role).size())
                .reduce(0, Integer::sum);
    }

    /**
     * The squadrons take off.
     */
    public void takeOff() {
        MissionRole.stream()
                .flatMap(role -> squadronMap.get(role).stream())
                .forEach(Squadron::takeOff);
    }

    /**
     * The squadrons land.
     */
    public void land() {
        MissionRole.stream()
                .flatMap(role -> squadronMap.get(role).stream())
                .forEach(Squadron::land);
    }
}
