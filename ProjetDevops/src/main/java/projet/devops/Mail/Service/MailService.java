package projet.devops.Mail.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
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

    public List<Mail> fetchAllMails() {
        List<Mail> mailList = new ArrayList<>();
        Store store = null;
        Folder inbox = null;
        try {
            store = connect();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int totalMessages = inbox.getMessageCount();
            int limite = 30;
            int start = Math.max(1, totalMessages - limite + 1);

            Message[] messages = inbox.getMessages(start, totalMessages);

            for (int i = messages.length - 1; i >= 0; i--) {
                Message msg = messages[i];
                mailList.add(new Mail(
                        getMessageId(msg),
                        msg.getSentDate() != null ? msg.getSentDate().toString() : "Date inconnue",
                        msg.getSubject(),
                        msg.getFrom()[0].toString(),
                        getTextFromMessage(msg)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inbox != null)
                    inbox.close(false);
                if (store != null)
                    store.close();
            } catch (Exception e) {
            }
        }
        return mailList;
    }

    // --- ENVOI RÉEL (SMTP) ---
    public void sendEmail(String toEmail, String subject, String body) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
        System.out.println("✈️ Mail envoyé à : " + toEmail);
    }

    // --- SYNCHRONISATION DES LABELS ---
    public void applyLabelToMail(String messageId, String labelName) {
        try (Store store = connect()) {
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new MessageIDTerm(messageId));
            if (messages.length > 0) {
                Folder labelFolder = store.getFolder(labelName);
                if (!labelFolder.exists())
                    labelFolder.create(Folder.HOLDS_MESSAGES);
                inbox.copyMessages(messages, labelFolder);
                System.out.println("✅ Label [" + labelName + "] appliqué sur Gmail.");
            }
            inbox.close(false);
        } catch (Exception e) {
            System.err.println("❌ Erreur applyLabel : " + e.getMessage());
        }
    }

    public void createDraft(String toEmail, String subject, String body) throws Exception {
        try (Store store = connect()) {
            Folder draftsFolder = store.getFolder("[Gmail]/Brouillons");
            if (!draftsFolder.exists()) {
                draftsFolder = store.getFolder("Drafts");
                if (!draftsFolder.exists())
                    draftsFolder.create(Folder.HOLDS_MESSAGES);
            }
            draftsFolder.open(Folder.READ_WRITE);

            Message message = new MimeMessage(Session.getInstance(new Properties()));
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            message.setFlag(Flags.Flag.DRAFT, true);

            draftsFolder.appendMessages(new Message[] { message });
        }
    }

    private Store connect() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(host, username, password);
        return store;
    }

    private String getMessageId(Message msg) {
        try {
            return (msg instanceof MimeMessage) ? ((MimeMessage) msg).getMessageID() : "id-" + msg.getMessageNumber();
        } catch (Exception e) {
            return "";
        }
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain"))
            return message.getContent().toString();
        if (message.isMimeType("multipart/*"))
            return getTextFromMimeMultipart((MimeMultipart) message.getContent());
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain"))
                return bodyPart.getContent().toString();
            else if (bodyPart.getContent() instanceof MimeMultipart)
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
        }
        return result.toString();
    }
}