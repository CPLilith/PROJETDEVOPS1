package projet.devops.Mail.Classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IAAggregator {
    
    private final List<IAInterface> iaProviders;
    
    // Constructeur par défaut avec 3 IA Ollama
    public IAAggregator() {
        this.iaProviders = new ArrayList<>();
        iaProviders.add(new OllamaIA("tinyllama:latest"));
        //Pas assez de ram pour lancer les 3. 
        //iaProviders.add(new OllamaIA("llama2:latest"));
        //iaProviders.add(new OllamaIA("phi:latest"));
    }
    
    // Constructeur avec liste personnalisée
    public IAAggregator(List<IAInterface> iaProviders) {
        this.iaProviders = iaProviders;
    }
    
    public List<IAResult> analyzeWithAllIA(String emailText) {
        List<IAResult> results = new ArrayList<>();
        
        for (IAInterface provider : iaProviders) {
            try {
                EisenhowerTag tag = provider.analyzeEmail(emailText);
                results.add(new IAResult(provider.getModelName(), tag));
                System.out.println(provider.getModelName() + " -> " + tag);
            } catch (Exception e) {
                System.err.println("Erreur avec " + provider.getModelName() + ": " + e.getMessage());
                results.add(new IAResult(provider.getModelName(), EisenhowerTag.A_MODIFIER));
            }
        }
        
        return results;
    }
    
    public EisenhowerTag getConsensusTag(String emailText) {
        List<IAResult> results = analyzeWithAllIA(emailText);
        
        // Compter les votes de chaque tag
        Map<EisenhowerTag, Integer> voteCount = new HashMap<>();
        for (IAResult result : results) {
            voteCount.put(result.getTag(), voteCount.getOrDefault(result.getTag(), 0) + 1);
        }
        
        // Trouver le tag avec le plus de votes
        EisenhowerTag bestTag = null;
        int maxVotes = 0;
        
        for (Map.Entry<EisenhowerTag, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                bestTag = entry.getKey();
            }
        }
        
        // Si un tag a au moins 2/3 des votes (au moins 2 IA sur 3 d'accord)
        int threshold = (int) Math.ceil(iaProviders.size() * 2.0 / 3.0);
        
        if (maxVotes >= threshold && bestTag != null) {
            return bestTag;
        }
        
        return EisenhowerTag.A_MODIFIER;
    }
    
    public static class IAResult {
        private final String modelName;
        private final EisenhowerTag tag;
        
        public IAResult(String modelName, EisenhowerTag tag) {
            this.modelName = modelName;
            this.tag = tag;
        }
        
        public String getModelName() {
            return modelName;
        }
        
        public EisenhowerTag getTag() {
            return tag;
        }
    }
}