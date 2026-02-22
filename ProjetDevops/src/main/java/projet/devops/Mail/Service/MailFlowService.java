package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final StatusClassifier statusClassifier;
    private final TeamService teamService;
    private List<Mail> cachedMails = new ArrayList<>();

    public MailFlowService(MailService imapService, EisenhowerClassifier classifier,
                           StatusClassifier statusClassifier, TeamService teamService) {
        this.imapService = imapService;
        this.classifier = classifier;
        this.statusClassifier = statusClassifier;
        this.teamService = teamService;
    }

    // --- 1. RÉCUPÉRATION ---
    public List<Mail> fetchMails() throws Exception {
        System.out.println("\n[GMAIL] 📥 Récupération...");
        List<Mail> fetched = imapService.fetchAllMails();
        for (Mail mail : fetched) {
            try {
                List<String> labels = imapService.getLabelsForMessage(mail.getMessageId());
                for (String label : labels) {
                    mail.setAction(label); // setAction(String) gère les custom tags
                }
            } catch (Exception e) {}
        }
        this.cachedMails = fetched;
        return this.cachedMails;
    }

    // --- 2. ANALYSE IA ---
    public void processPendingMails(Persona currentPersona) {
        if (cachedMails.isEmpty()) return;
        System.out.println("\n[IA] 🧠 Analyse Eisenhower...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() == EisenhowerAction.PENDING) {
                mail.setAction(classifier.classify(mail, currentPersona));
            }
        }
    }

    // --- 3. DÉLÉGATION ---
    public void processManualDelegation(String messageId, String assignee) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                mail.setAction(EisenhowerAction.DELEGATE);
                mail.setStatus("EN ATTENTE (" + assignee + ")");
                return;
            }
        }
    }

    public DelegationData processDelegation(String messageId) {
        for (Mail currentMail : cachedMails) {
            if (currentMail.getMessageId().equals(messageId)) {
                String trackingId = "DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String assignee = teamService.suggestAssignee(currentMail.getContent());
                String draft = teamService.generateDelegationDraft(
                        currentMail.getFrom(), assignee, currentMail.getContent(), trackingId);
                currentMail.setAction(EisenhowerAction.DELEGATE);
                String firstName = assignee.split(" ")[0].trim();
                currentMail.setStatus("EN ATTENTE (" + firstName + ")");
                return new DelegationData(assignee, draft, trackingId);
            }
        }
        return null;
    }

    public record DelegationData(String assignee, String draftBody, String trackingId) {}

    // --- 4. TAGS (supporte les custom DO tags) ---

    public void updateMailTagById(String messageId, String tag) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                // setAction(String) gère automatiquement les tags builtin ET custom
                mail.setAction(tag);
                return;
            }
        }
    }

    public void updateStatusById(String messageId, String status) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                mail.setStatus(status);
                return;
            }
        }
    }

    public void detectStatusWithAI() {
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                mail.setStatus(statusClassifier.classifyStatus(mail.getContent()));
            }
        }
    }

    public List<Mail> getMails() { return cachedMails; }

    public void syncToGmail() {
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                // On sync le tag effectif (custom ou builtin)
                imapService.applyLabelToMail(mail.getMessageId(), mail.getEffectiveTag());
            }
        }
    }

    public void updateMailTag(int index, String tag) {
        if (index >= 0 && index < cachedMails.size()) {
            cachedMails.get(index).setAction(tag);
        }
    }
}