package projet.devops.Mail.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.StatusClassifier;
import projet.devops.Mail.Event.MailClassifiedEvent;
import projet.devops.Mail.Event.MailEventPublisher;
import projet.devops.Mail.Mail;
import projet.devops.Mail.Repository.MailCacheRepository;
import projet.devops.Mail.Repository.PersonaRepository;

@Service
public class MailFlowService {

    private final MailService imapService;
    private final EisenhowerClassifier classifier;
    private final StatusClassifier statusClassifier;
    private final TeamService teamService;
    private final ContactService contactService;
    private final MailSenderService mailSenderService;
    private final MailCacheRepository cacheRepository;
    private final MailEventPublisher eventPublisher;   
    private final PersonaRepository personaRepository; 

    private List<Mail> cachedMails = new ArrayList<>();

    public MailFlowService(MailService imapService, EisenhowerClassifier classifier,
            StatusClassifier statusClassifier, TeamService teamService,
            ContactService contactService, MailSenderService mailSenderService,
            MailCacheRepository cacheRepository,
            MailEventPublisher eventPublisher,
            PersonaRepository personaRepository) {
        this.imapService = imapService;
        this.classifier = classifier;
        this.statusClassifier = statusClassifier;
        this.teamService = teamService;
        this.contactService = contactService;
        this.mailSenderService = mailSenderService;
        this.cacheRepository = cacheRepository;
        this.eventPublisher = eventPublisher;
        this.personaRepository = personaRepository;
    }

    @PostConstruct
    public void init() {
        this.cachedMails = cacheRepository.loadCache();
    }

    private void saveCache() {
        cacheRepository.saveCache(this.cachedMails);
    }

    private Mail findMailById(String messageId) {
        return cachedMails.stream()
                .filter(m -> m.getMessageId().equals(messageId))
                .findFirst()
                .orElse(null);
    }

    // --- RÉCUPÉRATION ---
    public List<Mail> fetchMails() throws Exception {
        this.cachedMails = imapService.fetchAllMails();
        saveCache();
        contactService.analyzeAndSaveNewContacts(this.cachedMails);
        return this.cachedMails;
    }

    public void processPendingMails(Persona currentPersona) {
        for (Mail mail : cachedMails) {
            if (mail.getAction() == EisenhowerAction.PENDING) {
                String tag = classifier.classifyAsString(mail, currentPersona);
                mail.setAction(tag);

                eventPublisher.publish(new MailClassifiedEvent(mail));
            }
        }
        saveCache();
    }

    public DelegationData suggestDelegation(String messageId) {
        Mail mail = findMailById(messageId);
        if (mail == null) return null;

        String trackingId = "DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Map<String, String> availableContacts = contactService.getAllContacts();

        String assigneeEmail = teamService.suggestAssignee(mail.getContent(), availableContacts);
        String draft = teamService.generateDelegationDraft(mail.getFrom(), assigneeEmail,
                mail.getContent(), trackingId);

        return new DelegationData(assigneeEmail, draft, trackingId);
    }

    public void confirmDelegation(String messageId, String assigneeEmail, String finalDraft) {
        Mail mail = findMailById(messageId);
        if (mail == null) return;

        try {
            mailSenderService.sendEmail(assigneeEmail, "Fwd: " + mail.getSubject(), finalDraft);
        } catch (Exception e) {
            System.err.println("Erreur SMTP : " + e.getMessage());
        }

        mail.setAction(EisenhowerAction.DELEGATE.name());
        String firstName = assigneeEmail.contains("@") ? assigneeEmail.split("@")[0] : assigneeEmail;
        mail.setStatus("ENVOYÉ (" + firstName + ")");
        saveCache();
    }

    public void processManualDelegation(String messageId, String assignee) {
        Mail mail = findMailById(messageId);
        if (mail != null) {
            mail.setAction(EisenhowerAction.DELEGATE.name());
            mail.setStatus("EN ATTENTE (" + assignee + ")");
            saveCache();
        }
    }

    // --- SYNCHRONISATION ---
    public void syncToGmail() {
        System.out.println("Synchronisation des labels vers Gmail...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                String tag = mail.getEffectiveTag();
                if (tag != null && !tag.isEmpty()) {
                    imapService.applyLabelToMail(mail.getMessageId(), tag);
                }
            }
        }
    }

    public void detectStatusWithAI() {
        System.out.println("Analyse automatique des statuts Kanban...");
        for (Mail mail : cachedMails) {
            if (mail.getAction() != EisenhowerAction.PENDING) {
                String status = statusClassifier.classifyStatus(mail.getContent());
                mail.setStatus(status);
            }
        }
        saveCache();
    }

    // --- MISES À JOUR MANUELLES ---
    public void updateMailTagById(String messageId, String tag) {
        Mail mail = findMailById(messageId);
        if (mail != null) {
            mail.setAction(tag);
            saveCache();
        }
    }

    public void updateStatusById(String messageId, String status) {
        Mail mail = findMailById(messageId);
        if (mail != null) {
            mail.setStatus(status);
            saveCache();
        }
    }

    public List<Mail> getMails() {
        return cachedMails;
    }

    public record DelegationData(String assignee, String draftBody, String trackingId) {}

    public void cleanMailsAfterTagDeletion(String deletedTagName) {
        boolean modified = false;
        for (Mail mail : cachedMails) {
            if (deletedTagName.equals(mail.getEffectiveTag())) {
                mail.setAction("DO");
                modified = true;
            }
        }
        if (modified) saveCache();
    }
}