package projet.devops.Mail.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import projet.devops.Mail.Mail;
import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Service.MailFlowService;

// Le record utilisé pour formater la réponse JSON avec les liens HATEOAS
record RestResponse<T>(T data, Map<String, String> _links) {
}

@RestController // <-- Remplace @Controller + @ResponseBody, c'est plus DRY pour les API !
@RequestMapping("/api/mails") // <-- Applique "/api/mails" à toutes les méthodes de la classe (DRY)
public class MailRestApiController {

    private final MailFlowService flowService;

    public MailRestApiController(MailFlowService flowService) {
        this.flowService = flowService;
    }

    /**
     * Endpoint racine de l'API (Collection de ressources)
     * Renvoye la liste de tous les mails avec des liens dynamiques
     */
    @GetMapping
    public RestResponse<List<RestResponse<Mail>>> apiGetAllMails() {
        List<Mail> mails = flowService.getMails();
        if (mails == null)
            mails = new ArrayList<>();

        List<RestResponse<Mail>> mailResponses = new ArrayList<>();

        for (Mail mail : mails) {
            Map<String, String> links = new HashMap<>();

            // Liens universels pour cette ressource (Niveau 3)
            links.put("self", "/api/mails/" + mail.getMessageId());
            links.put("update-status", "/update-status?messageId=" + mail.getMessageId());

            // Liens contextuels (Le cœur du niveau 3 HATEOAS)
            if (mail.getAction() == EisenhowerAction.DELEGATE) {
                links.put("delegate-auto", "/delegate-auto?messageId=" + mail.getMessageId());
            } else if (mail.getAction() == EisenhowerAction.PLAN) {
                links.put("prepare-meeting", "/events/prepare?messageId=" + mail.getMessageId());
            } else if (mail.getAction() == EisenhowerAction.PENDING) {
                links.put("analyze", "/analyze");
            }

            mailResponses.add(new RestResponse<>(mail, links));
        }

        // Liens globaux pour l'API
        Map<String, String> globalLinks = new HashMap<>();
        globalLinks.put("self", "/api/mails");
        globalLinks.put("fetch", "/fetch");
        globalLinks.put("sync", "/sync");

        return new RestResponse<>(mailResponses, globalLinks);
    }

    /**
     * Endpoint d'une ressource unique
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<Mail>> apiGetMailById(@PathVariable("id") String id) {
        Mail foundMail = flowService.getMails().stream()
                .filter(m -> m.getMessageId().equals(id))
                .findFirst()
                .orElse(null);

        if (foundMail == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/mails/" + id);
        links.put("collection", "/api/mails");
        links.put("update-status", "/update-status?messageId=" + id);

        // HATEOAS : on ne propose la génération de PDF que si le mail est tagué PLAN
        if (foundMail.getAction() == EisenhowerAction.PLAN) {
            links.put("prepare-meeting", "/events/prepare?messageId=" + id);
        }
        if (foundMail.getAction() == EisenhowerAction.DELEGATE) {
            links.put("delegate-auto", "/delegate-auto?messageId=" + id);
        }

        return ResponseEntity.ok(new RestResponse<>(foundMail, links));
    }
}