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
    private String startTime; // Heure de début (ex: "10:00")
    private int durationMinutes; // Durée de la tâche en minutes (ex: 60)
    private boolean allDay;

    public CalendarIntent() {
        this.durationMinutes = 60;
    }

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

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public boolean isAllDay() { return allDay; }
    public void setAllDay(boolean allDay) { this.allDay = allDay; }
}