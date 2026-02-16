package projet.devops.Mail;

import projet.devops.Mail.Classifier.EisenhowerAction;

public class Mail {
    private String messageId;
    private String date;
    private String subject;
    private String from;
    private String content;
    private EisenhowerAction action;
    private String status = "SUIVI"; // Par défaut

    public Mail(String messageId, String date, String subject, String from, String content) {
        this.messageId = messageId;
        this.date = date;
        this.subject = subject;
        this.from = from;
        this.content = content;
        this.action = EisenhowerAction.PENDING;
    }

    // Getters et Setters existants...
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessageId() { return messageId; }
    public String getDate() { return date; }
    public String getSubject() { return subject; }
    public String getFrom() { return from; }
    public String getContent() { return content; }
    public EisenhowerAction getAction() { return action; }
    public void setAction(EisenhowerAction action) { this.action = action; }
    public void setAction(String actionStr) { 
        try { this.action = EisenhowerAction.valueOf(actionStr.toUpperCase()); } 
        catch (Exception e) { this.action = EisenhowerAction.PENDING; }
    }
}