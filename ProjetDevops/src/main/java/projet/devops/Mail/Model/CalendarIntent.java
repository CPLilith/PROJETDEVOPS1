package projet.devops.Mail.Model;

public class CalendarIntent {
    public enum Strategy { SIMPLE_PLAN, BOOMERANG, TOTAL_DELEGATION, UNKNOWN }

    private Strategy strategy;
    private String title;
    private String deadline;      // La date d'échéance finale
    private String followUpDate;  // La date de travail ou de rappel (Patate chaude)
    private String assignee;      // À qui on délègue
    private boolean dateDetected; // Pour bloquer l'UI si aucune date n'est trouvée

    public CalendarIntent() {}

    // Getters et Setters
    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    
    public String getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }
    
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    
    public boolean isDateDetected() { return dateDetected; }
    public void setDateDetected(boolean dateDetected) { this.dateDetected = dateDetected; }
}