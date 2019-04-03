package engima.waratsea.model.squadron.allotment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.squadron.allotment.data.AllotmentData;
import engima.waratsea.model.squadron.allotment.data.AllotmentTableData;
import engima.waratsea.model.squadron.data.SquadronData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a nations squadron allotment.
 *
 */
@Slf4j
public class Allotment {
    private final AllotmentTableFactory factory;

    @Getter
    private final Nation nation;

    @Getter
    private final List<SquadronData> bombers;

    @Getter
    private final List<SquadronData> fighters;

    @Getter
    private final List<SquadronData> recon;

    /**
     * Constructor called by guice.
     *
     * @param data Allotment data read in from a JSON file.
     * @param factory Allotment table factory.
     */
    @Inject
    public Allotment(@Assisted final AllotmentData data,
                               final AllotmentTableFactory factory) {
        this.factory = factory;

        nation = data.getNation();

        log.debug("Nation allotment: '{}'", nation);

        log.debug("Bombers:");
        bombers = Optional.ofNullable(data.getBombers())
                .map(this::getSquadrons)
                .orElse(Collections.emptyList());

        log.debug("Fighters:");
        fighters = Optional.ofNullable(data.getFighters())
                .map(this::getSquadrons)
                .orElse(Collections.emptyList());

        log.debug("Recon:");
        recon = Optional.ofNullable(data.getRecon())
                .map(this::getSquadrons)
                .orElse(Collections.emptyList());
    }

    /**
     * Get all the squadron types.
     *
     * @return A stream of all the squadron types.
     */
    public Stream<SquadronData> get() {
        return Stream.of(bombers, fighters, recon).flatMap(Collection::stream);
    }

    /**
     * Get the squadrons from the allotment table.
     *
     * @param data The allotment table data.
     * @return A list of squadron models.
     */
    private List<SquadronData> getSquadrons(final AllotmentTableData data) {
        return factory.create(data).getSquadrons();
    }
}
