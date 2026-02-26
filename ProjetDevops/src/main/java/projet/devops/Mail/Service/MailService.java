package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.MessageIDTerm;
import projet.devops.Mail.Mail;

@Service
public class MailService {

    // --- PRINCIPE DRY : Fini les chaînes en dur éparpillées partout ! ---
    private static final String FOLDER_INBOX = "INBOX";
    private static final String FOLDER_TRASH_FR = "[Gmail]/Corbeille";
    private static final String FOLDER_TRASH_EN = "[Gmail]/Trash";
    private static final String FOLDER_DRAFTS_FR = "[Gmail]/Brouillons";
    private static final String FOLDER_DRAFTS_EN = "Drafts";
    private static final String PROTOCOL_IMAP = "gimap";
    private static final String ACTION_DELETE = "DELETE";

    @Value("${mail.imap.host}")
    private String host;
    @Value("${mail.imap.username}")
    private String username;
    @Value("${mail.imap.password}")
    private String password;

    private final MailMapper mailMapper; // Injection de notre nouvel assistant !

    public MailService(MailMapper mailMapper) {
        this.mailMapper = mailMapper;
    }

    public List<Mail> fetchAllMails() {
        List<Mail> mailList = new ArrayList<>();
        try (Store store = connect()) {
            Folder inbox = store.getFolder(FOLDER_INBOX);
            inbox.open(Folder.READ_ONLY);

            int totalMessages = inbox.getMessageCount();
            int limite = 30;
            int start = Math.max(1, totalMessages - limite + 1);

            Message[] messages = inbox.getMessages(start, totalMessages);

            for (int i = messages.length - 1; i >= 0; i--) {
                // SRP : C'est le mapper qui se charge de décoder le message technique !
                mailList.add(mailMapper.toDomainMail(messages[i]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mailList;
    }

    public void applyLabelToMail(String messageId, String labelName) {
        try (Store store = connect()) {
            Folder inbox = store.getFolder(FOLDER_INBOX);
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new MessageIDTerm(messageId));
            if (messages.length > 0) {
                if (ACTION_DELETE.equalsIgnoreCase(labelName)) {
                    Folder trash = store.getFolder(FOLDER_TRASH_FR);
                    if (!trash.exists())
                        trash = store.getFolder(FOLDER_TRASH_EN);
                    if (trash.exists())
                        inbox.copyMessages(messages, trash);

                    for (Message msg : messages) {
                        msg.setFlag(Flags.Flag.DELETED, true);
                    }
                } else {
                    Folder labelFolder = store.getFolder(labelName);
                    if (!labelFolder.exists())
                        labelFolder.create(Folder.HOLDS_MESSAGES);
                    inbox.copyMessages(messages, labelFolder);
                }
            }
            // true : On valide la suppression définitive de l'inbox
            inbox.close(true);
        } catch (Exception e) {
            System.err.println("❌ Erreur applyLabel : " + e.getMessage());
        }
    }

    public void createDraft(String toEmail, String subject, String body) throws Exception {
        try (Store store = connect()) {
            Folder draftsFolder = store.getFolder(FOLDER_DRAFTS_FR);
            if (!draftsFolder.exists()) {
                draftsFolder = store.getFolder(FOLDER_DRAFTS_EN);
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
        props.put("mail.store.protocol", PROTOCOL_IMAP);
        Session session = Session.getInstance(props);
        Store store = session.getStore(PROTOCOL_IMAP);
        store.connect(host, username, password);
        return store;
    }
}