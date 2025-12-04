package projet.devops.Mail.Classifier;

import java.util.List;

import projet.devops.Mail.Mail;

public class MailProcessor {

    public enum EisenhowerTag {
        Q1, Q2, Q3, Q4
    }

    public static Mail tagMail(Mail mail, List<String> importanceWords, List<String> urgencyWords) {

        String text = (mail.getFrom() + " " + mail.getSubject() + " " + mail.getContent()).toLowerCase();

        boolean importance = importanceWords.stream().anyMatch(text::contains);
        boolean urgency = urgencyWords.stream().anyMatch(text::contains);

        EisenhowerTag tag;
        if (importance && urgency) tag = EisenhowerTag.Q1;
        else if (importance) tag = EisenhowerTag.Q2;
        else if (urgency) tag = EisenhowerTag.Q3;
        else tag = EisenhowerTag.Q4;

        return mail.withTag(tag);
    }

    public static int tagPriority(EisenhowerTag tag) {
        return switch (tag) {
            case Q1 -> 1;
            case Q3 -> 2;
            case Q2 -> 3;
            case Q4 -> 4;
        };
    }
}
