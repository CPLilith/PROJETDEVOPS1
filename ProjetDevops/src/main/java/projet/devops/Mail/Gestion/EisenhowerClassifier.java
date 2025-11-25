package projet.devops.Mail.Gestion;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class EisenhowerClassifier {

    // Mots-clés pour déterminer l'importance
    private static final List<String> IMPORTANT_KEYWORDS = Arrays.asList(
            "utilise peut-être votre compte",
            "sécurisez votre compte",
            "compte",
            "google"
    );

    // Mots-clés pour déterminer l'urgence
    private static final List<String> URGENT_KEYWORDS = Arrays.asList(
            "immédiatement",
            "urgent",
            "alerte",
            "clé d'accès",
            "mot de passe d'appli"
    );

    /**
     * Classifie un mail en fonction de son importance et de son urgence.
     *
     * @param from    L'expéditeur du mail.
     * @param subject Le sujet du mail.
     * @param content Le contenu du mail.
     * @return Un tag Eisenhower correspondant à la classification.
     */
    public EisenhowerTag classifyMail(String from, String subject, String content) {
        // Gérer les cas où les paramètres sont null
        String text = ((from != null ? from : "") + " " +
                       (subject != null ? subject : "") + " " +
                       (content != null ? content : "")).toLowerCase();

        // Vérifier l'importance et l'urgence
        boolean isImportant = containsAny(text, IMPORTANT_KEYWORDS);
        boolean isUrgent = containsAny(text, URGENT_KEYWORDS);

        // Retourner le tag approprié
        if (isImportant && isUrgent) return EisenhowerTag.EISENHOWER_Q1;
        if (isImportant) return EisenhowerTag.EISENHOWER_Q2;
        if (isUrgent) return EisenhowerTag.EISENHOWER_Q3;

        return EisenhowerTag.EISENHOWER_Q4;
    }

    /**
     * Vérifie si un texte contient au moins un mot-clé d'une liste.
     *
     * @param text     Le texte à analyser.
     * @param keywords La liste des mots-clés.
     * @return true si au moins un mot-clé est trouvé, sinon false.
     */
    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}