package projet.devops.Mail.Service;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import projet.devops.Mail.Model.EisenhowerAction;
import projet.devops.Mail.Model.Mail;

@Component // SRP: Son seul rôle est de transformer un objet technique en objet métier
public class MailMapper {

    public Mail toDomainMail(Message msg) throws Exception {
        Mail mail = new Mail(
                getMessageId(msg),
                msg.getSentDate() != null ? msg.getSentDate().toString() : "Date inconnue",
                msg.getSubject(),
                msg.getFrom() != null && msg.getFrom().length > 0 ? msg.getFrom()[0].toString() : "Inconnu",
                getTextFromMessage(msg));

        // Extraction des labels (étiquettes Gmail)
        try {
            java.lang.reflect.Method getLabelsMethod = msg.getClass().getMethod("getLabels");
            String[] labels = (String[]) getLabelsMethod.invoke(msg);
            if (labels != null) {
                for (String label : labels) {
                    try {
                        String cleanLabel = label.replace("\"", "").replace("\\", "").trim().toUpperCase();
                        mail.setAction(EisenhowerAction.valueOf(cleanLabel));
                        break;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return mail;
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
            if (bodyPart.isMimeType("text/plain")) {
                return bodyPart.getContent().toString();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }
}