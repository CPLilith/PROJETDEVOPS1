package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

import projet.devops.Mail.Model.Persona;
import projet.devops.Mail.Repository.PersonaRepository;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.MailFlowService.DelegationData;

@Controller
public class MailActionController {

    private static final String REDIRECT_HOME   = "redirect:/";
    private static final String REDIRECT_KANBAN = "redirect:/kanban";

    private final MailFlowService flowService;
    private final PersonaRepository personaRepository; 

    public MailActionController(MailFlowService flowService, PersonaRepository personaRepository) {
        this.flowService = flowService;
        this.personaRepository = personaRepository;
    }

    @PostMapping("/fetch")
    public String fetch() throws Exception {
        flowService.fetchMails();
        return REDIRECT_HOME;
    }

    @PostMapping("/sync")
    public String sync() throws Exception {
        flowService.syncToGmail();
        flowService.fetchMails();
        return REDIRECT_HOME;
    }

    @PostMapping("/analyze")
    public String analyze() {
        flowService.processPendingMails(personaRepository.load());
        return REDIRECT_HOME;
    }

    @PostMapping("/update-mail-tag")
    public String updateMailTag(@RequestParam("messageId") String messageId,
                                @RequestParam("tag") String tag) {
        flowService.updateMailTagById(messageId, tag);
        return REDIRECT_HOME;
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam String messageId, @RequestParam String status) {
        flowService.updateStatusById(messageId, status.toUpperCase());
        return REDIRECT_KANBAN;
    }

    @PostMapping("/auto-status")
    public String autoStatus() {
        flowService.detectStatusWithAI();
        return REDIRECT_KANBAN;
    }

    // --- DÉLÉGATION ---
    @PostMapping("/delegate-auto")
    @ResponseBody
    public DelegationData delegateAuto(@RequestParam String messageId) {
        return flowService.suggestDelegation(messageId);
    }

    @PostMapping("/delegate-confirm")
    @ResponseBody
    public Map<String, String> delegateConfirm(@RequestParam String messageId, @RequestParam String assignee, @RequestParam String draftBody) {
        flowService.confirmDelegation(messageId, assignee, draftBody);
        return Map.of("status", "success");
    }

    @PostMapping("/delegate-manual")
    public String delegateManual(@RequestParam String messageId, @RequestParam String assignee) {
        flowService.processManualDelegation(messageId, assignee);
        return REDIRECT_KANBAN;
    }

    // --- PERSONA ---
    @PostMapping("/persona")
    public String persona(@RequestParam("persona") String p) {
        personaRepository.save(Persona.valueOf(p.toUpperCase()));
        return REDIRECT_HOME;
    }
}