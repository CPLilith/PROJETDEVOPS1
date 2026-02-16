package projet.devops.Mail;

import projet.devops.Mail.Classifier.EisenhowerAction;

public class Mail {
    private String messageId;
    private String date;
    private String subject;
    private String from;
    private String content;
    private EisenhowerAction action;
    private String status = "SUIVI"; // Nouveau champ pour le Kanban

    public Mail(String messageId, String date, String subject, String from, String content) {
        this.messageId = messageId;
        this.date = date;
        this.subject = subject;
        this.from = from;
        this.content = content;
        this.action = EisenhowerAction.PENDING;
    }

    // --- NOUVEAUX GETTERS/SETTERS POUR LE STATUT ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // --- GETTERS ET SETTERS EXISTANTS ---
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
            this.action = EisenhowerAction.valueOf(actionStr.toUpperCase()); 
        } catch (Exception e) {
            this.action = EisenhowerAction.PENDING;
        }
    }

    public void setAction(EisenhowerAction action) { 
        this.action = action; 
    }
}