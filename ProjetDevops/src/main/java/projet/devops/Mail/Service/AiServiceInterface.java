package projet.devops.Mail.Service;

import projet.devops.Mail.Classifier.OllamaClient;

public abstract class AiServiceInterface {

    protected static final String AI_MODEL = "tinyllama";

    protected final OllamaClient ollamaClient;

    protected AiServiceInterface(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public final String execute(String input) {
        try {
            String prompt = buildPrompt(input);
            String raw = callAi(prompt);
            return parseResult(raw);
        } catch (Exception e) {
            System.err.println("[" + getClass().getSimpleName() + "] Erreur IA : " + e.getMessage());
            return getDefaultResult();
        }
    }

    protected abstract String buildPrompt(String input);

    protected abstract String parseResult(String rawResponse);

    protected abstract String getDefaultResult();

    private String callAi(String prompt) throws Exception {
        System.out.println("[" + getClass().getSimpleName() + "] Appel IA en cours...");
        return ollamaClient.generateResponse(AI_MODEL, prompt);
    }
}