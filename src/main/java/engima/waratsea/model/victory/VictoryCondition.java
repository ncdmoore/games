package engima.waratsea.model.victory;

/**
 * Victory condition.
 */
public interface VictoryCondition {
    /**
     * Indicates that the victory condition was satisfied.
     * @return True if hte victory condition was satisfied. False otherwise.
     */
    boolean isRequirementMet();
}
