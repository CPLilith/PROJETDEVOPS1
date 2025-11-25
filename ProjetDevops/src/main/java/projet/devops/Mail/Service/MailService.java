package projet.devops.Mail.Service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    public List<Map<String, String>> getAllMails() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect(host, username, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        List<Map<String, String>> list = new ArrayList<>();

        for (Message message : messages) {
            Map<String, String> info = new HashMap<>();
            info.put("from", Arrays.toString(message.getFrom()));
            info.put("subject", message.getSubject());
            info.put("date", message.getSentDate().toString());
            list.add(info);
        }

        inbox.close(false);
        store.close();

        return list;
    }
}