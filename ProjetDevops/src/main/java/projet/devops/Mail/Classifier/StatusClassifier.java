package projet.devops.Mail.Classifier;

import org.springframework.stereotype.Component;

@Component
public class StatusClassifier {
    private final OllamaClient client;

    // public StatusClassifier() {
    //     this.client = new OllamaClient("http://localhost:11434");
    // }

    public StatusClassifier(OllamaClient client) {
        this.client = client;
    }

    public String classifyStatus(String content) {
        try {
            // Nettoyage rapide du contenu pour l'IA
            String clean = content.length() > 300 ? content.substring(0, 300) : content;
            
            String prompt = """
                Analyse ce mail. La tâche mentionnée est-elle terminée, validée ou faite ?
                Réponds UNIQUEMENT par 'FINALISÉ' si c'est fini, ou 'SUIVI' si c'est encore à faire.
                
                Texte : "%s"
                """.formatted(clean);

            String response = client.generateResponse("tinyllama", prompt).toUpperCase();
            return response.contains("FINALISÉ") ? "FINALISÉ" : "SUIVI";
        } catch (Exception e) {
            return "SUIVI";
        }
    }
}