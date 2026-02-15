package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Mail;

@Service
public class MailFlowService {

    private final MailService imapService; 
    private final EisenhowerClassifier classifier;
    private List<Mail> cachedMails = new ArrayList<>();

    public MailFlowService(MailService imapService, EisenhowerClassifier classifier) {
        this.imapService = imapService;
        this.classifier = classifier;
    }

    /**
     * Récupère les mails ET leurs labels existants sur Gmail
     */
    public List<Mail> fetchMails() throws Exception {
        System.out.println("\n[GMAIL] 📥 Récupération des messages...");
        List<Mail> fetched = imapService.fetchAllMails(); 

        for (Mail mail : fetched) {
            // Pour chaque mail, on demande à Gmail quelles sont ses étiquettes
            try {
                List<String> labels = imapService.getLabelsForMessage(mail.getMessageId());
                for (String label : labels) {
                    try {
                        // Si une étiquette Gmail match avec notre Enum (DO, PLAN, etc.)
                        EisenhowerAction action = EisenhowerAction.valueOf(label.toUpperCase());
                        mail.setAction(action);
                    } catch (IllegalArgumentException e) {
                        // C'est un label Gmail standard (INBOX, etc.), on ignore
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Impossible de charger les labels pour : " + mail.getSubject());
            }
        }

        this.cachedMails = fetched;
        System.out.println("[GMAIL] ✅ " + cachedMails.size() + " messages chargés (Labels restaurés).");
        return this.cachedMails;
    }

    public void processPendingMails(Persona currentPersona) {
        if (cachedMails.isEmpty()) {
            System.err.println("[ERREUR] ❌ Aucun mail à analyser. Cliquez sur 'Charger' d'abord.");
            return;
        }

        System.out.println("\n[IA-OLLAMA] 🧠 Démarrage analyse - Profil: " + currentPersona);
        for (Mail mail : cachedMails) {
            // Grâce au chargement des labels, l'IA n'analyse QUE ce qui est resté en PENDING
            if (mail.getAction() == EisenhowerAction.PENDING) {
                EisenhowerAction result = classifier.classify(mail, currentPersona);
                mail.setAction(result);
                System.out.println("[RESULT] 🎯 Sujet: " + mail.getSubject() + " -> " + result);
            }
        }
        System.out.println("[IA-OLLAMA] ✅ Analyse terminée.");
    }

    public List<Mail> getMails() { return cachedMails; }
    
    public void syncToGmail() {
        System.out.println("\n[SYNC] 🔄 Synchronisation vers Gmail...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                imapService.applyLabelToMail(mail.getMessageId(), mail.getAction().name());
            }
        }
        System.out.println("[SYNC] ✅ Terminé.");
    }

    public void updateMailTag(int index, String tag) {
    // On utilise cachedMails au lieu de mails
        if (index >= 0 && index < cachedMails.size()) {
            try {
                EisenhowerAction actionEnum = EisenhowerAction.valueOf(tag.toUpperCase());
                cachedMails.get(index).setAction(actionEnum);
                System.out.println("✅ Mail " + index + " mis à jour : " + actionEnum);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Tag invalide");
            }
        }
    }
}