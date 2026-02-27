package projet.devops.Mail.Service;

import projet.devops.Mail.Model.Mail;

public class TextCleaner {

    public static String cleanEmailText(String text, int maxLength) {
        if (text == null) return "";

        String cleaned = text
                .replaceAll("\\r\\n|\\n|\\r", " ")        // Sauts de ligne
                .replaceAll("\"", "'")
                .replaceAll("https?://\\S+", "")           // Supprime les URLs http/https
                .replaceAll("www\\.\\S+", "")              // Supprime les URLs www
                .replaceAll("<[^>]*>", "")                 // Supprime les balises HTML
                .replaceAll("\\s+", " ")                   // Espaces multiples -> un seul
                .trim();

        if (cleaned.isEmpty()) return "";

        int endIndex = Math.min(maxLength, cleaned.length());
        if (endIndex == 0) return "";

        return cleaned.substring(0, endIndex);
    }

    public static String prepareEmailForAnalysis(Mail mail) {
        if (mail == null) return "";

        String from = safeString(mail.getFrom());
        String subject = safeString(mail.getSubject());
        String content = safeString(mail.getContent());

        String combined = String.join(" ", from, subject, content).toLowerCase();
        return cleanEmailText(combined, 500);
    }

    private static String safeString(String str) {
        return str != null ? str : "";
    }
}