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
import jakarta.mail.Flags;
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
            }
            inbox.close(false);
        }

        return list;
    }

    /**
     * Récupère les labels Gmail via X-GM-LABELS (header Gmail IMAP)
     */
    private List<String> getGmailLabels(Message message) {
        List<String> labels = new ArrayList<>();
        try {
            String[] xGmLabels = message.getHeader("X-GM-LABELS");
            if (xGmLabels != null && xGmLabels.length > 0) {
                // Les labels sont séparés par des espaces
                String labelsStr = xGmLabels[0];
                if (labelsStr != null && !labelsStr.isEmpty()) {
                    // Nettoyer et séparer les labels
                    String[] labelArray = labelsStr.replace("\"", "").split("\\s+");
                    labels.addAll(Arrays.asList(labelArray));
                }
            }
        } catch (MessagingException e) {
            System.err.println("Erreur lors de la récupération des labels: " + e.getMessage());
        }
        return labels;
    }

    /**
     * Vérifie si un message a le label "non_urgent_non_important"
     */
    private boolean hasNonUrgentNonImportantLabel(Message message) {
        List<String> labels = getGmailLabels(message);
        
        for (String label : labels) {
            // Vérifier différentes variantes du label
            String normalizedLabel = label.toLowerCase().replace(" ", "_").replace("-", "_");
            if (normalizedLabel.contains("non_urgent_non_important") ||
                normalizedLabel.contains("nonurgent_nonimportant") ||
                normalizedLabel.equals("non_urgent_non_important")) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Supprime les emails ayant le label "non_urgent_non_important"
     * @return nombre d'emails supprimés
     */
    public int deleteNonUrgentNonImportantMails() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        int deletedCount = 0;
        
        try (Store store = session.getStore("imaps")) {
            store.connect(host, username, password);
            
            // Ouvrir le dossier avec le label Gmail
            Folder labelFolder = store.getFolder("[Gmail]/non_urgent_non_important");
            
            // Si le dossier avec label existe, l'utiliser
            if (labelFolder.exists()) {
                labelFolder.open(Folder.READ_WRITE);
                Message[] messages = labelFolder.getMessages();
                System.out.println("Trouvé " + messages.length + " messages avec le label non_urgent_non_important");
                
                for (Message message : messages) {
                    try {
                        String subject = message.getSubject() != null ? message.getSubject() : "(sans sujet)";
                        message.setFlag(Flags.Flag.DELETED, true);
                        deletedCount++;
                        System.out.println("✓ Email marqué pour suppression: " + subject);
                    } catch (Exception e) {
                        System.err.println("✗ Erreur: " + e.getMessage());
                    }
                }
                labelFolder.close(true);
            } else {
                // Sinon, parcourir INBOX et vérifier les headers
                System.out.println("Dossier label non trouvé, recherche dans INBOX...");
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);
                
                Message[] messages = inbox.getMessages();
                System.out.println("Analyse de " + messages.length + " messages dans INBOX...");
                
                for (Message message : messages) {
                    try {
                        if (hasNonUrgentNonImportantLabel(message)) {
                            String subject = message.getSubject() != null ? message.getSubject() : "(sans sujet)";
                            message.setFlag(Flags.Flag.DELETED, true);
                            deletedCount++;
                            System.out.println("✓ Email marqué pour suppression: " + subject);
                        }
                    } catch (Exception e) {
                        System.err.println("✗ Erreur: " + e.getMessage());
                    }
                }
                inbox.close(true);
            }
            
            System.out.println("=================================");
            System.out.println("Total d'emails supprimés: " + deletedCount);
            System.out.println("=================================");
        }
        
        return deletedCount;
    }

    /**
     * Prévisualise les emails qui seraient supprimés
     */
    public List<Map<String, String>> previewEmailsToDelete() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        List<Map<String, String>> emailsToDelete = new ArrayList<>();
        
        try (Store store = session.getStore("imaps")) {
            store.connect(host, username, password);
            
            // Essayer le dossier avec label
            Folder labelFolder = store.getFolder("[Gmail]/non_urgent_non_important");
            
            if (labelFolder.exists()) {
                labelFolder.open(Folder.READ_ONLY);
                Message[] messages = labelFolder.getMessages();
                
                for (Message message : messages) {
                    try {
                        emailsToDelete.add(createEmailMap(message));
                    } catch (Exception e) {
                        System.err.println("Erreur: " + e.getMessage());
                    }
                }
                labelFolder.close(false);
            } else {
                // Sinon parcourir INBOX
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                Message[] messages = inbox.getMessages();
                
                for (Message message : messages) {
                    try {
                        if (hasNonUrgentNonImportantLabel(message)) {
                            emailsToDelete.add(createEmailMap(message));
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur: " + e.getMessage());
                    }
                }
                inbox.close(false);
            }
        }
        
        return emailsToDelete;
    }

    private Map<String, String> createEmailMap(Message message) throws MessagingException {
        String from = Arrays.toString(message.getFrom());
        String subject = message.getSubject() != null ? message.getSubject() : "";
        String date = message.getSentDate().toString();
        List<String> labels = getGmailLabels(message);
        
        Map<String, String> emailData = new HashMap<>();
        emailData.put("from", from);
        emailData.put("subject", subject);
        emailData.put("date", date);
        emailData.put("labels", labels.toString());
        
        return emailData;
    }

    /**
     * Récupère tous les emails avec leurs labels Gmail
     */
    public List<Map<String, String>> getAllMailsWithLabels() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        List<Map<String, String>> list = new ArrayList<>();
        
        try (Store store = session.getStore("imaps")) {
            store.connect(host, username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            
            int messageCount = inbox.getMessageCount();
            int start = Math.max(1, messageCount - 19); // Les 20 derniers
            int end = messageCount;
            
            if (messageCount > 0) {
                Message[] messages = inbox.getMessages(start, end);
                
                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    list.add(createEmailMap(message));
                }
            }
            
            inbox.close(false);
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