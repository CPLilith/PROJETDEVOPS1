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
 * Service dédié à la gestion des tags/labels sur les emails Gmail
 */
@Service
public class TagService {

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.username}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    /**
     * Ajoute un tag à un email via son Message-ID
     * @param messageId L'ID unique du message (ex: "<CABc123@mail.gmail.com>")
     * @param tag Le tag à ajouter (ex: "URGENT", "NON_URGENT_NON_IMPORTANT")
     * @return true si succès, false sinon
     */
    public boolean addTag(String messageId, String tag) {
        return modifyTag(messageId, tag, true);
    }

    /**
     * Retire un tag d'un email
     * @param messageId L'ID unique du message
     * @param tag Le tag à retirer
     * @return true si succès, false sinon
     */
    public boolean removeTag(String messageId, String tag) {
        return modifyTag(messageId, tag, false);
    }

    /**
     * Récupère les tags d'un email
     * @param messageId L'ID unique du message
     * @return Tableau des tags, ou null si erreur
     */
    public String[] getTags(String messageId) {
        Store store = null;
        Folder inbox = null;

        try {
            store = connect();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.search(new MessageIDTerm(messageId));

            if (messages.length > 0) {
                Message message = messages[0];
                Flags flags = message.getFlags();
                return flags.getUserFlags();
            }
            
            return null;

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des tags: " + e.getMessage());
            return null;
        } finally {
            close(inbox, store);
        }
    }

    /**
     * Remplace tous les tags d'un email par un nouveau tag
     * @param messageId L'ID unique du message
     * @param newTag Le nouveau tag
     * @return true si succès, false sinon
     */
    public boolean replaceTag(String messageId, String newTag) {
        String[] currentTags = getTags(messageId);
        
        if (currentTags != null) {
            for (String tag : currentTags) {
                removeTag(messageId, tag);
            }
        }
        
        return addTag(messageId, newTag);
    }

    /**
     * Méthode privée pour ajouter ou retirer un tag
     */
    private boolean modifyTag(String messageId, String tag, boolean add) {
        if (messageId == null || tag == null || tag.isEmpty()) {
            return false;
        }

        Store store = null;
        Folder inbox = null;

        try {
            store = connect();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new MessageIDTerm(messageId));

            if (messages.length > 0) {
                Message message = messages[0];
                String cleanTag = tag.replaceAll("[^a-zA-Z0-9_-]", "");
                
                if (!cleanTag.isEmpty()) {
                    Flags flags = new Flags(cleanTag);
                    message.setFlags(flags, add);
                    
                    System.out.println("✓ Tag '" + cleanTag + "' " + 
                        (add ? "ajouté au" : "retiré du") + " message: " + messageId);
                    return true;
                }
            } else {
                System.out.println("✗ Message non trouvé: " + messageId);
            }
            
            return false;

        } catch (Exception e) {
            System.err.println("Erreur lors de la modification du tag: " + e.getMessage());
            return false;
        } finally {
            close(inbox, store);
        }
    }

    /**
     * Connexion au serveur IMAP
     */
    private Store connect() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.ssl.enable", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(host, username, password);
        
        return store;
    }

    /**
     * Ferme proprement les connexions IMAP
     */
    private void close(Folder inbox, Store store) {
        try {
            if (inbox != null && inbox.isOpen()) {
                inbox.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
        }
    }
}