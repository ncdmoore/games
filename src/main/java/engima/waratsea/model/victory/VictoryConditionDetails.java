package engima.waratsea.model.victory;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * This class represents the generic victory condition details.
 * All victory condition classes can be represented as a collection of String key-value pairs.
 * This class is used to hold these key-value pairs.
 */
public class VictoryConditionDetails {
    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private Map<String, String> info;

    /**
     * The String representation of this object.
     *
     * @return The String representation of this object.
     */
    @Override
    public String toString() {
        return key;
    }

}
