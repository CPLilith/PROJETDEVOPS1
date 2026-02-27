package projet.devops.Mail.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Model.Mail;

@Service
public class MeetingPrepService extends AiServiceInterface {

    private static final int MAX_CONTENT_LENGTH = 200;

    private final MailFlowService mailFlowService;

    // On garde une référence au mail courant pour buildPrompt()
    private Mail currentMail;
    private List<Mail> contextMails;

    public MeetingPrepService(MailFlowService mailFlowService, OllamaClient ollamaClient) {
        super(ollamaClient);
        this.mailFlowService = mailFlowService;
    }

    public String generateMeetingMemo(String messageId) {
        this.currentMail = findMail(messageId);

        if (currentMail == null) {
            return "Erreur : Impossible de retrouver les détails de ce rendez-vous.";
        }

        this.contextMails = getContextMails(currentMail.getFrom());

        return execute(currentMail.getSubject());
    }

    @Override
    protected String buildPrompt(String input) {
        StringBuilder contextText = new StringBuilder();
        for (Mail m : contextMails) {
            String cleanContent = TextCleaner.cleanEmailText(m.getContent(), MAX_CONTENT_LENGTH);
            if (m.getContent().length() > MAX_CONTENT_LENGTH) cleanContent += "...";
            contextText.append(String.format("- Date: %s | Sujet: %s\n  Extrait: %s\n\n",
                    m.getDate(), m.getSubject(), cleanContent));
        }

        return String.format("""
                Tu es un assistant de direction expert. Un rendez-vous est prévu concernant le sujet : "%s" avec %s.

                Voici l'historique de nos derniers échanges :
                %s

                Génère une brève "Fiche Mémo" pour préparer cette réunion.
                Réponds de manière professionnelle et structure ta réponse ainsi :
                - 🎯 Objectif supposé du RDV
                - 📝 Synthèse des derniers échanges
                - ⚠️ Points clés à retenir
                """, currentMail.getSubject(), currentMail.getFrom(), contextText.toString());
    }

    @Override
    protected String parseResult(String rawResponse) {
        return rawResponse;
    }

    @Override
    protected String getDefaultResult() {
        return "Une erreur est survenue lors de la génération de la fiche.";
    }

    private Mail findMail(String messageId) {
        return mailFlowService.getMails().stream()
                .filter(m -> m.getMessageId().equals(messageId))
                .findFirst()
                .orElse(null);
    }

    private List<Mail> getContextMails(String sender) {
        return mailFlowService.getMails().stream()
                .filter(m -> m.getFrom().equals(sender))
                .limit(5)
                .collect(Collectors.toList());
    }
}