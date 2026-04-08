package projet.devops.Mail.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import projet.devops.Mail.Model.Persona;
import projet.devops.Mail.Repository.PersonaRepository;
import projet.devops.Mail.Service.CustomDoTagService;
import projet.devops.Mail.Service.CustomProfileService;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CustomDoTagService customDoTagService;
    private final PersonaRepository personaRepository;
    private final CustomProfileService customProfileService;

    public GlobalModelAdvice(CustomDoTagService customDoTagService, PersonaRepository personaRepository,
                             CustomProfileService customProfileService) {
        this.customDoTagService = customDoTagService;
        this.personaRepository = personaRepository;
        this.customProfileService = customProfileService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        try {
            model.addAttribute("currentPersona", personaRepository.load());
        } catch (Exception e) {
            model.addAttribute("currentPersona", Persona.NEUTRE);
        }
        try {
            model.addAttribute("currentPersonaRaw", personaRepository.loadRaw());
        } catch (Exception e) {
            model.addAttribute("currentPersonaRaw", "NEUTRE");
        }
        model.addAttribute("customDoTags", customDoTagService.getCustomTags());
        model.addAttribute("customProfiles", customProfileService.getProfiles());
    }
}