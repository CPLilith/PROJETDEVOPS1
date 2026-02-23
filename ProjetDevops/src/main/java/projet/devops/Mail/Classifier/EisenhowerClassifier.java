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

    // Injection de la dépendance (SOLID)
    private final OllamaClient ollamaClient;

    public EisenhowerClassifier(List<ClassificationStrategy> strategyList, OllamaClient ollamaClient) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ClassificationStrategy::getStrategyName, s -> s));
        this.currentStrategyName = "Ollama (LLM Local)";
        this.ollamaClient = ollamaClient; // L'IA est fournie par Spring
    }

    public EisenhowerAction classify(Mail mail, Persona persona) {
        ClassificationStrategy strategy = strategies.get(currentStrategyName);
        if (strategy == null)
            return EisenhowerAction.PENDING;
        return strategy.classify(mail, persona);
    }

    public List<String> getAvailableStrategies() {
        return List.copyOf(strategies.keySet());
    }

    public void setStrategy(String strategyName) {
        if (strategies.containsKey(strategyName))
            this.currentStrategyName = strategyName;
    }

    public String extractEventDetails(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "AUCUN";
        }

        try {
            // Utilisation du TextCleaner (DRY)
            String cleanContent = TextCleaner.cleanEmailText(content, 500);

            String prompt = """
                    Analyse le texte ci-dessous pour un agenda.
                    Contient-il une date précise de rendez-vous (ou échéance) ET un lieu ?

                    - Si OUI : Réponds UNIQUEMENT au format "JJ/MM/AAAA - LIEU" (Ex: 12/05/2026 - Paris).
                    - Si NON : Réponds UNIQUEMENT "AUCUN".

                    Texte :
                    """ + cleanContent;

            return ollamaClient.generateResponse("tinyllama", prompt);

        } catch (Exception e) {
            System.err.println("⚠️ [Agenda IA Error] " + e.getMessage());
            return "AUCUN";
        }
    }
}