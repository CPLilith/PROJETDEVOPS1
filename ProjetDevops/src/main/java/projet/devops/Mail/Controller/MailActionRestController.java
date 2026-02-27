package projet.devops.Mail.Controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Repository.PersonaRepository;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.MailFlowService.DelegationData;

@RestController
@RequestMapping("/api/mail-actions")
public class MailActionRestController {

    private final MailFlowService flowService;
    private final PersonaRepository personaRepository; 

    public MailActionRestController(MailFlowService flowService, PersonaRepository personaRepository) {
        this.flowService = flowService;
        this.personaRepository = personaRepository;
    }

    @PostMapping("/fetch")
    public ResponseEntity<Map<String, String>> fetch() {
        try {
            flowService.fetchMails();
            return ResponseEntity.ok(Map.of("status", "success", "message", "Mails fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> sync() {
        try {
            flowService.syncToGmail();
            flowService.fetchMails();
            return ResponseEntity.ok(Map.of("status", "success", "message", "Mails synced and fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyze() {
        flowService.processPendingMails(personaRepository.load());
        return ResponseEntity.ok(Map.of("status", "success", "message", "Pending mails analyzed"));
    }

    @PutMapping("/{messageId}/tag")
    public ResponseEntity<Map<String, String>> updateMailTag(
            @PathVariable("messageId") String messageId,
            @RequestBody Map<String, String> payload) {
        
        String tag = payload.get("tag");
        if (tag == null || tag.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tag cannot be empty"));
        }

        flowService.updateMailTagById(messageId, tag);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Tag updated"));
    }

    @PutMapping("/{messageId}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable("messageId") String messageId, 
            @RequestBody Map<String, String> payload) {
        
        String status = payload.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status cannot be empty"));
        }

        flowService.updateStatusById(messageId, status.toUpperCase());
        return ResponseEntity.ok(Map.of("status", "success", "message", "Status updated"));
    }

    @PostMapping("/auto-status")
    public ResponseEntity<Map<String, String>> autoStatus() {
        flowService.detectStatusWithAI();
        return ResponseEntity.ok(Map.of("status", "success", "message", "Statuses automatically updated with AI"));
    }

    // --- DÉLÉGATION ---
    
    @PostMapping("/{messageId}/delegate-auto")
    public ResponseEntity<DelegationData> delegateAuto(@PathVariable("messageId") String messageId) {
        DelegationData data = flowService.suggestDelegation(messageId);
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(data);
    }

    @PostMapping("/{messageId}/delegate-confirm")
    public ResponseEntity<Map<String, String>> delegateConfirm(
            @PathVariable("messageId") String messageId, 
            @RequestBody Map<String, String> payload) {
        
        String assignee = payload.get("assignee");
        String draftBody = payload.get("draftBody");
        
        if (assignee == null || draftBody == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Assignee and draftBody are required"));
        }

        flowService.confirmDelegation(messageId, assignee, draftBody);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Delegation confirmed"));
    }

    @PostMapping("/{messageId}/delegate-manual")
    public ResponseEntity<Map<String, String>> delegateManual(
            @PathVariable("messageId") String messageId, 
            @RequestBody Map<String, String> payload) {
        
        String assignee = payload.get("assignee");
        if (assignee == null || assignee.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Assignee is required"));
        }

        flowService.processManualDelegation(messageId, assignee);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Manual delegation processed"));
    }

    // --- PERSONA ---
    
    @PutMapping("/persona")
    public ResponseEntity<Map<String, String>> persona(@RequestBody Map<String, String> payload) {
        String p = payload.get("persona");
        if (p == null || p.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Persona value is required"));
        }

        try {
            personaRepository.save(Persona.valueOf(p.toUpperCase()));
            return ResponseEntity.ok(Map.of("status", "success", "persona", p.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid Persona value"));
        }
    }
}