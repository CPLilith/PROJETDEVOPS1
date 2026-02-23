package projet.devops.Mail.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Classifier.TextCleaner;
import projet.devops.Mail.Mail;

@Service
public class MeetingPrepService {

    private final MailFlowService mailFlowService;
    private final OllamaClient ollamaClient;

    // Injection par Spring
    public MeetingPrepService(MailFlowService mailFlowService, OllamaClient ollamaClient) {
        this.mailFlowService = mailFlowService;
        this.ollamaClient = ollamaClient;
    }

    public String generateMeetingMemo(String messageId) {
        Mail targetMail = mailFlowService.getMails().stream()
                .filter(m -> m.getMessageId().equals(messageId))
                .findFirst()
                .orElse(null);

        if (targetMail == null) {
            return "Erreur : Impossible de retrouver les détails de ce rendez-vous.";
        }

        String sender = targetMail.getFrom();
        List<Mail> contextMails = mailFlowService.getMails().stream()
                .filter(m -> m.getFrom().equals(sender))
                .limit(5)
                .collect(Collectors.toList());

        StringBuilder contextText = new StringBuilder();
        for (Mail m : contextMails) {
            // Utilisation du TextCleaner (DRY)
            String cleanContent = TextCleaner.cleanEmailText(m.getContent(), 200);

            // On rajoute "..." visuellement si on a tronqué, pour que l'IA comprenne que
            // c'est un extrait
            if (m.getContent().length() > 200) {
                cleanContent += "...";
            }

            contextText.append("- Date: ").append(m.getDate())
                    .append(" | Sujet: ").append(m.getSubject())
                    .append("\n  Extrait: ").append(cleanContent)
                    .append("\n\n");
        }

        String prompt = String.format("""
                Tu es un assistant de direction expert. Un rendez-vous est prévu concernant le sujet : "%s" avec %s.

                Voici l'historique de nos derniers échanges :
                %s

                Génère une brève "Fiche Mémo" pour préparer cette réunion.
                Réponds de manière professionnelle et structure ta réponse ainsi :
                - 🎯 Objectif supposé du RDV
                - 📝 Synthèse des derniers échanges
                - ⚠️ Points clés à retenir
                """, targetMail.getSubject(), sender, contextText.toString());

        try {
            System.out.println("[IA] ⏳ Génération de la fiche mémo en cours...");
            return ollamaClient.generateResponse("tinyllama", prompt);
        } catch (Exception e) {
            System.err.println("❌ Erreur de génération IA : " + e.getMessage());
            return "Une erreur est survenue lors de la génération de la fiche.";
        }
    }
}