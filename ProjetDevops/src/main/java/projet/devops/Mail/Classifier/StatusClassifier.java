package projet.devops.Mail.Classifier;

import org.springframework.stereotype.Component;

import projet.devops.Mail.Service.AiServiceInterface;

@Component
public class StatusClassifier extends AiServiceInterface {

    public StatusClassifier(OllamaClient client) {
        super(client);
    }

    public String classifyStatus(String content) {
        return execute(content);
    }

    @Override
    protected String buildPrompt(String input) {
        String clean = input.length() > 300 ? input.substring(0, 300) : input;
        return """
                Analyse ce mail. La tâche mentionnée est-elle terminée, validée ou faite ?
                Réponds UNIQUEMENT par 'FINALISÉ' si c'est fini, ou 'SUIVI' si c'est encore à faire.

                Texte : "%s"
                """.formatted(clean);
    }

    @Override
    protected String parseResult(String rawResponse) {
        return rawResponse.toUpperCase().contains("FINALISÉ") ? "FINALISÉ" : "SUIVI";
    }

    @Override
    protected String getDefaultResult() {
        return "SUIVI";
    }
}