package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.StatusClassifier;
import projet.devops.Mail.Gestion.FichierTemp;
import projet.devops.Mail.Mail;

@Service
public class MailFlowService {

    private final MailService imapService;
    private final EisenhowerClassifier classifier;
    private final StatusClassifier statusClassifier;
    private final TeamService teamService;
    private final FichierTemp fichierTemp;
    private final ContactService contactService;
    
    private List<Mail> cachedMails = new ArrayList<>();

    public MailFlowService(MailService imapService, EisenhowerClassifier classifier,
                           StatusClassifier statusClassifier, TeamService teamService,
                           FichierTemp fichierTemp, ContactService contactService) {
        this.imapService = imapService;
        this.classifier = classifier;
        this.statusClassifier = statusClassifier;
        this.teamService = teamService;
        this.fichierTemp = fichierTemp;
        this.contactService = contactService;
    }

    @PostConstruct
    public void init() { loadCache(); }

    // --- PERSISTANCE ---
    private void saveCache() {
        try {
            List<Map<String, String>> dataToSave = new ArrayList<>();
            for (Mail m : cachedMails) {
                Map<String, String> map = new HashMap<>();
                map.put("messageId", m.getMessageId());
                map.put("subject", m.getSubject());
                map.put("from", m.getFrom());
                map.put("content", m.getContent());
                map.put("date", m.getDate());
                map.put("action", m.getAction().name());
                map.put("status", m.getStatus() != null ? m.getStatus() : "");
                dataToSave.add(map);
            }
            fichierTemp.sauvegarderMails(dataToSave);
        } catch (Exception e) {}
    }

    private void loadCache() {
        try {
            List<Map<String, String>> loadedData = fichierTemp.lireMails();
            if (loadedData != null && !loadedData.isEmpty()) {
                this.cachedMails = new ArrayList<>();
                for (Map<String, String> map : loadedData) {
                    Mail m = new Mail(map.get("messageId"), map.get("date"), map.get("subject"), map.get("from"), map.get("content"));
                    m.setAction(map.get("action"));
                    m.setStatus(map.get("status"));
                    this.cachedMails.add(m);
                }
            }
        } catch (Exception e) {}
    }

    // --- RÉCUPÉRATION ---
    public List<Mail> fetchMails() throws Exception {
        List<Mail> fetched = imapService.fetchAllMails();
        this.cachedMails = fetched;
        saveCache();
        contactService.analyzeAndSaveNewContacts(this.cachedMails);
        return this.cachedMails;
    }

    // --- DÉLÉGATION IA (2 ÉTAPES) ---
    public DelegationData suggestDelegation(String messageId) {
        for (Mail currentMail : cachedMails) {
            if (currentMail.getMessageId().equals(messageId)) {
                String trackingId = "DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Map<String, String> availableContacts = contactService.getAllContacts();
                
                String assigneeEmail = teamService.suggestAssignee(currentMail.getContent(), availableContacts);
                String draft = teamService.generateDelegationDraft(currentMail.getFrom(), assigneeEmail, currentMail.getContent(), trackingId);
                
                return new DelegationData(assigneeEmail, draft, trackingId);
            }
        }
        return null;
    }

    public void confirmDelegation(String messageId, String assigneeEmail, String finalDraft) {
        for (Mail currentMail : cachedMails) {
            if (currentMail.getMessageId().equals(messageId)) {
                try {
                    imapService.sendEmail(assigneeEmail, "Fwd: " + currentMail.getSubject(), finalDraft);
                } catch (Exception e) {
                    System.err.println("❌ Erreur SMTP : " + e.getMessage());
                }

                currentMail.setAction(EisenhowerAction.DELEGATE.name());
                String firstName = assigneeEmail.contains("@") ? assigneeEmail.split("@")[0] : assigneeEmail;
                currentMail.setStatus("ENVOYÉ (" + firstName + ")");
                saveCache();
                return;
            }
        }
    }

    // --- MÉTHODES RÉTABLIES (POUR LE CONTROLLER) ---

    public void processManualDelegation(String messageId, String assignee) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                mail.setAction(EisenhowerAction.DELEGATE.name());
                mail.setStatus("EN ATTENTE (" + assignee + ")");
                saveCache();
                return;
            }
        }
    }

    public void syncToGmail() {
    System.out.println("🔄 Synchronisation des labels vers Gmail...");
    for (Mail mail : cachedMails) {
        // On ne synchronise que si le mail n'est plus en attente (PENDING)
        if (mail.getAction() != EisenhowerAction.PENDING) {
            String tag = mail.getEffectiveTag(); 
            
                // On vérifie que le tag n'est pas nul avant d'appeler le service
                if (tag != null && !tag.isEmpty()) {
                    imapService.applyLabelToMail(mail.getMessageId(), tag);
                }
            }
        }
    }

    public void detectStatusWithAI() {
        System.out.println("🧠 Analyse automatique des statuts Kanban...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                String status = statusClassifier.classifyStatus(mail.getContent());
                mail.setStatus(status);
            }
        }
        saveCache();
    }

    // --- AUTRES FONCTIONNALITÉS ---

    public void processPendingMails(Persona currentPersona) {
        for (Mail mail : cachedMails) {
            if (mail.getAction() == EisenhowerAction.PENDING) {
                mail.setAction(classifier.classify(mail, currentPersona));
            }
        }
        saveCache();
    }

    public void updateMailTagById(String messageId, String tag) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                mail.setAction(tag);
                saveCache();
                return;
            }
        }
    }

    public void updateStatusById(String messageId, String status) {
        for (Mail mail : cachedMails) {
            if (mail.getMessageId().equals(messageId)) {
                mail.setStatus(status);
                saveCache();
                return;
            }
        }
    }

    public List<Mail> getMails() { return cachedMails; }

    public record DelegationData(String assignee, String draftBody, String trackingId) {}
}