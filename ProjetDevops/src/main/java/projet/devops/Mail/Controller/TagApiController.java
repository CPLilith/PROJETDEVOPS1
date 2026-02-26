package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;

import projet.devops.Mail.Service.CustomDoTagService;
import projet.devops.Mail.Service.MailFlowService;

/**
 * Contrôleur gérant la création et la suppression des tags personnalisés.
 * Supporte les requêtes classiques (Formulaires) et asynchrones (AJAX).
 */
@Controller
public class TagApiController {

    private final CustomDoTagService customDoTagService;
    private final MailFlowService flowService;

    public TagApiController(CustomDoTagService customDoTagService, MailFlowService flowService) {
        this.customDoTagService = customDoTagService;
        this.flowService = flowService;
    }

    /**
     * Création d'un tag via formulaire standard.
     * Utilise une redirection pour garantir la mise à jour de la liste affichée.
     */
    @PostMapping("/tags/create")
    public String createTag(@RequestParam("label") String label, RedirectAttributes redirectAttributes) {
        String created = customDoTagService.createTag(label);

        if (created == null) {
            redirectAttributes.addFlashAttribute("tagError", "Ce tag existe déjà ou le nom est invalide.");
        } else {
            redirectAttributes.addFlashAttribute("tagSuccess", "Tag créé : " + CustomDoTagService.toLabel(created));
        }

        // On redirige vers l'URL /tags pour forcer le rechargement des données par le
        // HomeController
        return "redirect:/tags";
    }

    /**
     * Suppression d'un tag.
     */
    @PostMapping("/tags/delete")
    public String deleteTag(@RequestParam("tagName") String tagName) {
        // 1. On supprime le tag de la liste globale
        customDoTagService.deleteTag(tagName);

        // 2. On nettoie les mails qui l'utilisaient
        flowService.cleanMailsAfterTagDeletion(tagName);

        return "redirect:/tags";
    }

    /**
     * API JSON : Retourne la liste des tags pour les composants JS.
     */
    @GetMapping("/api/tags")
    @ResponseBody
    public List<Map<String, String>> apiGetTags() {
        return customDoTagService.getCustomTags().stream()
                .map(t -> Map.of("name", t, "label", CustomDoTagService.toLabel(t)))
                .toList();
    }

    /**
     * Création rapide d'un tag (AJAX) depuis le lecteur de mail.
     */
    @PostMapping("/tags/create-ajax")
    @ResponseBody
    public Map<String, String> createTagAjax(@RequestParam("label") String label) {
        String created = customDoTagService.createTag(label);
        if (created == null) {
            return Map.of("error", "Tag déjà existant ou nom invalide.");
        }
        return Map.of("tag", created, "label", CustomDoTagService.toLabel(created));
    }
}