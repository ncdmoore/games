package engima.waratsea.model.squadron.allotment;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.allotment.data.AllotmentTableData;

/**
 * Creates allotment tables.
 */
public interface AllotmentTableFactory {
    /**
     * Creates an Allotment table.
     *
     * @param nation The nation BRITISH, ITALIAN, etc...
     * @param data Allotment table data read from a JSON file.
     * @return An allotment initialized with the data from the JSON file.
     */
    AllotmentTable create(Nation nation, AllotmentTableData data);
}
