package projet.devops.Mail.Event;

public interface MailEventObserver {
    void onMailClassified(MailClassifiedEvent event);
}