package projet.devops.Mail.Service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;

@Service
public class TeamService {

    private final OllamaClient ollamaClient;

    // Simulation de l'annuaire de ton entreprise
    private final Map<String, String> teamDirectory = Map.of(
        "BACKEND", "Thomas (thomas.dev@company.com) - Expert Java, Spring, Base de données",
        "FRONTEND", "Sophie (sophie.ui@company.com) - Expert CSS, Thymeleaf, JS",
        "OPS", "Marc (marc.ops@company.com) - Serveurs, Docker, Sécurité, Réseau",
        "MANAGER", "Julie (julie.boss@company.com) - Budget, Planning, RH, Validation"
    );

    public TeamService() {
        // Instanciation du client Ollama
        this.ollamaClient = new OllamaClient("http://localhost:11434");
    }

    // 1. Demander à l'IA qui est le meilleur destinataire
    public String suggestAssignee(String emailContent) {
    try {
        // On limite le texte pour ne pas perdre l'IA
        String cleanContent = emailContent.length() > 400 ? emailContent.substring(0, 400) : emailContent;

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
        
        // NETTOYAGE DE SÉCURITÉ (on garde juste le premier mot alphabétique)
        String cleaned = response.replaceAll("[^a-zA-Z]", " ").trim().split("\\s+")[0];
        
        // On vérifie que c'est bien un membre de l'équipe, sinon par défaut Thomas
        List<String> team = List.of("Thomas", "Sophie", "Marc", "Julie");
        return team.stream()
                   .filter(name -> name.equalsIgnoreCase(cleaned))
                   .findFirst()
                   .orElse("Thomas");

        } catch (Exception e) {
            return "Thomas";
        }
    }

    // 2. Générer le brouillon du mail de délégation
    public String generateDelegationDraft(String originalSender, String assignee, String content, String trackingId) {
        try {
            String cleanContent = content.length() > 300 ? content.substring(0, 300) : content;
            
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