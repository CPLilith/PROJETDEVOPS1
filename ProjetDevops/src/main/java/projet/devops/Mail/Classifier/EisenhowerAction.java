package projet.devops.Mail.Classifier;

public enum EisenhowerAction {
    DO,         // Urgent + Important (Faire)
    PLAN,       // Important + Pas Urgent (Planifier)
    DELEGATE,   // Urgent + Pas Important (Déléguer - Ta Feature 2)
    DELETE,     // Ni l'un ni l'autre (Supprimer/Archiver)
    PENDING     // Pas encore traité
}