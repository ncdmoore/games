package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.utility.Executer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class represents the phases of a game turn.
 *
 * The order of the phases is defined by the order of the Phase enum.
 * @see Phase
 *
 * This class implements the strategy design pattern in that the
 * phases of a turn may be configured. Note, by default that the
 * phases of a turn include all the defined phase enums.
 */
@Singleton
public class Phases {
    private final Map<Phase, Executer> phases = new HashMap<>();
    private List<Phase> turnPhases;

    @Inject
    public Phases() {
        // By default, a game's turn phases include all the defined turn phases.
        turnPhases = Phase
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * Configure the game's turn phases. If a game needs a special phase
     * then this can be used to configure the turn's phases. This will
     * then be needed by games that do not need the special phase as by
     * default all defined phases are included in the game's turn phases.
     *
     * @param newPhases The game's turn phases.
     */
    public void configure(final List<Phase> newPhases) {
        turnPhases = newPhases;
    }

    /**
     * Register a new phase of a game turn.
     * @param phase The new phase.
     * @param executer The callback that executes the newly registered phase.
     */
    public void register(final Phase phase, final Executer executer) {
        phases.put(phase, executer);
    }

    /**
     * Execute each phase of a game turn.
     */
    public void execute() {
        turnPhases
                .stream()
                .map(phases::get)
                .filter(Objects::nonNull)
                .forEach(Executer::execute);
    }
}
