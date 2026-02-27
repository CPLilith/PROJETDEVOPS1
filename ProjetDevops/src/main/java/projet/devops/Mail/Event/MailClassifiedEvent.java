package projet.devops.Mail.Event;

import projet.devops.Mail.Model.Mail;

public class MailClassifiedEvent {

    private final Mail mail;

    public MailClassifiedEvent(Mail mail) {
        this.mail = mail;
    }

    public Mail getMail() {
        return mail;
    }
}