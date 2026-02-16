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
import projet.devops.Mail.Classifier.EisenhowerAction;
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
     * Récupère les mails (Version stable pour compilation)
     */
    public List<Mail> fetchAllMails() {
        List<Mail> mails = new ArrayList<>();
        try {
            Store store = connect();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int count = inbox.getMessageCount();
            int start = Math.max(1, count - 10);
            Message[] messages = inbox.getMessages(start, count);

            for (Message msg : messages) {
                // Utilisation des méthodes utilitaires sécurisées
                String messageId = getMessageId(msg);
                String subject = msg.getSubject() != null ? msg.getSubject() : "(Sans sujet)";
                String from = msg.getFrom()[0].toString();
                String date = msg.getSentDate() != null ? msg.getSentDate().toString() : "";
                String content = getTextFromMessage(msg);

                Mail mail = new Mail(messageId, date, subject, from, content);
                
                // Pour éviter l'erreur de compilation sur les Labels Gmail complexes,
                // on initialise par défaut en PENDING.
                // La récupération des labels Gmail spécifiques nécessite une config Maven plus avancée.
                mail.setAction(EisenhowerAction.PENDING);

                mails.add(mail);
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mails;
    }

    /**
     * Version simplifiée pour éviter l'erreur "incompatible types"
     */
    public List<String> getLabelsForMessage(String messageId) {
        // Retourne une liste vide pour permettre la compilation immédiate.
        // L'implémentation complète des extensions Gmail IMAP (X-GM-LABELS)
        // est désactivée temporairement pour résoudre le BUILD FAILURE.
        return new ArrayList<>();
    }

    /**
     * Applique un label (Tag) sur un mail dans Gmail
     */
    public void applyLabelToMail(String messageId, String labelName) {
        try {
            Store store = connect();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new MessageIDTerm(messageId));
            if (messages.length > 0) {
                Folder labelFolder = store.getFolder(labelName);
                if (!labelFolder.exists()) labelFolder.create(Folder.HOLDS_MESSAGES);
                inbox.copyMessages(messages, labelFolder);
                System.out.println("✅ Sync Gmail : Label " + labelName + " appliqué.");
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            System.err.println("❌ Erreur sync Gmail: " + e.getMessage());
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
            // Tentative sécurisée de récupération d'ID
            if (msg instanceof jakarta.mail.internet.MimeMessage) {
                 return ((jakarta.mail.internet.MimeMessage) msg).getMessageID();
            }
            return "";
        } catch (Exception e) { return ""; }
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) return message.getContent().toString();
        if (message.isMimeType("multipart/*")) return getTextFromMimeMultipart((MimeMultipart) message.getContent());
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) return bodyPart.getContent().toString();
            else if (bodyPart.getContent() instanceof MimeMultipart) 
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
        }
        return result.toString();
    }
}