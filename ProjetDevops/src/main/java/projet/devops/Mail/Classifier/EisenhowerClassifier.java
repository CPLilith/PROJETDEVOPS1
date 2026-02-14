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

    // Spring injecte toutes les implémentations de ClassificationStrategy automatiquement
    public EisenhowerClassifier(List<ClassificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(ClassificationStrategy::getStrategyName, s -> s));
        
        // On sélectionne Ollama par défaut
        this.currentStrategyName = "Ollama (LLM Local)";
    }

    public EisenhowerAction classify(Mail mail, Persona persona) {
        ClassificationStrategy strategy = strategies.get(currentStrategyName);
        if (strategy == null) return EisenhowerAction.PENDING;
        return strategy.classify(mail, persona);
    }

    public List<String> getAvailableStrategies() {
        return List.copyOf(strategies.keySet());
    }

    public void setStrategy(String strategyName) {
        if (strategies.containsKey(strategyName)) {
            this.currentStrategyName = strategyName;
        }
    }
}