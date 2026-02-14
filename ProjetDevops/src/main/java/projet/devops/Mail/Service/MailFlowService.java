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

    public List<Mail> fetchMails() throws Exception {
        System.out.println("\n[GMAIL] 📥 Récupération des messages...");
        this.cachedMails = imapService.fetchAllMails(); 
        System.out.println("[GMAIL] ✅ " + cachedMails.size() + " messages chargés en mémoire.");
        return this.cachedMails;
    }

    public void processPendingMails(Persona currentPersona) {
        if (cachedMails.isEmpty()) {
            System.err.println("[ERREUR] ❌ Aucun mail à analyser. Cliquez sur 'Charger' d'abord.");
            return;
        }

        System.out.println("\n[IA-OLLAMA] 🧠 Démarrage analyse - Profil: " + currentPersona);
        System.out.println("------------------------------------------------------------");

        for (Mail mail : cachedMails) {
            if (mail.getAction() == EisenhowerAction.PENDING) {
                System.out.println("[ANALYSING] 🔍 Sujet: " + mail.getSubject());
                
                // Appel Ollama
                EisenhowerAction result = classifier.classify(mail, currentPersona);
                
                mail.setAction(result);
                System.out.println("[RESULT]    🎯 Catégorie: " + result);
                System.out.println("------------------------------------------------------------");
            }
        }
        System.out.println("[IA-OLLAMA] ✅ Analyse terminée.");
    }

    public List<Mail> getMails() { return cachedMails; }
    
    public void syncToGmail() {
        System.out.println("\n[SYNC] 🔄 Synchronisation des labels vers Gmail...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                imapService.applyLabelToMail(mail.getMessageId(), mail.getAction().name());
            }
        }
        System.out.println("[SYNC] ✅ Terminé.");
    }
}