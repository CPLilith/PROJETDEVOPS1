package projet.devops.Mail.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Service.CustomDoTagService;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CustomDoTagService customDoTagService;

    public GlobalModelAdvice(CustomDoTagService customDoTagService) {
        this.customDoTagService = customDoTagService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        // Injection du Persona (ou étudiant par défaut)
        try {
            model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        } catch (Exception e) {
            model.addAttribute("currentPersona", Persona.ETUDIANT);
        }

        // Injection des Tags personnalisés
        model.addAttribute("customDoTags", customDoTagService.getCustomTags());
    }
}