package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.Dice;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for handling mission squadrons.
 */
public class MissionSquadrons {
    @Getter private Map<MissionRole, List<Squadron>> squadronMap;

    @Getter private final List<Squadron> turnedAway = new ArrayList<>();

    private final Dice dice;

    @Inject
    public MissionSquadrons(final Dice dice) {
        this.dice = dice;
    }

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
     * Get the squadron on this mission for the given role.
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
     * Turn away the given number of steps on this mission.
     *
     * @param numStepsToTurnAway The number of squadrons to turn away from the mission.
     */
    public void turnAway(final int numStepsToTurnAway) {
        for (int i = 0; i < numStepsToTurnAway; i++) {
            int numAttackingSquadrons = squadronMap.get(MissionRole.MAIN).size();

            // Pick a random squadron to turn away.
            int squadronIndex = dice.roll(numAttackingSquadrons) - 1;  // list indices start at 0.

            if (squadronIndex < numAttackingSquadrons) {
                Squadron squadron = squadronMap.get(MissionRole.MAIN).get(squadronIndex);
                squadron.reduceEffectiveStrength();

                turnedAway.add(squadron);

                if (squadron.isNotEffective()) {
                    // Remove the squadron so it cannot be turned away again.
                    // It if already fully turned away.
                    squadronMap.get(MissionRole.MAIN).remove(squadron);
                }
            }
        }
    }

    /**
     * Destroy the given number of steps on this mission.
     *
     * @param numStepsToDestroy Number of steps to destroy.
     */
    public void destroy(final int numStepsToDestroy) {
        for (int i = 0; i < numStepsToDestroy; i++) {
            int numTurnedAwaySquadrons = turnedAway.size();

            int squadronIndex = dice.roll(numTurnedAwaySquadrons) - 1; // list indices start at 0.

            if (squadronIndex < numTurnedAwaySquadrons) {
                Squadron squadron = turnedAway.get(squadronIndex);
                squadron.reduceStrength();

                if (squadron.isDestroyed()) {
                    squadronMap.get(MissionRole.MAIN).remove(squadron);
                    turnedAway.remove(squadron);
                    Airbase airbase = squadron.getHome();
                    airbase.removeSquadron(squadron);
                }
            }
        }
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
