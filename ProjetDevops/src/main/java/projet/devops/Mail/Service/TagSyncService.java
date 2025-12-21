package projet.devops.Mail.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import projet.devops.Mail.Gestion.FichierTempTraiter;

/**
 * Service de synchronisation des tags depuis le fichier JSON vers Gmail
 */
@Service
public class TagSyncService {

    private final FichierTempTraiter fichierTempTraiter;
    private final MailService mailService;
    private final TagService tagService;
    private final GmailLabelService gmailLabelService;

    public TagSyncService(FichierTempTraiter fichierTempTraiter, MailService mailService, 
                         TagService tagService, GmailLabelService gmailLabelService) {
        this.fichierTempTraiter = fichierTempTraiter;
        this.mailService = mailService;
        this.tagService = tagService;
        this.gmailLabelService = gmailLabelService;
    }

    /**
     * Synchronise les tags du fichier JSON vers Gmail
     * @return Résultat de la synchronisation
     */
    public SyncResult syncTagsToGmail() throws Exception {
        SyncResult result = new SyncResult();

        // 1. Lire les mails traités avec leurs tags depuis le JSON
        List<Map<String, String>> mailsTraites = fichierTempTraiter.lireMailsTraites();
        result.setTotalEmails(mailsTraites.size());

        if (mailsTraites.isEmpty()) {
            result.setError("Aucun mail traité trouvé. Veuillez d'abord récupérer les mails.");
            return result;
        }

        // 2. Récupérer les mails depuis Gmail pour obtenir les messageId
        List<Map<String, String>> gmailMails = mailService.getAllMails();
        
        // 3. Créer une map pour retrouver rapidement les messageId
        Map<String, String> subjectToMessageId = buildSubjectMap(gmailMails);

        // 4. Appliquer les tags
        for (Map<String, String> mailTraite : mailsTraites) {
            String subject = mailTraite.get("subject");
            String tag = mailTraite.get("tag");

            if (tag == null || tag.isEmpty()) {
                result.incrementSkipped();
                continue;
            }

            String messageId = subjectToMessageId.get(cleanSubject(subject));

            if (messageId != null) {
                // Appliquer IMAP flag
                boolean imapSuccess = tagService.addTag(messageId, tag);
                
                // Appliquer label Gmail
                boolean gmailSuccess = gmailLabelService.applyGmailLabel(messageId, tag);
                
                boolean success = imapSuccess || gmailSuccess;
                
                if (success) {
                    result.incrementSuccess();
                    result.addSuccess(subject, tag);
                } else {
                    result.incrementFailed();
                    result.addFailure(subject, "Échec de l'ajout du tag");
                }
            } else {
                result.incrementNotFound();
                result.addNotFound(subject);
            }
        }

        return result;
    }

    /**
     * Construit une map subject -> messageId pour recherche rapide
     */
    private Map<String, String> buildSubjectMap(List<Map<String, String>> gmailMails) {
        Map<String, String> map = new HashMap<>();
        for (Map<String, String> mail : gmailMails) {
            String subject = mail.get("subject");
            String messageId = mail.get("messageId");
            if (subject != null && messageId != null) {
                map.put(cleanSubject(subject), messageId);
            }
        }
        return map;
    }

    /**
     * Nettoie le subject pour comparaison (minuscules, sans espaces superflus)
     */
    private String cleanSubject(String subject) {
        return subject != null ? subject.trim().toLowerCase() : "";
    }

    /**
     * Classe représentant le résultat de la synchronisation
     */
    public static class SyncResult {
        private int totalEmails;
        private int success;
        private int failed;
        private int notFound;
        private int skipped;
        private Map<String, String> successDetails = new HashMap<>();
        private Map<String, String> failureDetails = new HashMap<>();
        private List<String> notFoundList = new java.util.ArrayList<>();
        private String error;

        public int getTotalEmails() { return totalEmails; }
        public void setTotalEmails(int total) { this.totalEmails = total; }

        public int getSuccess() { return success; }
        public void incrementSuccess() { this.success++; }

        public int getFailed() { return failed; }
        public void incrementFailed() { this.failed++; }

        public int getNotFound() { return notFound; }
        public void incrementNotFound() { this.notFound++; }

        public int getSkipped() { return skipped; }
        public void incrementSkipped() { this.skipped++; }

        public Map<String, String> getSuccessDetails() { return successDetails; }
        public void addSuccess(String subject, String tag) {
            this.successDetails.put(subject, tag);
        }

        public Map<String, String> getFailureDetails() { return failureDetails; }
        public void addFailure(String subject, String reason) {
            this.failureDetails.put(subject, reason);
        }

        public List<String> getNotFoundList() { return notFoundList; }
        public void addNotFound(String subject) {
            this.notFoundList.add(subject);
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getSummary() {
            return String.format(
                "Synchronisation terminée: %d/%d réussis, %d échecs, %d non trouvés, %d ignorés",
                success, totalEmails, failed, notFound, skipped
            );
        }

        public boolean isSuccess() {
            return error == null && failed == 0 && notFound == 0;
        }
    }
}