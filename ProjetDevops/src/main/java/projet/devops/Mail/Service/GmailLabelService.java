package projet.devops.Mail.Service;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.MessageIDTerm;

/**
 * Service pour appliquer les LABELS Gmail natifs (pas les flags IMAP)
 * Les labels doivent exister dans Gmail !
 */
@Service
public class GmailLabelService {

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    /**
     * Applique un label Gmail à un message
     * Le label DOIT déjà exister dans Gmail !
     * 
     * @param messageId L'ID du message
     * @param labelName Le nom exact du label Gmail (ex: "URGENT_IMPORTANT")
     * @return true si succès
     */
    public boolean applyGmailLabel(String messageId, String labelName) {
        Store store = null;
        Folder inbox = null;
        Folder labelFolder = null;

        try {
            store = connect();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Chercher le message
            Message[] messages = inbox.search(new MessageIDTerm(messageId));

            if (messages.length > 0) {
                Message message = messages[0];
                
                // Accéder au label Gmail
                try {
                    labelFolder = store.getFolder(labelName);
                    
                    if (!labelFolder.exists()) {
                        // j'aime trop les emojis xD
                        // ça rend plus visible les erreurs dans les logs
                        System.err.println("❌ Le label '" + labelName + "' n'existe pas dans Gmail !");
                        System.err.println("👉 Crée-le d'abord dans Gmail web, puis réessaye.");
                        return false;
                    }
                    
                    // Copier le message dans le dossier du label (= appliquer le label)
                    inbox.copyMessages(new Message[]{message}, labelFolder);
                    
                    System.out.println("✅ Label Gmail '" + labelName + "' appliqué au message");
                    return true;
                    
                } catch (MessagingException e) {
                    System.err.println("❌ Erreur avec le label '" + labelName + "': " + e.getMessage());
                    System.err.println("👉 Vérifie que le label existe dans Gmail !");
                    return false;
                }
            } else {
                System.err.println("❌ Message non trouvé: " + messageId);
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return false;
        } finally {
            close(labelFolder, null);
            close(inbox, store);
        }
    }

    /**
     * Retire un label Gmail d'un message
     */
    public boolean removeGmailLabel(String messageId, String labelName) {
        Store store = null;
        Folder labelFolder = null;

        try {
            store = connect();
            labelFolder = store.getFolder(labelName);
            
            if (!labelFolder.exists()) {
                System.err.println("❌ Le label '" + labelName + "' n'existe pas");
                return false;
            }
            
            labelFolder.open(Folder.READ_WRITE);
            
            // Chercher le message dans le dossier du label
            Message[] messages = labelFolder.search(new MessageIDTerm(messageId));
            
            if (messages.length > 0) {
                // Marquer pour suppression (= retirer le label)
                messages[0].setFlag(Flags.Flag.DELETED, true);
                labelFolder.expunge(); // Appliquer les suppressions
                
                System.out.println("✅ Label '" + labelName + "' retiré");
                return true;
            }
            
            return false;

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return false;
        } finally {
            close(labelFolder, store);
        }
    }

    /**
     * Liste tous les labels Gmail disponibles
     */
    public String[] listGmailLabels() {
        Store store = null;
        
        try {
            store = connect();
            Folder[] folders = store.getDefaultFolder().list("*");
            
            java.util.List<String> labels = new java.util.ArrayList<>();
            
            for (Folder folder : folders) {
                String name = folder.getName();
                // Filtrer les dossiers système
                if (!name.equals("INBOX") && 
                    !name.equals("[Gmail]") && 
                    !name.startsWith("[Gmail]/")) {
                    labels.add(name);
                }
            }
            
            System.out.println("📋 Labels Gmail trouvés: " + labels);
            return labels.toArray(String[]::new);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return new String[0];
        } finally {
            close(null, store);
        }
    }

    private Store connect() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.ssl.enable", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(host, username, password);
        
        return store;
    }

    private void close(Folder folder, Store store) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
        }
    }
}