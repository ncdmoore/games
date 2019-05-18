package engima.waratsea.model.scenario;

import engima.waratsea.model.squadron.deployment.SquadronDeploymentType;
import engima.waratsea.model.weather.WeatherType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class represents a game scenario.
 */
@Slf4j
public class Scenario implements Comparable<Scenario> {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String image;

    @Getter
    @Setter
    private String description;

    private Date date;

    @Getter
    @Setter
    private WeatherType weather;

    @Getter
    @Setter
    private int maxTurns;

    @Getter
    @Setter
    private String map;

    @Getter
    @Setter
    private String objectives;

    @Getter
    @Setter
    private SquadronDeploymentType squadron;

    @Getter
    @Setter
    private boolean minefieldForHumanSide;

    /**
     * The scenario constructor.
     */
    public Scenario() {
        squadron = SquadronDeploymentType.HUMAN;
    }

    /**
     * Get the scenario start date.
     *
     * @return The scenario start date.
     */
    public String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
        return simpleDateFormat.format(date);
    }

    /**
     * Set the scenario start date.
     *
     * @param date The scenario start date.
     */
    @SuppressWarnings("unused") // Gson sets the date.
    public void setDate(final Date date) {
        this.date = new Date(date.getTime());
    }

    /**
     * Get the scenario month.
     *
     * @param separator The string separator between the month and year.
     *
     * @return The month and year.
     */
    public String getMonthYear(final String separator) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        return month + separator + year;
    }

    /**
     * Get the scenario year.
     *
      * @return The scenario year.
     */
    public String getYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) + "";
    }

    /**
     * Returns the string health of the scenario. This is used in the javafx list view control.
     *
     * @return The string health of the scenario.
     */
    @Override
    public String toString() {
        return id + ". " + title;
    }

    /**
     * Called to sort scenarios.
     *
     * @param otherScenario The 'other' scenario in which this scenario is compared.
     * @return an integer indicating whether this scenario is alphabetically first, last or equal with the other
     * scenario.
     */
    @Override
    public int compareTo(@Nonnull final Scenario otherScenario) {
        return id.compareTo(otherScenario.id);
    }

    /**
     * Determines if two scenario objects are equal.
     *
     * @param other The 'other' scenario object.
     * @return True if the two scenario objects are equal. False otherwise.
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Scenario) && id.equals(((Scenario) other).id);
    }

    /**
     * Defined just to make findbugs happy.
     *
     * @return The super classes hashcode.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
