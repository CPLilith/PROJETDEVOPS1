package projet.devops.Mail.Strategy;

import java.time.LocalDate;
import java.util.List;

public interface DelegationStrategy {
    /**
     * Calcule les dates de relance en fonction de la deadline et de la date du jour.
     */
    List<LocalDate> calculateReminderDates(LocalDate deadline, LocalDate currentDate);
}