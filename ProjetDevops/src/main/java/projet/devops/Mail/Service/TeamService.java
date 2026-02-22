package projet.devops.Mail.Service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.TextCleaner;

@Service
public class TeamService {

    private final OllamaClient ollamaClient;

    private final Map<String, String> teamDirectory = Map.of(
        "BACKEND", "Thomas (thomas.dev@company.com) - Expert Java, Spring, Base de données",
        "FRONTEND", "Sophie (sophie.ui@company.com) - Expert CSS, Thymeleaf, JS",
        "OPS", "Marc (marc.ops@company.com) - Serveurs, Docker, Sécurité, Réseau",
        "MANAGER", "Julie (julie.boss@company.com) - Budget, Planning, RH, Validation"
    );

    // Injection par Spring
    public TeamService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public String suggestAssignee(String emailContent) {
        try {
            // Utilisation du TextCleaner (DRY)
            String cleanContent = TextCleaner.cleanEmailText(emailContent, 400);

            String prompt = String.format("""
                TÂCHE : Choisis le responsable idéal pour ce mail.
                
                MEMBRES DE L'ÉQUIPE :
                - Thomas (Expert Technique, Serveurs, Bugs)
                - Sophie (Design, Frontend, Marketing)
                - Marc (Commercial, Client, Devis)
                - Julie (RH, Administratif, Factures)

                MAIL : "%s"

                RÈGLE : Réponds UNIQUEMENT avec le PRÉNOM. Pas de phrase. 
                Si tu hésites, réponds "Thomas".
                """, cleanContent);

            String response = ollamaClient.generateResponse("tinyllama", prompt);
            
            String cleaned = response.replaceAll("[^a-zA-Z]", " ").trim().split("\\s+")[0];
            
            List<String> team = List.of("Thomas", "Sophie", "Marc", "Julie");
            return team.stream()
                       .filter(name -> name.equalsIgnoreCase(cleaned))
                       .findFirst()
                       .orElse("Thomas");

        } catch (Exception e) {
            return "Thomas";
        }
    }

    public String generateDelegationDraft(String originalSender, String assignee, String content, String trackingId) {
        try {
            // Utilisation du TextCleaner (DRY)
            String cleanContent = TextCleaner.cleanEmailText(content, 300);
            
            String prompt = String.format("""
                Rédige un mail de délégation court et professionnel pour %s.
                Contexte : Je transmets un mail reçu de %s qui parle de : "%s".
                
                Consignes :
                1. Sois direct et poli.
                2. Inclus IMPÉRATIVEMENT cet ID de suivi dans le texte : [Ref: %s].
                3. Ne mets pas d'objet, juste le corps du message.
                """, assignee, originalSender, cleanContent, trackingId);

            return ollamaClient.generateResponse("tinyllama", prompt);
        } catch (Exception e) {
            return "Bonjour " + assignee + ",\nPeux-tu regarder ça ?\n\n[Ref: " + trackingId + "]";
        }
    }
}