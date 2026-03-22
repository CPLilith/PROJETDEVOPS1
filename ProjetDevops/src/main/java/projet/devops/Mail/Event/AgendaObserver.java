package projet.devops.Mail.Event;

import org.springframework.stereotype.Component;

import projet.devops.Mail.Model.EisenhowerAction;
import projet.devops.Mail.Model.Mail;
import projet.devops.Mail.Service.CalendarIntelligenceService;

@Component
public class AgendaObserver implements MailEventObserver {

    private final CalendarIntelligenceService calendarIntelligenceService;

    // Injection du nouveau service
    public AgendaObserver(CalendarIntelligenceService calendarIntelligenceService) {
        this.calendarIntelligenceService = calendarIntelligenceService;
    }

    @Override
    public void onMailClassified(MailClassifiedEvent event) {
        Mail mail = event.getMail();

        // Si le mail nécessite une action d'agenda (PLAN ou DELEGATE)
        if (mail.getAction() == EisenhowerAction.PLAN || mail.getAction() == EisenhowerAction.DELEGATE) {
            System.out.println("[AgendaObserver] Déclenchement de l'IA Agenda pour : " + mail.getSubject());
            
            // L'IA analyse le mail et stocke l'intention
            calendarIntelligenceService.analyzeAndStoreIntent(
                mail.getMessageId(), 
                mail.getSubject(), 
                mail.getContent()
            );
        }

        if (mail.getAction().isDo()) {
            System.out.println("[AgendaObserver] Mail DO détecté, tag effectif : " + mail.getEffectiveTag());
        }
    }
}