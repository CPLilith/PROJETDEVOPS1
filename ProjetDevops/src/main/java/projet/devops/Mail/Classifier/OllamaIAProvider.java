package projet.devops.Mail.Classifier;

public class OllamaIAProvider {
    
    private final OllamaClient client;
    private final String model;
    private final String promptTemplate;
    
    public OllamaIAProvider(String model) {
        this.client = new OllamaClient();
        this.model = model;
        this.promptTemplate = createPromptTemplate();
    }
    
    public OllamaIAProvider(String model, String baseUrl) {
        this.client = new OllamaClient(baseUrl);
        this.model = model;
        this.promptTemplate = createPromptTemplate();
    }
    
    private String createPromptTemplate() {
        return "Analyse cet email et détermine son quadrant Eisenhower. "
            + "Réponds UNIQUEMENT par: URGENT_IMPORTANT, IMPORTANTS, URGENT ou NON_URGENT_NON_IMPORTANT\n"
            + "URGENT_IMPORTANT = Urgent et Important (à faire immédiatement)\n"
            + "IMPORTANTS = Important mais pas Urgent (à planifier)\n"
            + "URGENT = Urgent mais pas Important (à déléguer)\n"
            + "NON_URGENT_NON_IMPORTANT = Ni Urgent ni Important (à éliminer)\n\n"
            + "Email: %s";
    }
    
    public EisenhowerTag analyzeEmail(String emailText) throws Exception {
        String cleanedText = TextCleaner.cleanEmailText(emailText, 500);
        String prompt = String.format(promptTemplate, cleanedText);
        
        String response = client.generateResponse(model, prompt);
        System.out.println("Réponse Ollama (" + model + "): " + response);
        
        return parseResponse(response);
    }
    
    private EisenhowerTag parseResponse(String response) {
        String upperResponse = response.toUpperCase();
        
        if (upperResponse.contains("URGENT_IMPORTANT")) {
            return EisenhowerTag.URGENT_IMPORTANT;
        }
        if (upperResponse.contains("NON_URGENT_NON_IMPORTANT")) {
            return EisenhowerTag.NON_URGENT_NON_IMPORTANT;
        }
        if (upperResponse.contains("IMPORTANTS") || upperResponse.contains("IMPORTANT")) {
            return EisenhowerTag.IMPORTANTS;
        }
        if (upperResponse.contains("URGENT")) {
            return EisenhowerTag.URGENT;
        }
        
        return EisenhowerTag.A_MODIFIER;
    }
}