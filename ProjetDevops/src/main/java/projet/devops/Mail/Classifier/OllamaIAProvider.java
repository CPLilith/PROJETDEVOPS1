package projet.devops.Mail.Classifier;

public class OllamaIAProvider {

    private final OllamaClient client;
    private final String model;
    private final Persona persona;


    public OllamaIAProvider(String model) {
        this.client = new OllamaClient();
        this.model = model;
        this.persona = PersonaResourceService.loadPersona();
    }

    public OllamaIAProvider(String model, String baseUrl) {
        this.client = new OllamaClient(baseUrl);
        this.model = model;
        this.persona = PersonaConfigLoader.loadPersona();
    }

    /**
     * Génère le prompt complet en fonction de l'email et du persona sélectionné.
     */
    private String createPrompt(String emailText, Persona persona) {
        return """
            %s
            Analyse cet email et détermine son quadrant Eisenhower.
            Réponds UNIQUEMENT par:
            URGENT_IMPORTANT, IMPORTANTS, URGENT ou NON_URGENT_NON_IMPORTANT

            URGENT_IMPORTANT = Urgent et Important (à faire immédiatement)
            IMPORTANTS = Important mais pas Urgent (à planifier)
            URGENT = Urgent mais pas Important (à déléguer)
            NON_URGENT_NON_IMPORTANT = Ni Urgent ni Important (à éliminer)

            Email:
            %s
            """
            .formatted(
                PersonaPromptProvider.getPersonaSection(persona),
                emailText
            );
    }

    /**
     * Analyse un email avec un persona donné.
     */
    public EisenhowerTag analyzeEmail(String emailText, Persona persona) throws Exception {
        String cleanedText = TextCleaner.cleanEmailText(emailText, 500);
        String prompt = createPrompt(cleanedText, persona);

        String response = client.generateResponse(model, prompt);
        System.out.println("Réponse Ollama (" + model + "): " + response);

        return parseResponse(response);
    }

    /**
     * Fallback sans persona explicite (NEUTRE).
     */
    public EisenhowerTag analyzeEmail(String emailText) throws Exception {
        return analyzeEmail(emailText, Persona.NEUTRE);
    }

    /**
     * Interprétation robuste de la réponse du LLM.
     */
    private EisenhowerTag parseResponse(String response) {
        if (response == null || response.isBlank()) {
            return EisenhowerTag.A_MODIFIER;
        }

        String upperResponse = response.toUpperCase().trim();

        if (upperResponse.contains("URGENT_IMPORTANT")) {
            return EisenhowerTag.URGENT_IMPORTANT;
        }
        if (upperResponse.contains("NON_URGENT_NON_IMPORTANT")) {
            return EisenhowerTag.NON_URGENT_NON_IMPORTANT;
        }
        if (upperResponse.contains("IMPORTANTS")) {
            return EisenhowerTag.IMPORTANTS;
        }
        if (upperResponse.contains("URGENT")) {
            return EisenhowerTag.URGENT;
        }

        return EisenhowerTag.A_MODIFIER;
    }
}
