package projet.devops.Mail.Classifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import projet.devops.Mail.Mail;

@Service
public class EisenhowerClassifier {

    private final Map<String, ClassificationStrategy> strategies;
    private String currentStrategyName;
    private final RestTemplate restTemplate;

    public EisenhowerClassifier(List<ClassificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(ClassificationStrategy::getStrategyName, s -> s));
        this.currentStrategyName = "Ollama (LLM Local)";
        this.restTemplate = new RestTemplate();
    }

    public EisenhowerAction classify(Mail mail, Persona persona) {
        ClassificationStrategy strategy = strategies.get(currentStrategyName);
        if (strategy == null) return EisenhowerAction.PENDING;
        return strategy.classify(mail, persona);
    }

    public List<String> getAvailableStrategies() { return List.copyOf(strategies.keySet()); }

    public void setStrategy(String strategyName) {
        if (strategies.containsKey(strategyName)) this.currentStrategyName = strategyName;
    }

    // --- FIX ERREUR 400 & 500 ---
    // Dans EisenhowerClassifier.java

    public String extractEventDetails(String content) {
        if (content == null || content.trim().isEmpty()) {
            System.out.println("   [IA Warning] Contenu vide ou null.");
            return "AUCUN";
        }

        try {
            String cleanContent = content.length() > 500 ? content.substring(0, 500) : content;
            
            // DEBUG : Voir le prompt exact
            // System.out.println("   [IA Prompt] Envoi de " + cleanContent.length() + " caractères...");

            String prompt = """
                Analyse ce texte. S'il contient une date de rendez-vous ou d'échéance et un lieu, extrais-les.
                Format de réponse attendu UNIQUEMENT : "JJ/MM/AAAA - LIEU" (exemple: "12/03/2026 - Paris").
                Si aucune date précise n'est trouvée, réponds juste : "AUCUN".
                
                Texte : "%s"
            """.formatted(cleanContent);

            String url = "http://localhost:11434/api/generate";
            
            // Nettoyage JSON strict
            String jsonPrompt = prompt
                .replace("\\", "\\\\") 
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", " ")
                .replace("\t", " ");

            String jsonBody = String.format("{\"model\": \"tinyllama\", \"prompt\": \"%s\", \"stream\": false}", jsonPrompt); 

            String response = restTemplate.postForObject(url, jsonBody, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            
            if (root.has("response")) {
                String aiResponse = root.path("response").asText().trim();
                // DEBUG : Voir la réponse brute de l'IA
                // System.out.println("   [IA Raw Response] " + aiResponse);
                return aiResponse;
            }
            return "AUCUN";

        } catch (Exception e) {
            System.err.println("   [IA ERROR] " + e.getMessage());
            return "AUCUN";
        }
    }
}