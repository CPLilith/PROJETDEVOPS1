package projet.devops.Mail.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;

@Service
public class TeamService {

    // --- CONSTANTES DRY ---
    private static final String DEFAULT_EMAIL = "equipe@defaut.com";
    private static final String AI_MODEL = "tinyllama";
    private static final int MAX_CONTENT_LENGTH_AI = 250;
    private static final int MAX_CONTENT_LENGTH_DRAFT = 200;

    // Le Pattern Regex compilé une seule fois pour toute la classe (Optimisation)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");

    private final OllamaClient ollamaClient;

    public TeamService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    // SRP : La méthode principale devient un chef d'orchestre ultra lisible
    public String suggestAssignee(String emailContent, Map<String, String> contactsMap) {
        if (contactsMap == null || contactsMap.isEmpty()) {
            return DEFAULT_EMAIL;
        }

        String lowerContent = emailContent.toLowerCase();

        // Étape 1 : Recherche prioritaire par mots-clés
        String assignee = findAssigneeByKeywords(lowerContent, contactsMap);
        if (assignee != null)
            return assignee;

        // Étape 2 : Appel IA en secours
        assignee = findAssigneeByAI(emailContent, contactsMap);
        if (assignee != null)
            return assignee;

        // Étape 3 : Retour par défaut
        System.out.println("⚠️ Aucune détection précise, retour au premier contact du JSON.");
        return contactsMap.keySet().iterator().next();
    }

    public String generateDelegationDraft(String originalSender, String assignee, String content, String trackingId) {
        String cleanContent = TextCleaner.cleanEmailText(content, MAX_CONTENT_LENGTH_DRAFT);
        return String.format(
                "Bonjour,\n\nPeux-tu traiter la demande de %s qui dit :\n\"%s\"\n\nMerci.\n\n[Ref: %s]",
                originalSender, cleanContent, trackingId);
    }

    // =========================================================
    // SOUS-MÉTHODES PRIVÉES (SRP & DRY)
    // =========================================================

    private String findAssigneeByKeywords(String lowerContent, Map<String, String> contactsMap) {
        for (Map.Entry<String, String> entry : contactsMap.entrySet()) {
            String email = entry.getKey();
            String roleDesc = entry.getValue().toLowerCase();

            // DRY : L'utilisation de notre helper rend la lecture et l'ajout de rôles
            // incroyablement simples
            if (matchesRole(lowerContent, roleDesc, "devops", "docker", "git", "serveur", "infra")) {
                System.out.println("✅ Match Mot-clé : DevOps identifié (" + email + ")");
                return email;
            }
            if (matchesRole(lowerContent, roleDesc, "backend", "ia", "backend", "api", "ollama")) {
                System.out.println("✅ Match Mot-clé : Backend identifié (" + email + ")");
                return email;
            }
            if (matchesRole(lowerContent, roleDesc, "frontend", "css", "interface", "ui", "visuel")) {
                System.out.println("✅ Match Mot-clé : Frontend identifié (" + email + ")");
                return email;
            }
            if (matchesRole(lowerContent, roleDesc, "bdd", "sql", "base de données", "bug", "test")) {
                System.out.println("✅ Match Mot-clé : BDD/QA identifié (" + email + ")");
                return email;
            }
        }
        return null; // Aucun match trouvé
    }

    /**
     * Helper DRY : Vérifie si le rôle correspond ET si au moins un des mots-clés
     * est présent.
     */
    private boolean matchesRole(String content, String actualRole, String targetRole, String... keywords) {
        if (!actualRole.contains(targetRole))
            return false;

        for (String kw : keywords) {
            if (content.contains(kw))
                return true;
        }
        return false;
    }

    private String findAssigneeByAI(String emailContent, Map<String, String> contactsMap) {
        try {
            System.out.println("🧠 Mots-clés non trouvés, consultation de l'IA...");
            String cleanContent = TextCleaner.cleanEmailText(emailContent, MAX_CONTENT_LENGTH_AI);

            StringBuilder contactsList = new StringBuilder();
            contactsMap.forEach((email, role) -> contactsList.append(String.format("- %s : %s\n", email, role)));

            String prompt = String.format("""
                    Tu es un routeur de mails technique. Choisis l'email le plus adapté dans la liste.

                    ÉQUIPE DISPONIBLE:
                    %s
                    MAIL À ANALYSER:
                    "%s"

                    RÈGLE: Réponds UNIQUEMENT avec l'email choisi.
                    EMAIL DU RESPONSABLE:
                    """, contactsList.toString(), cleanContent);

            String response = ollamaClient.generateResponse(AI_MODEL, prompt).trim();
            Matcher matcher = EMAIL_PATTERN.matcher(response);

            if (matcher.find()) {
                String foundEmail = matcher.group();
                if (contactsMap.containsKey(foundEmail)) {
                    System.out.println("🤖 IA Success : " + foundEmail);
                    return foundEmail;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur TeamService (IA) : " + e.getMessage());
        }
        return null;
    }
}