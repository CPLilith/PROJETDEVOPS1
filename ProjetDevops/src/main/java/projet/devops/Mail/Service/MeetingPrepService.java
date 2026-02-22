package projet.devops.Mail.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Mail;

@Service
public class MeetingPrepService {

    private final MailFlowService mailFlowService;
    private final OllamaClient ollamaClient;

    public MeetingPrepService(MailFlowService mailFlowService) {
        this.mailFlowService = mailFlowService;
        // On instancie le client Ollama pour interroger l'IA locale
        this.ollamaClient = new OllamaClient("http://localhost:11434");
    }

    public String generateMeetingMemo(String messageId) {
        // 1. Récupérer le mail qui a déclenché le RDV
        Mail targetMail = mailFlowService.getMails().stream()
                .filter(m -> m.getMessageId().equals(messageId))
                .findFirst()
                .orElse(null);

        if (targetMail == null) {
            return "Erreur : Impossible de retrouver les détails de ce rendez-vous.";
        }

        // 2. Trouver le contexte : les 5 derniers mails échangés avec le même expéditeur
        String sender = targetMail.getFrom();
        List<Mail> contextMails = mailFlowService.getMails().stream()
                .filter(m -> m.getFrom().equals(sender))
                .limit(5) // On limite pour ne pas saturer l'IA (tinyllama)
                .collect(Collectors.toList());

        // 3. Construire l'historique pour l'IA
        StringBuilder contextText = new StringBuilder();
        for (Mail m : contextMails) {
            String cleanContent = m.getContent().length() > 200 
                ? m.getContent().substring(0, 200) + "..." 
                : m.getContent();
                
            contextText.append("- Date: ").append(m.getDate())
                       .append(" | Sujet: ").append(m.getSubject())
                       .append("\n  Extrait: ").append(cleanContent)
                       .append("\n\n");
        }

        // 4. Créer le Prompt pour générer la Fiche Mémo
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

        // 5. Appeler l'IA
        try {
            System.out.println("[IA] ⏳ Génération de la fiche mémo en cours...");
            return ollamaClient.generateResponse("tinyllama", prompt);
        } catch (Exception e) {
            System.err.println("❌ Erreur de génération IA : " + e.getMessage());
            return "Une erreur est survenue lors de la génération de la fiche.";
        }
    }
}