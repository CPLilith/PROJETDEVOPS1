package projet.devops.Mail.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@Service
public class MailService {

    @Value("${mail.imap.host}")
    private String host;
    // Pk port on l'utilise pas ??
    @Value("${mail.imap.port}")
    private int port;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    /**
     * Récupère les mails depuis IMAP SANS tags
     * Les tags seront ajoutés par FichierTempTraiter
     */
    public List<Map<String, String>> getAllMails() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getInstance(props);
        List<Map<String, String>> list;
        try (Store store = session.getStore("imaps")) {
            store.connect(host, username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            int messageCount = inbox.getMessageCount();
            list = new ArrayList<>();
            int start = Math.max(1, messageCount - 9); // Les 10 derniers
            int end = messageCount;
            if (messageCount > 0) {
                Message[] messages = inbox.getMessages(start, end);
                
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    
                    String from = Arrays.toString(message.getFrom());
                    String subject = message.getSubject() != null ? message.getSubject() : "";
                    String content = getTextFromMessage(message);
                    String date = message.getSentDate().toString();
                    
                    // Récupérer le messageId (important pour le tagging)
                    String messageId = null;
                    try {
                        messageId = ((com.sun.mail.imap.IMAPMessage) message).getMessageID();
                    } catch (MessagingException e) {
                        System.err.println("Impossible de récupérer le messageId: " + e.getMessage());
                    }
                    
                    // Créer la map SANS tag
                    Map<String, String> info = new HashMap<>();
                    info.put("from", from);
                    info.put("subject", subject);
                    info.put("date", date);
                    info.put("content", content);
                    info.put("messageId", messageId);
                    
                    list.add(info);
                }
            }   inbox.close(false);
        }

        return list;
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append(html);
            } else if (bodyPart.getContent() instanceof MimeMultipart mimeMultipart1) {
                result.append(getTextFromMimeMultipart(mimeMultipart1));
            }
        }
        return result.toString();
    }
}