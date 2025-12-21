package projet.devops.Mail.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import projet.devops.Mail.Service.TagSyncService;
import projet.devops.Mail.Service.TagSyncService.SyncResult;

/**
 * Controller REST pour synchroniser les tags vers Gmail
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagSyncController {

    private final TagSyncService tagSyncService;

    public TagSyncController(TagSyncService tagSyncService) {
        this.tagSyncService = tagSyncService;
    }

    /**
     * Synchronise les tags depuis le fichier JSON vers Gmail
     * POST /api/tags/sync
     * 
     * @return Résultat détaillé de la synchronisation
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResult> syncTags() {
        try {
            SyncResult result = tagSyncService.syncTagsToGmail();
            
            if (result.getError() != null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(result);
            }
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .body(result);
            }
            
        } catch (Exception e) {
            SyncResult errorResult = new SyncResult();
            errorResult.setError("Erreur lors de la synchronisation: " + e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResult);
        }
    }

    /**
     * Vérifie le statut du service
     * GET /api/tags/status
     */
    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        return ResponseEntity.ok(new StatusResponse(
            "Tag sync service is running",
            "ready"
        ));
    }

    /**
     * Classe pour la réponse de statut
     */
    public static class StatusResponse {
        private String message;
        private String status;

        public StatusResponse(String message, String status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
}