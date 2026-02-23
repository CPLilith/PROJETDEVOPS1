package projet.devops.Mail.Classifier;

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
    public EisenhowerAction classify(Mail mail, Persona persona) {
        try {
            String prompt = buildPrompt(mail, persona);
            // On peut garder tinyllama ou passer à llama3 ici
            String response = client.generateResponse("tinyllama", prompt);
            return parseResponse(response);
        } catch (Exception e) {
            System.err.println("[STRATEGY OLLAMA] Erreur: " + e.getMessage());
            return EisenhowerAction.PENDING;
        }
    }

    private String buildPrompt(Mail mail, Persona persona) {
        String personaText = PersonaPromptProvider.getPersonaSection(persona);
        String cleanContent = TextCleaner.cleanEmailText(mail.getContent(), 500);

        return """
                %s
                Tu es un expert en productivité utilisant la matrice d'Eisenhower.
                Ton rôle est de classer les emails dans l'un des 4 quadrants suivants :
                1. DO (Urgent & Important)
                2. PLAN (Important, non Urgent)
                3. DELEGATE (Urgent, non Important)
                4. DELETE (Ni Urgent, ni Important)

                ### EXEMPLES :
                - Panne serveur -> DO
                - Rapport mensuel -> PLAN
                - Demande administrative -> DELEGATE
                - Pub/Newsletter -> DELETE

                ### INSTRUCTIONS :
                Réponds UNIQUEMENT par : DO, PLAN, DELEGATE ou DELETE.

                Sujet: %s
                De: %s
                Contenu: %s
                """.formatted(personaText, mail.getSubject(), mail.getFrom(), cleanContent);
    }

    private EisenhowerAction parseResponse(String response) {
        if (response == null)
            return EisenhowerAction.PENDING;
        String clean = response.trim().toUpperCase();
        if (clean.contains("DO"))
            return EisenhowerAction.DO;
        if (clean.contains("PLAN"))
            return EisenhowerAction.PLAN;
        if (clean.contains("DELEGATE"))
            return EisenhowerAction.DELEGATE;
        if (clean.contains("DELETE"))
            return EisenhowerAction.DELETE;
        return EisenhowerAction.PENDING;
    }
}