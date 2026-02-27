package projet.devops.Mail.Model;

public class Mail {
    private String messageId;
    private String date;
    private String subject;
    private String from;
    private String content;
    private EisenhowerAction action;
    private String status = "SUIVI";

    /**
     * Tag DO personnalisé créé par l'utilisateur (ex: "DO_FORMATION").
     * Quand ce champ est renseigné, action vaut EisenhowerAction.DO.
     */
    private String customDoTag;

    public Mail(String messageId, String date, String subject, String from, String content) {
        this.messageId = messageId;
        this.date = date;
        this.subject = subject;
        this.from = from;
        this.content = content;
        this.action = EisenhowerAction.PENDING;
    }

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

    /**
     * Setter String : si le tag est inconnu de l'enum (= custom DO),
     * on garde action=DO et on stocke dans customDoTag.
     */
    public void setAction(String actionStr) {
        if (actionStr == null || actionStr.isBlank()) {
            this.action = EisenhowerAction.PENDING;
            return;
        }
        String normalized = actionStr.trim().toUpperCase().replaceAll("\\s+", "_");
        try {
            this.action = EisenhowerAction.valueOf(normalized);
            this.customDoTag = null;
        } catch (IllegalArgumentException e) {
            // Tag custom DO
            this.action = EisenhowerAction.DO;
            this.customDoTag = normalized;
        }
    }

    public void setAction(EisenhowerAction action) {
        this.action = action;
        this.customDoTag = null;
    }

    public String getCustomDoTag() {
        return customDoTag;
    }

    public void setCustomDoTag(String customDoTag) {
        this.customDoTag = customDoTag;
    }

    /**
     * Tag effectif à afficher dans l'UI : custom si présent, sinon nom de l'action.
     */
    public String getEffectiveTag() {
        if (customDoTag != null && !customDoTag.isBlank())
            return customDoTag;
        return action != null ? action.name() : "PENDING";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String status() {
        return status;
    }
}