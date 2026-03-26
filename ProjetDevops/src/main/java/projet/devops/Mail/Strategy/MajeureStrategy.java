package projet.devops.Mail.Strategy;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class MajeureStrategy implements DelegationStrategy {
    
    @Override
    public List<LocalDate> calculateReminderDates(LocalDate deadline, LocalDate currentDate) {
        // Confiance Majeure : On fait juste un rappel la veille.
        LocalDate veille = deadline.minusDays(1);
        
        // Sécurité : si la tâche est pour aujourd'hui ou demain, le rappel est aujourd'hui
        if (veille.isBefore(currentDate) || veille.isEqual(currentDate)) {
            return Collections.singletonList(currentDate);
        }
        
        return Collections.singletonList(veille);
    }
}