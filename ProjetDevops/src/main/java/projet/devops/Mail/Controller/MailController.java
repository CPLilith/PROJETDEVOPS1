package projet.devops.Mail.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;

import projet.devops.Mail.Mail;
import projet.devops.Mail.Service.MailFlowService;
import java.util.List;


@RestController
@RequestMapping("/api")
public class MailController {

    private final MailFlowService flowService;

    public MailController(MailFlowService flowService) {
        this.flowService = flowService;
    }

    @GetMapping("/mails")
    public List<Mail> getMails() throws Exception {
        return flowService.fetchMails();
    }

    @GetMapping("/kanban")
    public String kanban(Model model) {
        List<Mail> mails = flowService.getMails();
        
        // Filtrage pour le Kanban
        model.addAttribute("todoMails", mails.stream()
            .filter(m -> !"FINALISÉ".equals(m.getStatus())).toList());
        model.addAttribute("doneMails", mails.stream()
            .filter(m -> "FINALISÉ".equals(m.getStatus())).toList());
        
        model.addAttribute("view", "kanban");
        return "mails"; // On réutilise ton template de base
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam int index, @RequestParam String status) {
        List<Mail> mails = flowService.getMails();
        if (index >= 0 && index < mails.size()) {
            mails.get(index).setStatus(status.toUpperCase());
        }
        return "redirect:/kanban";
    }

    @PostMapping("/auto-status")
    public String autoStatus() {
        flowService.detectStatusWithAI();
        return "redirect:/kanban";
    }
}
