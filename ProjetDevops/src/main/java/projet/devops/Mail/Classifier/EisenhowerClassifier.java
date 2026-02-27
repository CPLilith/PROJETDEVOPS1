package projet.devops.Mail.Classifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Model.EisenhowerAction;
import projet.devops.Mail.Model.Mail;
import projet.devops.Mail.Model.Persona;
import projet.devops.Mail.Service.CustomDoTagService;
import projet.devops.Mail.Service.PersonaPromptProvider;
import projet.devops.Mail.Service.TextCleaner;

@Service
public class EisenhowerClassifier {

    private final Map<String, ClassificationStrategy> strategies;
    private String currentStrategyName;

    private final OllamaClient ollamaClient;
    private final CustomDoTagService customDoTagService;

    public EisenhowerClassifier(List<ClassificationStrategy> strategyList,
                                OllamaClient ollamaClient,
                                CustomDoTagService customDoTagService) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ClassificationStrategy::getStrategyName, s -> s));
        this.currentStrategyName = "Ollama (LLM Local)";
        this.ollamaClient = ollamaClient;
        this.customDoTagService = customDoTagService;
    }

    /**
     * Retourne l'action Eisenhower standard (DO, PLAN, DELEGATE, DELETE).
     * Utiliser classifyAsString() si on veut récupérer un custom tag DO.
     */
    public EisenhowerAction classify(Mail mail, Persona persona) {
        ClassificationStrategy strategy = strategies.get(currentStrategyName);
        if (strategy == null)
            return EisenhowerAction.PENDING;
        List<String> customTags = customDoTagService.getCustomTags();
        return strategy.classify(mail, persona, customTags);
    }

    /**
     * Retourne le tag brut sous forme de String — peut être un custom tag comme "DO_FORMATION".
     * À utiliser dans processPendingMails pour appeler mail.setAction(String).
     */
    public String classifyAsString(Mail mail, Persona persona) {
        if (strategies.get(currentStrategyName) == null)
            return EisenhowerAction.PENDING.name();

        List<String> customTags = customDoTagService.getCustomTags();

        try {
            // On demande à la stratégie de classifier
            // La réponse IA brute peut contenir un custom tag
            // On réutilise OllamaClient directement pour avoir la string brute
            String personaText = PersonaPromptProvider.getPersonaSection(persona);
            String cleanContent = TextCleaner.cleanEmailText(mail.getContent(), 500);

            StringBuilder customTagsSection = new StringBuilder();
            String customTagsInstruction = "";
            if (!customTags.isEmpty()) {
                customTagsSection.append("### SOUS-CATÉGORIES DO DISPONIBLES :\n");
                customTagsSection.append("Si le mail est DO, affine avec une de ces sous-catégories :\n");
                for (String tag : customTags) {
                    String label = tag.startsWith("DO_") ? tag.substring(3).replace("_", " ") : tag;
                    customTagsSection.append("- ").append(tag).append(" (").append(label).append(")\n");
                }
                customTagsSection.append("Si aucune sous-catégorie ne correspond, réponds simplement DO.\n\n");
                customTagsInstruction = ", ou un tag DO personnalisé listé ci-dessus";
            }

            String prompt = """
                    %s
                    Tu es un expert en productivité utilisant la matrice d'Eisenhower.
                    Ton rôle est de classer les emails dans l'un des quadrants suivants :
                    1. DO (Urgent & Important)
                    2. PLAN (Important, non Urgent)
                    3. DELEGATE (Urgent, non Important)
                    4. DELETE (Ni Urgent, ni Important)

                    %s### INSTRUCTIONS :
                    Réponds UNIQUEMENT par : DO, PLAN, DELEGATE, DELETE%s.

                    Sujet: %s
                    De: %s
                    Contenu: %s
                    """.formatted(
                    personaText,
                    customTagsSection.toString(),
                    customTagsInstruction,
                    mail.getSubject(), mail.getFrom(), cleanContent);

            String raw = ollamaClient.generateResponse("tinyllama", prompt).trim().toUpperCase();

            // Cherche d'abord un custom tag dans la réponse
            for (String tag : customTags) {
                if (raw.contains(tag)) return tag;
            }

            // Sinon retombe sur les quadrants standard
            if (raw.contains("DELETE"))  return "DELETE";
            if (raw.contains("DELEGATE")) return "DELEGATE";
            if (raw.contains("PLAN"))    return "PLAN";
            if (raw.contains("DO"))      return "DO";

            return EisenhowerAction.PENDING.name();

        } catch (Exception e) {
            System.err.println("⚠️ [classifyAsString] " + e.getMessage());
            return EisenhowerAction.PENDING.name();
        }
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