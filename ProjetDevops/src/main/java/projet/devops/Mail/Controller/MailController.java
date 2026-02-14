package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Service.MailFlowService;

@Controller
public class MailController {

    private final MailFlowService flowService;

    public MailController(MailFlowService flowService) {
        this.flowService = flowService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("mails", flowService.getMails());
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    @PostMapping("/fetch")
    public String fetchMails() throws Exception {
        flowService.fetchMails();
        return "redirect:/";
    }

    @PostMapping("/analyze")
    public String analyzeMails() {
        Persona current = PersonaResourceService.loadPersona();
        flowService.processPendingMails(current);
        return "redirect:/";
    }

    @PostMapping("/sync")
    public String sync() {
        flowService.syncToGmail();
        return "redirect:/";
    }
    
    @PostMapping("/persona")
    public String changePersona(@RequestParam String persona) throws Exception {
        PersonaResourceService.savePersona(Persona.valueOf(persona.toUpperCase()));
        return "redirect:/";
    }
}