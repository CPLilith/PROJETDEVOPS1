package projet.devops.Mail.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.TextCleaner;
import projet.devops.Mail.Mail;

@Service
public class MeetingPrepService {

    // --- CONSTANTES DRY ---
    private static final String AI_MODEL = "tinyllama";
    private static final int MAX_CONTENT_LENGTH = 200;

    private final MailFlowService mailFlowService;
    private final OllamaClient ollamaClient;

    public MeetingPrepService(MailFlowService mailFlowService, OllamaClient ollamaClient) {
        this.mailFlowService = mailFlowService;
        this.ollamaClient = ollamaClient;
    }

    // SRP : La méthode principale devient un chef d'orchestre ultra lisible
    public String generateMeetingMemo(String messageId) {
        Mail targetMail = findMail(messageId);

        if (targetMail == null) {
            return "Erreur : Impossible de retrouver les détails de ce rendez-vous.";
        }

        List<Mail> contextMails = getContextMails(targetMail.getFrom());
        String prompt = buildPrompt(targetMail, contextMails);

        return callAi(prompt);
    }

    // =========================================================
    // SOUS-MÉTHODES (Chacune a UNE SEULE responsabilité)
    // =========================================================

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

    private String buildPrompt(Mail targetMail, List<Mail> contextMails) {
        StringBuilder contextText = new StringBuilder();

        for (Mail m : contextMails) {
            String cleanContent = TextCleaner.cleanEmailText(m.getContent(), MAX_CONTENT_LENGTH);
            if (m.getContent().length() > MAX_CONTENT_LENGTH) {
                cleanContent += "...";
            }

            // DRY : Utilisation de String.format plutôt que de concaténer avec des +
            // partout
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
                """, targetMail.getSubject(), targetMail.getFrom(), contextText.toString());
    }

    private String callAi(String prompt) {
        try {
            System.out.println("[IA] ⏳ Génération de la fiche mémo en cours...");
            return ollamaClient.generateResponse(AI_MODEL, prompt);
        } catch (Exception e) {
            System.err.println("❌ Erreur de génération IA : " + e.getMessage());
            return "Une erreur est survenue lors de la génération de la fiche.";
        }
    }
}