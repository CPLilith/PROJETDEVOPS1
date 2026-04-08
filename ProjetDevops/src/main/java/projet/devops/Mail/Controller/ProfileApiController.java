package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import projet.devops.Mail.Service.CustomProfileService;

@Controller
public class ProfileApiController {

    private final CustomProfileService customProfileService;

    public ProfileApiController(CustomProfileService customProfileService) {
        this.customProfileService = customProfileService;
    }

    @PostMapping("/profiles/create")
    public String createProfile(@RequestParam("label") String label, RedirectAttributes redirectAttributes) {
        String created = customProfileService.createProfile(label);
        if (created == null) {
            redirectAttributes.addFlashAttribute("profileError", "Ce profil existe déjà ou le nom est invalide.");
        } else {
            redirectAttributes.addFlashAttribute("profileSuccess", "Profil créé : " + CustomProfileService.toLabel(created));
        }
        return "redirect:/tags";
    }

    @PostMapping("/profiles/delete")
    public String deleteProfile(@RequestParam("profileName") String profileName) {
        customProfileService.deleteProfile(profileName);
        return "redirect:/tags";
    }
}
