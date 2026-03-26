package projet.devops.Mail.Model;

public class CalendarIntent {
    private String title;
    private String strategy;
    private String assignee;
    private String deadline;
    private boolean dateDetected;
    private String confidence;
    private String followUpDate;
    private String fullMailContent; // NOUVEAU : Pour stocker le corps du mail original

    public CalendarIntent() {}

    // Getters et Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public boolean isDateDetected() { return dateDetected; }
    public void setDateDetected(boolean dateDetected) { this.dateDetected = dateDetected; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }

    public String getFullMailContent() { return fullMailContent; }
    public void setFullMailContent(String fullMailContent) { this.fullMailContent = fullMailContent; }
}