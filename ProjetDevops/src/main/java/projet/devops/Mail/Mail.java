package projet.devops.Mail;

import projet.devops.Mail.Classifier.EisenhowerAction;

public class Mail {
    private String messageId;
    private String date;
    private String subject;
    private String from;
    private String content;
    private EisenhowerAction action;
    private String status = "SUIVI"; // Initialisé par défaut pour le Kanban

    public Mail(String messageId, String date, String subject, String from, String content) {
        this.messageId = messageId;
        this.date = date;
        this.subject = subject;
        this.from = from;
        this.content = content;
        this.action = EisenhowerAction.PENDING;
    }

    // --- GETTERS & SETTERS ---

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public EisenhowerAction getAction() {
        return action;
    }

    // Setter intelligent qui accepte une String (utile pour l'IA et IMAP)
    public void setAction(String actionStr) {
        try {
            if (actionStr != null) {
                this.action = EisenhowerAction.valueOf(actionStr.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            this.action = EisenhowerAction.PENDING;
        }
    }

    // Setter standard pour l'Enum
    public void setAction(EisenhowerAction action) {
        this.action = action;
    }

    // --- GESTION DU STATUT (KANBAN) ---

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    // Pour l'affichage dans le template (compatibilité)
    public String status() {
        return status;
    }
}