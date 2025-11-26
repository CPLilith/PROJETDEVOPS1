package projet.devops.Mail.Classifier;

import java.util.List;

public class MailProcessor {

    public enum EisenhowerTag {
        Q1, Q2, Q3, Q4
    }

    public static class Mail {

        private String date;
        private String subject;
        private String from;
        private String content;
        private EisenhowerTag tag;

        // ⚠️ Nécessaire pour Jackson
        public Mail() {}

        public Mail(String date, String subject, String from, String content, EisenhowerTag tag) {
            this.date = date;
            this.subject = subject;
            this.from = from;
            this.content = content;
            this.tag = tag;
        }

        // Getters & Setters obligatoires
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public EisenhowerTag getTag() { return tag; }
        public void setTag(EisenhowerTag tag) { this.tag = tag; }

        // Pour retourner un nouveau mail avec nouveau tag
        public Mail withTag(EisenhowerTag newTag) {
            return new Mail(this.date, this.subject, this.from, this.content, newTag);
        }
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
