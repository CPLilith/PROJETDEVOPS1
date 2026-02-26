package projet.devops.Mail.Event;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MailEventPublisher {

    private final List<MailEventObserver> observers;

    public MailEventPublisher(List<MailEventObserver> observers) {
        this.observers = observers;
        System.out.println("[MailEventPublisher] " + observers.size() + " observer(s) enregistré(s).");
    }

    public void publish(MailClassifiedEvent event) {
        for (MailEventObserver observer : observers) {
            try {
                observer.onMailClassified(event);
            } catch (Exception e) {
                System.err.println("[MailEventPublisher] Erreur dans " + observer.getClass().getSimpleName() + " : " + e.getMessage());
            }
        }
    }
}