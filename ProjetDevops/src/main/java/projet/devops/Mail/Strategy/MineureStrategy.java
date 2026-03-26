package projet.devops.Mail.Strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MineureStrategy implements DelegationStrategy {

    @Override
    public List<LocalDate> calculateReminderDates(LocalDate deadline, LocalDate currentDate) {
        long daysBetween = ChronoUnit.DAYS.between(currentDate, deadline);
        
        if (daysBetween <= 1) {
            return Collections.singletonList(currentDate);
        } else if (daysBetween <= 3) {
            // S'il reste très peu de temps, on ne fait qu'un rappel la veille pour ne pas spammer
            return Collections.singletonList(deadline.minusDays(1));
        }
        
        // Sinon, on place deux rappels : un à 50% du temps, et un la veille
        long halfTime = (long) (daysBetween * 0.50);
        LocalDate firstReminder = currentDate.plusDays(halfTime);
        LocalDate secondReminder = deadline.minusDays(1);
        
        return Arrays.asList(firstReminder, secondReminder); // Renvoie une liste de 2 dates !
    }
}