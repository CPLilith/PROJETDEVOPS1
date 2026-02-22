package projet.devops.Mail.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.TextCleaner;

@Service
public class TeamService {

    private final OllamaClient ollamaClient;

    public TeamService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public String suggestAssignee(String emailContent, Map<String, String> contactsMap) {
        if (contactsMap == null || contactsMap.isEmpty()) return "equipe@defaut.com";

        String lowerContent = emailContent.toLowerCase();

        // --- ÉTAPE 1 : RECHERCHE PRIORITAIRE PAR MOTS-CLÉS (SÉCURITÉ) ---
        // On scanne le mail pour voir s'il contient des mots techniques correspondant aux rôles
        for (Map.Entry<String, String> entry : contactsMap.entrySet()) {
            String email = entry.getKey();
            String roleDescription = entry.getValue().toLowerCase();

            // Match DevOps
            if ((lowerContent.contains("docker") || lowerContent.contains("git") || lowerContent.contains("serveur") || lowerContent.contains("infra")) 
                && roleDescription.contains("devops")) {
                System.out.println("✅ Match Mot-clé : DevOps identifié (" + email + ")");
                return email;
            }
            // Match Backend / IA
            if ((lowerContent.contains("ia") || lowerContent.contains("backend") || lowerContent.contains("api") || lowerContent.contains("ollama")) 
                && roleDescription.contains("backend")) {
                System.out.println("✅ Match Mot-clé : Backend identifié (" + email + ")");
                return email;
            }
            // Match Frontend
            if ((lowerContent.contains("css") || lowerContent.contains("interface") || lowerContent.contains("ui") || lowerContent.contains("visuel")) 
                && roleDescription.contains("frontend")) {
                System.out.println("✅ Match Mot-clé : Frontend identifié (" + email + ")");
                return email;
            }
            // Match BDD / QA
            if ((lowerContent.contains("sql") || lowerContent.contains("base de données") || lowerContent.contains("bug") || lowerContent.contains("test")) 
                && roleDescription.contains("bdd")) {
                System.out.println("✅ Match Mot-clé : BDD/QA identifié (" + email + ")");
                return email;
            }
        }

        // --- ÉTAPE 2 : APPEL IA (TINYLLAMA) EN DERNIER RECOURS ---
        try {
            System.out.println("🧠 Mots-clés non trouvés, consultation de l'IA...");
            String cleanContent = TextCleaner.cleanEmailText(emailContent, 250);
            
            StringBuilder contactsList = new StringBuilder();
            for (Map.Entry<String, String> entry : contactsMap.entrySet()) {
                contactsList.append("- ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
            }

            String prompt = String.format("""
                Tu es un routeur de mails technique. Choisis l'email le plus adapté dans la liste.
                
                ÉQUIPE DISPONIBLE:
                %s

                MAIL À ANALYSER:
                "%s"

                RÈGLE: Réponds UNIQUEMENT avec l'email choisi.
                EMAIL DU RESPONSABLE:
                """, contactsList.toString(), cleanContent);

            String response = ollamaClient.generateResponse("tinyllama", prompt).trim();

            // Validation de la réponse de l'IA par Regex
            Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
            Matcher matcher = emailPattern.matcher(response);
            
            if (matcher.find()) {
                String foundEmail = matcher.group();
                // On vérifie que l'email extrait existe bien dans notre dictionnaire
                if (contactsMap.containsKey(foundEmail)) {
                    System.out.println("🤖 IA Success : " + foundEmail);
                    return foundEmail;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur TeamService (IA)");
        }

        // --- ÉTAPE 3 : RETOUR PAR DÉFAUT ---
        // Si rien n'a matché, on prend le premier du dictionnaire
        System.out.println("⚠️ Aucune détection précise, retour au premier contact du JSON.");
        return contactsMap.keySet().iterator().next();
    }

    /**
     * Génère un brouillon propre et structuré sans utiliser l'IA 
     * pour éviter les hallucinations dans le corps du mail.
     */
    public String generateDelegationDraft(String originalSender, String assignee, String content, String trackingId) {
        String cleanContent = TextCleaner.cleanEmailText(content, 200);
        
        return String.format(
            "Bonjour,\n\nPeux-tu traiter la demande de %s qui dit :\n\"%s\"\n\nMerci.\n\n[Ref: %s]",
            originalSender, 
            cleanContent, 
            trackingId
        );
    }
}