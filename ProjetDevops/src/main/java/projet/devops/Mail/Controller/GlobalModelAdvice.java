package projet.devops.Mail.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Repository.PersonaRepository;
import projet.devops.Mail.Service.CustomDoTagService;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CustomDoTagService customDoTagService;
    private final PersonaRepository personaRepository; 

    public GlobalModelAdvice(CustomDoTagService customDoTagService, PersonaRepository personaRepository) {
        this.customDoTagService = customDoTagService;
        this.personaRepository = personaRepository;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        try {
            model.addAttribute("currentPersona", personaRepository.load());
        } catch (Exception e) {
            model.addAttribute("currentPersona", Persona.NEUTRE);
        }
        model.addAttribute("customDoTags", customDoTagService.getCustomTags());
    }
}