package projet.devops.Mail.Strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class MoyenneStrategy implements DelegationStrategy {

    @Override
    public List<LocalDate> calculateReminderDates(LocalDate deadline, LocalDate currentDate) {
        // On calcule combien de jours il reste entre aujourd'hui et la deadline
        long daysBetween = ChronoUnit.DAYS.between(currentDate, deadline);
        
        if (daysBetween <= 1) {
            return Collections.singletonList(currentDate);
        }
        
        // On calcule 75% du temps écoulé (ex: sur 10 jours, on ajoute 7 jours à la date actuelle)
        long daysToAdd = (long) (daysBetween * 0.75);
        LocalDate reminderDate = currentDate.plusDays(daysToAdd);
        
        return Collections.singletonList(reminderDate);
    }
}