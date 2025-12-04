package projet.devops.Mail;

import projet.devops.Mail.Classifier.MailProcessor.EisenhowerTag;

// faudrais faire une interface Maile ou une enuleratuion afin de cacher Mail et le sécuriser. 
// ou sinon IMail et on imprlmente dans MailTraitée et MailBrute. 

public class Mail {
        private String date;
        private String subject;
        private String from;
        private String content;
        private EisenhowerTag tag;

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
