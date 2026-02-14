package projet.devops.Mail.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.MessageIDTerm;
import projet.devops.Mail.Mail;

@Service
public class MailService {

    @Value("${mail.imap.host}")
    private String host;
    @Value("${mail.imap.username}")
    private String username;
    @Value("${mail.imap.password}")
    private String password;

    /**
     * Récupère les mails depuis Gmail et les convertit en objets Mail
     */
    public List<Mail> fetchAllMails() {
        List<Mail> mails = new ArrayList<>();
        try {
            Store store = connect();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // On prend les 10 derniers mails pour l'exemple (évite de charger 5000 mails)
            int count = inbox.getMessageCount();
            int start = Math.max(1, count - 10);
            Message[] messages = inbox.getMessages(start, count);

            for (Message msg : messages) {
                String messageId = getMessageId(msg);
                String subject = msg.getSubject() != null ? msg.getSubject() : "(Sans sujet)";
                String from = msg.getFrom()[0].toString();
                String date = msg.getSentDate() != null ? msg.getSentDate().toString() : "";
                String content = getTextFromMessage(msg);

                // On crée l'objet propre. Par défaut l'action est PENDING.
                mails.add(new Mail(messageId, date, subject, from, content));
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mails;
    }

    /**
     * Applique un label (Tag) sur un mail dans Gmail (Pour la synchro)
     */
    public void applyLabelToMail(String messageId, String labelName) {
        try {
            Store store = connect();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Recherche du mail par son ID
            Message[] messages = inbox.search(new MessageIDTerm(messageId));
            if (messages.length > 0) {
                // Création/Récupération du dossier Label (ex: "DO", "DELEGATE")
                Folder labelFolder = store.getFolder(labelName);
                if (!labelFolder.exists()) labelFolder.create(Folder.HOLDS_MESSAGES);

                // Copie du message vers le label
                inbox.copyMessages(messages, labelFolder);
                System.out.println("Label " + labelName + " appliqué sur " + messageId);
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            System.err.println("Erreur sync Gmail: " + e.getMessage());
        }
    }

    // --- Méthodes Utilitaires Privées ---

    private Store connect() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.ssl.enable", "true");
        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(host, username, password);
        return store;
    }

    private String getMessageId(Message msg) {
        try {
            return ((com.sun.mail.imap.IMAPMessage) msg).getMessageID();
        } catch (Exception e) {
            return "";
        }
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                return result.append(bodyPart.getContent()).toString();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }
}