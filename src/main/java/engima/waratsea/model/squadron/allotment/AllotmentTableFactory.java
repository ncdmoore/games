package engima.waratsea.model.squadron.allotment;

import engima.waratsea.model.squadron.allotment.data.AllotmentTableData;

/**
 * Creates allotment tables.
 */
public interface AllotmentTableFactory {
    /**
     * Creates an Allotment table.
     *
     * @param data Allotment table data read from a JSON file.
     * @return An allotment initialized with the data from the JSON file.
     */
    AllotmentTable create(AllotmentTableData data);
}
