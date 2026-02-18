package projet.devops.Mail.Classifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Mail;

@Service
public class EisenhowerClassifier {

    private final Map<String, ClassificationStrategy> strategies;
    private String currentStrategyName;
    
    // On utilise OllamaClient au lieu de RestTemplate pour éviter les erreurs de JSON
    private final OllamaClient ollamaClient;

    public EisenhowerClassifier(List<ClassificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(ClassificationStrategy::getStrategyName, s -> s));
        this.currentStrategyName = "Ollama (LLM Local)";
        // On initialise le client proprement
        this.ollamaClient = new OllamaClient("http://localhost:11434");
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

    /**
     * Cette méthode a été réécrite pour être robuste (fini les erreurs 500).
     */
    public String extractEventDetails(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "AUCUN";
        }

        try {
            // 1. On limite la taille pour ne pas saturer l'IA (et éviter les timeouts)
            String cleanContent = content.length() > 500 ? content.substring(0, 500) : content;

            // 2. Le prompt pour l'agenda
            String prompt = """
                Analyse le texte ci-dessous pour un agenda.
                Contient-il une date précise de rendez-vous (ou échéance) ET un lieu ?
                
                - Si OUI : Réponds UNIQUEMENT au format "JJ/MM/AAAA - LIEU" (Ex: 12/05/2026 - Paris).
                - Si NON : Réponds UNIQUEMENT "AUCUN".
                
                Texte : 
                """ + cleanContent;

            // 3. Appel sécurisé via OllamaClient (gère les guillemets et caractères spéciaux)
            return ollamaClient.generateResponse("tinyllama", prompt);

        } catch (Exception e) {
            // En cas de problème, on log l'erreur mais ON NE PLANTE PAS l'application
            System.err.println("⚠️ [Agenda IA Error] " + e.getMessage());
            return "AUCUN";
        }
    }
}