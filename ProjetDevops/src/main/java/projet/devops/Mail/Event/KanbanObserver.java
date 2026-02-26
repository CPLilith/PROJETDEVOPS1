package projet.devops.Mail.Event;

import org.springframework.stereotype.Component;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Mail;

@Component
public class KanbanObserver implements MailEventObserver {

    @Override
    public void onMailClassified(MailClassifiedEvent event) {
        Mail mail = event.getMail();

        if (mail.getAction() == EisenhowerAction.DELEGATE) {
            if (mail.getStatus() == null || mail.getStatus().isBlank()) {
                mail.setStatus("SUIVI");
            }
            System.out.println("[KanbanObserver] Mail DELEGATE ajouté au suivi : " + mail.getSubject());
        }
    }
}