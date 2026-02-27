package projet.devops.Mail.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import projet.devops.Mail.Model.EisenhowerAction;
import projet.devops.Mail.Model.Mail;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.PersonaResourceService;

record RestResponse<T>(T data, Map<String, String> _links) {}

@RestController
@RequestMapping("/api/mails")
public class MailRestApiController {

    private final MailFlowService flowService;

    public MailRestApiController(MailFlowService flowService) {
        this.flowService = flowService;
    }

    @GetMapping
    public RestResponse<List<RestResponse<Mail>>> apiGetAllMails() {
        List<Mail> mails = flowService.getMails();
        if (mails == null) mails = new ArrayList<>();

        List<RestResponse<Mail>> mailResponses = new ArrayList<>();
        for (Mail mail : mails) {
            mailResponses.add(new RestResponse<>(mail, buildMailLinks(mail)));
        }

        Map<String, String> globalLinks = new HashMap<>();
        globalLinks.put("self", "/api/mails");
        globalLinks.put("fetch", "/api/mails/fetch");
        globalLinks.put("analyze", "/api/mails/analyze");

        return new RestResponse<>(mailResponses, globalLinks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<Mail>> apiGetMailById(@PathVariable("id") String id) {
        Mail foundMail = findById(id);
        if (foundMail == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new RestResponse<>(foundMail, buildMailLinks(foundMail)));
    }

    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> apiFetch() {
        try {
            List<Mail> mails = flowService.fetchMails();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", mails.size());
            Map<String, String> links = new HashMap<>();
            links.put("self", "/api/mails/fetch");
            links.put("mails", "/api/mails");
            response.put("_links", links);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> apiAnalyze() {
        flowService.processPendingMails(PersonaResourceService.loadPersona());
        long classified = flowService.getMails().stream()
                .filter(m -> m.getAction() != EisenhowerAction.PENDING)
                .count();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("classified", classified);
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/mails/analyze");
        links.put("mails", "/api/mails");
        response.put("_links", links);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RestResponse<Mail>> apiUpdateStatus(
            @PathVariable("id") String id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null || status.isBlank()) return ResponseEntity.badRequest().build();

        if (findById(id) == null) return ResponseEntity.notFound().build();

        flowService.updateStatusById(id, status.toUpperCase());
        return ResponseEntity.ok(new RestResponse<>(findById(id), buildMailLinks(findById(id))));
    }

    @PutMapping("/{id}/tag")
    public ResponseEntity<RestResponse<Mail>> apiUpdateTag(
            @PathVariable("id") String id,
            @RequestBody Map<String, String> body) {

        String tag = body.get("tag");
        if (tag == null || tag.isBlank()) return ResponseEntity.badRequest().build();

        if (findById(id) == null) return ResponseEntity.notFound().build();

        flowService.updateMailTagById(id, tag);
        return ResponseEntity.ok(new RestResponse<>(findById(id), buildMailLinks(findById(id))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> apiDeleteMail(@PathVariable("id") String id) {
        if (findById(id) == null) return ResponseEntity.notFound().build();

        flowService.updateMailTagById(id, EisenhowerAction.DELETE.name());

        Map<String, String> response = new HashMap<>();
        response.put("status", "deleted");
        response.put("messageId", id);
        response.put("collection", "/api/mails");
        return ResponseEntity.ok(response);
    }


    private Mail findById(String id) {
        return flowService.getMails().stream()
                .filter(m -> m.getMessageId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Map<String, String> buildMailLinks(Mail mail) {
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/mails/" + mail.getMessageId());
        links.put("collection", "/api/mails");
        links.put("update-status", "/api/mails/" + mail.getMessageId() + "/status");
        links.put("update-tag", "/api/mails/" + mail.getMessageId() + "/tag");
        links.put("delete", "/api/mails/" + mail.getMessageId());

        if (mail.getAction() == EisenhowerAction.DELEGATE) {
            links.put("delegate-auto", "/delegate-auto?messageId=" + mail.getMessageId());
        } else if (mail.getAction() == EisenhowerAction.PLAN) {
            links.put("prepare-meeting", "/events/prepare?messageId=" + mail.getMessageId());
        } else if (mail.getAction() == EisenhowerAction.PENDING) {
            links.put("analyze", "/api/mails/analyze");
        }

        return links;
    }
}