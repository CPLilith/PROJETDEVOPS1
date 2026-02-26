package projet.devops.Mail.Event;

import org.springframework.stereotype.Component;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Mail;

@Component
public class AgendaObserver implements MailEventObserver {

    @Override
    public void onMailClassified(MailClassifiedEvent event) {
        Mail mail = event.getMail();

        if (mail.getAction() == EisenhowerAction.PLAN) {
            System.out.println("[AgendaObserver] Mail PLAN détecté pour l'agenda : " + mail.getSubject());
        }

        if (mail.getAction().isDo()) {
            System.out.println("[AgendaObserver] Mail DO détecté, tag effectif : " + mail.getEffectiveTag());
        }
    }
}