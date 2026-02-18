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
    private final TeamService teamService; // Service de délégation
    private List<Mail> cachedMails = new ArrayList<>();

    public MailFlowService(MailService imapService, EisenhowerClassifier classifier, 
                            StatusClassifier statusClassifier, TeamService teamService) {
        this.imapService = imapService;
        this.classifier = classifier;
        this.statusClassifier = statusClassifier;
        this.teamService = teamService;
    }

    // --- 1. RECUPERATION ---
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

    // --- 2. ANALYSE IA (EISENHOWER) ---
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

    /**
     * Délégation Manuelle : L'utilisateur choisit lui-même le destinataire.
     */
    public void processManualDelegation(String messageId, String assignee) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                mail.setAction(EisenhowerAction.DELEGATE);
                // On enregistre le nom choisi dans le statut pour le Kanban
                mail.setStatus("EN ATTENTE (" + assignee + ")");
                System.out.println("✅ Délégation manuelle affectée à : " + assignee);
                return;
            }
        }
    }

    /**
     * Auto-Délégation : L'IA choisit l'expert et rédige le brouillon.
     */
    public DelegationData processDelegation(String messageId) {
        for (Mail currentMail : cachedMails) {
            if (currentMail.getMessageId().equals(messageId)) {
                
                // Générer un ID de Tracking unique
                String trackingId = "DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                
                // Identifier le destinataire via l'IA
                System.out.println("[IA] Recherche expert pour délégation...");
                String assignee = teamService.suggestAssignee(currentMail.getContent());
                
                // Rédiger le brouillon
                System.out.println("[IA] 📧 Mail envoyé à la personne concernée..."); 
                String draft = teamService.generateDelegationDraft(currentMail.getFrom(), assignee, currentMail.getContent(), trackingId);
                
                // Mise à jour pour le Kanban
                currentMail.setAction(EisenhowerAction.DELEGATE);
                
                // Nettoyage du nom pour l'affichage (premier mot uniquement)
                String firstName = assignee.split(" ")[0].trim();
                currentMail.setStatus("EN ATTENTE (" + firstName + ")");
                
                return new DelegationData(assignee, draft, trackingId);
            }
        }
        return null;
    }

    public record DelegationData(String assignee, String draftBody, String trackingId) {}

    // --- 4. GESTION DES TAGS & KANBAN ---
    
    public void updateMailTagById(String messageId, String tag) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                try {
                    mail.setAction(EisenhowerAction.valueOf(tag.toUpperCase()));
                } catch (Exception e) {}
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
        System.out.println("\n[IA] 🤖 Analyse des statuts Kanban...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                mail.setStatus(statusClassifier.classifyStatus(mail.getContent()));
            }
        }
    }

    // --- AUTRES ---
    public List<Mail> getMails() { return cachedMails; }
    
    public void syncToGmail() {
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                imapService.applyLabelToMail(mail.getMessageId(), mail.getAction().name());
            }
        }
    }

    // Compatibilité Tests Unitaires
    public void updateMailTag(int index, String tag) {
        if (index >= 0 && index < cachedMails.size()) {
            try {
                cachedMails.get(index).setAction(EisenhowerAction.valueOf(tag.toUpperCase()));
            } catch (Exception e) {}
        }
    }
}