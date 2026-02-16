package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.StatusClassifier;
import projet.devops.Mail.Mail;

@Service
public class MailFlowService {

    private final MailService imapService; 
    private final EisenhowerClassifier classifier;
    private List<Mail> cachedMails = new ArrayList<>();
    private final StatusClassifier statusClassifier = new StatusClassifier();

    public MailFlowService(MailService imapService, EisenhowerClassifier classifier) {
        this.imapService = imapService;
        this.classifier = classifier;
    }

    public List<Mail> fetchMails() throws Exception {
        System.out.println("\n[GMAIL] 📥 Récupération...");
        List<Mail> fetched = imapService.fetchAllMails(); 
        for (Mail mail : fetched) {
            try {
                List<String> labels = imapService.getLabelsForMessage(mail.getMessageId());
                for (String label : labels) {
                    try {
                        mail.setAction(EisenhowerAction.valueOf(label.toUpperCase()));
                    } catch (IllegalArgumentException e) {}
                }
            } catch (Exception e) {}
        }
        this.cachedMails = fetched;
        return this.cachedMails;
    }

    public void processPendingMails(Persona currentPersona) {
        if (cachedMails.isEmpty()) return;
        System.out.println("\n[IA] 🧠 Analyse...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() == EisenhowerAction.PENDING) {
                mail.setAction(classifier.classify(mail, currentPersona));
            }
        }
    }

    // --- NOUVELLE MÉTHODE SÉCURISÉE PAR ID ---
    public void updateStatusById(String messageId, String status) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                mail.setStatus(status);
                System.out.println("✅ Status mis à jour pour : " + mail.getSubject());
                return;
            }
        }
    }

    // Vos méthodes existantes...
    public List<String> extractDoEvents() {
        List<Mail> doMails = cachedMails.stream()
                .filter(m -> m.getAction() == EisenhowerAction.DO)
                .collect(Collectors.toList());
        List<String> events = new ArrayList<>();
        for (Mail mail : doMails) {
            String details = classifier.extractEventDetails(mail.getContent());
            if (!details.contains("AUCUN")) events.add("📌 " + mail.getSubject() + " | " + details);
        }
        return events;
    }

    public List<Mail> getMails() { return cachedMails; }
    
    public void syncToGmail() {
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                imapService.applyLabelToMail(mail.getMessageId(), mail.getAction().name());
            }
        }
    }

    public void updateMailTag(int index, String tag) {
        if (index >= 0 && index < cachedMails.size()) {
            try {
                cachedMails.get(index).setAction(EisenhowerAction.valueOf(tag.toUpperCase()));
            } catch (Exception e) {}
        }
    }

    public void detectStatusWithAI() {
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                mail.setStatus(statusClassifier.classifyStatus(mail.getContent()));
            }
        }
    }
}