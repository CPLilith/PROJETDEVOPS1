package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * Récupère les 10 derniers mails ET restaure leurs labels Gmail.
     * Correction : On lit l'étiquette existante pour ne pas la supprimer.
     */
    public List<Mail> fetchMails() throws Exception {
        System.out.println("\n[GMAIL] 📥 Récupération des messages...");
        List<Mail> fetched = imapService.fetchAllMails(); 

        for (Mail mail : fetched) {
            try {
                // On récupère les étiquettes réelles depuis Gmail
                List<String> labels = imapService.getLabelsForMessage(mail.getMessageId());
                for (String label : labels) {
                    try {
                        // Si un tag (DO, PLAN...) existe déjà, on l'applique immédiatement
                        EisenhowerAction action = EisenhowerAction.valueOf(label.toUpperCase());
                        mail.setAction(action);
                    } catch (IllegalArgumentException e) {
                        // Label Gmail standard (ex: INBOX), on ignore
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Impossible de charger les labels pour : " + mail.getSubject());
            }
        }

        this.cachedMails = fetched;
        System.out.println("[GMAIL] ✅ " + cachedMails.size() + " messages chargés (Tags conservés).");
        return this.cachedMails;
    }

    /**
     * Analyse uniquement les mails qui sont encore en PENDING.
     */
    public void processPendingMails(Persona currentPersona) {
        if (cachedMails.isEmpty()) {
            System.err.println("[ERREUR] ❌ Aucun mail chargé.");
            return;
        }

        System.out.println("\n[IA-OLLAMA] 🧠 Analyse des nouveaux messages - Profil: " + currentPersona);
        for (Mail mail : cachedMails) {
            // Sécurité : On ne touche pas aux mails qui ont déjà un tag (DO, PLAN...)
            if (mail.getAction() == EisenhowerAction.PENDING) {
                EisenhowerAction result = classifier.classify(mail, currentPersona);
                mail.setAction(result);
                System.out.println("[IA] 🎯 " + mail.getSubject() + " -> " + result);
            }
        }
    }

    /**
     * EXTRACTION DES ÉVÉNEMENTS (Date & Lieu)
     * Filtre uniquement les éléments en "DO".
     */
    public List<String> extractDoEvents() {
        System.out.println("\n[IA-OLLAMA] 📅 Extraction des RDV (Priorité DO)...");
        
        // On ne garde que les mails marqués en DO
        List<Mail> doMails = cachedMails.stream()
                .filter(m -> m.getAction() == EisenhowerAction.DO)
                .collect(Collectors.toList());

        List<String> events = new ArrayList<>();
        for (Mail mail : doMails) {
            // On demande à l'IA d'extraire spécifiquement la Date et le Lieu
            String details = classifier.extractEventDetails(mail.getContent());
            if (!details.contains("AUCUN")) {
                events.add("📌 " + mail.getSubject() + " | " + details);
            }
        }
        return events;
    }

    public List<Mail> getMails() { return cachedMails; }
    
    public void syncToGmail() {
        System.out.println("\n[SYNC] 🔄 Mise à jour des labels Gmail...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                imapService.applyLabelToMail(mail.getMessageId(), mail.getAction().name());
            }
        }
    }

    public void updateMailTag(int index, String tag) {
        if (index >= 0 && index < cachedMails.size()) {
            try {
                EisenhowerAction actionEnum = EisenhowerAction.valueOf(tag.toUpperCase());
                cachedMails.get(index).setAction(actionEnum);
                System.out.println("✅ Manuel : Mail " + index + " passé en " + actionEnum);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Tag invalide");
            }
        }
    }
}