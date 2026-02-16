package projet.devops.Mail;

import projet.devops.Mail.Classifier.EisenhowerAction;

public class Mail {
    private String messageId;
    private String date;
    private String subject;
    private String from;
    private String content;
    private EisenhowerAction action;

    public Mail(String messageId, String date, String subject, String from, String content) {
        this.messageId = messageId;
        this.date = date;
        this.subject = subject;
        this.from = from;
        this.content = content;
        this.action = EisenhowerAction.PENDING;
    }

    // --- GETTERS ET SETTERS OBLIGATOIRES ---

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public EisenhowerAction getAction() { return action; }

    public void setAction(String actionStr) { 
        try {
            // On convertit le texte "DO" en l'objet Enum EisenhowerAction.DO
            this.action = EisenhowerAction.valueOf(actionStr.toUpperCase()); 
        } catch (IllegalArgumentException e) {
            // En cas de texte invalide, on remet en PENDING par sécurité
            this.action = EisenhowerAction.PENDING;
        }
    }

    // Garde aussi la version qui accepte l'Enum (pour l'IA)
    public void setAction(EisenhowerAction action) { 
        this.action = action; 
    }
    
}