package projet.devops.Mail.Classifier;

public enum EisenhowerTag {
    URGENT_IMPORTANT,              // Q1
    IMPORTANTS,                    // Q2
    URGENT,                        // Q3
    NON_URGENT_NON_IMPORTANT,      // Q4
    A_MODIFIER                     // Pour les mails à vérifier manuellement
}