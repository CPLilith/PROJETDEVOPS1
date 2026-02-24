package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;

import projet.devops.Mail.Service.CustomDoTagService;

@Controller
public class TagApiController {

    private final CustomDoTagService customDoTagService;

    public TagApiController(CustomDoTagService customDoTagService) {
        this.customDoTagService = customDoTagService;
    }

    @PostMapping("/tags/create")
    public String createTag(@RequestParam("label") String label, Model model) {
        String created = customDoTagService.createTag(label);
        if (created == null) {
            model.addAttribute("tagError", "Ce tag existe déjà ou le nom est invalide.");
        } else {
            model.addAttribute("tagSuccess", "Tag créé : " + CustomDoTagService.toLabel(created));
        }
        model.addAttribute("view", "tags");
        return "mails";
    }

    @PostMapping("/tags/delete")
    public String deleteTag(@RequestParam("tagName") String tagName) {
        customDoTagService.deleteTag(tagName);
        return "redirect:/tags";
    }

    @GetMapping("/api/tags")
    @ResponseBody
    public List<Map<String, String>> apiGetTags() {
        return customDoTagService.getCustomTags().stream()
                .map(t -> Map.of("name", t, "label", CustomDoTagService.toLabel(t)))
                .toList();
    }

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