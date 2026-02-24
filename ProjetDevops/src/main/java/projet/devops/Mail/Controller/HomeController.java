package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import projet.devops.Mail.Mail;
import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Service.MailFlowService;

@Controller
public class HomeController {

    private final MailFlowService flowService;

    public HomeController(MailFlowService flowService) {
        this.flowService = flowService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox");
        List<Mail> currentMails = flowService.getMails();
        if (currentMails == null)
            currentMails = new ArrayList<>();

        List<Mail> sortedMails = new ArrayList<>(currentMails);
        if (!sortedMails.isEmpty())
            Collections.reverse(sortedMails);

        model.addAttribute("mails", sortedMails);
        return "mails";
    }

    @GetMapping("/kanban")
    public String kanban(Model model) {
        List<Mail> mails = flowService.getMails();
        if (mails == null)
            mails = new ArrayList<>();

        model.addAttribute("todoMails", mails.stream()
                .filter(m -> m.getAction() == EisenhowerAction.DELEGATE && !"FINALISÉ".equals(m.getStatus())).toList());
        model.addAttribute("doneMails", mails.stream()
                .filter(m -> "FINALISÉ".equals(m.getStatus())).toList());
        model.addAttribute("view", "kanban");
        return "mails";
    }

    @GetMapping("/tags")
    public String tagsPage(Model model) {
        model.addAttribute("view", "tags");
        return "mails";
    }
}