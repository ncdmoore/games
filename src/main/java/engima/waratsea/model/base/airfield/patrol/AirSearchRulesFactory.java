package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.game.AssetType;

public interface AirSearchRulesFactory {
    /**
     * Creates air search rules.
     *
     * @param assetType The type of asset searched for.
     * @return The air search rules.
     */
    AirSearchRules create(AssetType assetType);
}
