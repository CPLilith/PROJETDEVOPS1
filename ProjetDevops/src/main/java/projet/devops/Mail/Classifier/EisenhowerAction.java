package projet.devops.Mail.Classifier;

public enum EisenhowerAction {
    DO, // Urgent + Important
    PLAN, // Important + Pas Urgent
    DELEGATE, // Urgent + Pas Important
    DELETE, // Ni l'un ni l'autre
    PENDING; // Pas encore traité

    public boolean isDo() {
        return this == DO;
    }
}