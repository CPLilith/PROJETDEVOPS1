package projet.devops.Mail.Classifier;

import java.util.List;

import org.springframework.stereotype.Component;

import projet.devops.Mail.Mail;

@Component
public class OllamaStrategy implements ClassificationStrategy {

    private final OllamaClient client;

    public OllamaStrategy(OllamaClient client) {
        this.client = client;
    }

    @Override
    public String getStrategyName() {
        return "Ollama (LLM Local)";
    }

    @Override
    public EisenhowerAction classify(Mail mail, Persona persona, List<String> customTags) {
        try {
            String prompt = buildPrompt(mail, persona, customTags);
            String response = client.generateResponse("tinyllama", prompt);
            return parseResponse(response, customTags);
        } catch (Exception e) {
            System.err.println("[STRATEGY OLLAMA] Erreur: " + e.getMessage());
            return EisenhowerAction.PENDING;
        }
    }

    private String buildPrompt(Mail mail, Persona persona, List<String> customTags) {
        String personaText = PersonaPromptProvider.getPersonaSection(persona);
        String cleanContent = TextCleaner.cleanEmailText(mail.getContent(), 500);

        StringBuilder customTagsSection = new StringBuilder();
        String customTagsInstruction = "";
        if (customTags != null && !customTags.isEmpty()) {
            customTagsSection.append("### SOUS-CATÉGORIES DO DISPONIBLES :\n");
            customTagsSection.append("Si le mail est DO, affine avec une de ces sous-catégories :\n");
            for (String tag : customTags) {
                String label = tag.startsWith("DO_") ? tag.substring(3).replace("_", " ") : tag;
                customTagsSection.append("- ").append(tag).append(" (").append(label).append(")\n");
            }
            customTagsSection.append("Si aucune sous-catégorie ne correspond, réponds simplement DO.\n\n");
            customTagsInstruction = ", ou un tag DO personnalisé listé ci-dessus";
        }

        return """
                %s
                Tu es un expert en productivité utilisant la matrice d'Eisenhower.
                Ton rôle est de classer les emails dans l'un des quadrants suivants :
                1. DO (Urgent & Important)
                2. PLAN (Important, non Urgent)
                3. DELEGATE (Urgent, non Important)
                4. DELETE (Ni Urgent, ni Important)

                %s### EXEMPLES :
                - Panne serveur -> DO
                - Rapport mensuel -> PLAN
                - Demande administrative -> DELEGATE
                - Pub/Newsletter -> DELETE

                ### INSTRUCTIONS :
                Réponds UNIQUEMENT par : DO, PLAN, DELEGATE, DELETE%s.

                Sujet: %s
                De: %s
                Contenu: %s
                """.formatted(
                personaText,
                customTagsSection.toString(),
                customTagsInstruction,
                mail.getSubject(), mail.getFrom(), cleanContent);
    }

    private EisenhowerAction parseResponse(String response, List<String> customTags) {
        if (response == null)
            return EisenhowerAction.PENDING;
        String clean = response.trim().toUpperCase();

        // Ordre important : DELETE avant DO pour éviter un faux match
        if (clean.contains("DELETE"))  return EisenhowerAction.DELETE;
        if (clean.contains("DELEGATE")) return EisenhowerAction.DELEGATE;
        if (clean.contains("PLAN"))    return EisenhowerAction.PLAN;
        if (clean.contains("DO"))      return EisenhowerAction.DO;
        return EisenhowerAction.PENDING;
    }
}