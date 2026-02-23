package projet.devops.Mail.Classifier;

import projet.devops.Mail.Mail;

public class TextCleaner {

    public static String cleanEmailText(String text, int maxLength) {
        // Vérifications de base
        if (text == null) {
            return "";
        }

        // Nettoyer
        String cleaned = text
                .replaceAll("\\r\\n|\\n|\\r", " ") // Tous les sauts de ligne
                .replaceAll("\"", "'")
                .replaceAll("\\s+", " ") // Espaces multiples -> un seul
                .trim();

        // Si vide après nettoyage
        if (cleaned.isEmpty()) {
            return "";
        }

        // CORRECTION PRINCIPALE : utiliser correctement Math.min
        // et s'assurer qu'on ne fait pas substring(0, 0)
        int textLength = cleaned.length();
        int endIndex = Math.min(maxLength, textLength);

        // Éviter endIndex = 0 (provoquerait substring(0, 0))
        if (endIndex == 0) {
            return "";
        }

        // Maintenant c'est safe
        return cleaned.substring(0, endIndex);
    }

    public static String prepareEmailForAnalysis(Mail mail) {
        if (mail == null) {
            return "";
        }

        String from = safeString(mail.getFrom());
        String subject = safeString(mail.getSubject());
        String content = safeString(mail.getContent());

        // Combiner
        String combined = String.join(" ", from, subject, content).toLowerCase();

        // Nettoyer et limiter
        return cleanEmailText(combined, 500);
    }

    private static String safeString(String str) {
        return str != null ? str : "";
    }
}