package projet.devops.Mail.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Mail;
import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.MailFlowService.DelegationData;
import projet.devops.Mail.Service.NoteService;

record EventItem(String title, String dateLieu, String type, String sourceId) {}
record RestResponse<T>(T data, Map<String, String> _links) {}

@Controller
public class MainController {

    private final MailFlowService flowService;
    private final NoteService noteService;
    private final EisenhowerClassifier classifier;

    public MainController(MailFlowService flowService, NoteService noteService, EisenhowerClassifier classifier) {
        this.flowService = flowService;
        this.noteService = noteService;
        this.classifier = classifier;
    }

    private void addPersonaToModel(Model model) {
        try {
            model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        } catch (Exception e) {
            model.addAttribute("currentPersona", Persona.ETUDIANT);
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox");
        List<Mail> currentMails = flowService.getMails();
        if (currentMails == null) currentMails = new ArrayList<>();
        
        List<Mail> sortedMails = new ArrayList<>(currentMails);
        if (!sortedMails.isEmpty()) Collections.reverse(sortedMails);
        
        model.addAttribute("mails", sortedMails);
        addPersonaToModel(model);
        return "mails";
    }

    @GetMapping("/kanban")
    public String kanban(Model model) {
        List<Mail> mails = flowService.getMails();
        if (mails == null) mails = new ArrayList<>();
        
        model.addAttribute("todoMails", mails.stream()
            .filter(m -> m.getAction() == EisenhowerAction.DELEGATE && !"FINALISÉ".equals(m.getStatus())).toList());
        
        model.addAttribute("doneMails", mails.stream()
            .filter(m -> "FINALISÉ".equals(m.getStatus())).toList());
        
        model.addAttribute("view", "kanban");
        addPersonaToModel(model);
        return "mails";
    }

    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        List<Note> sortedNotes = new ArrayList<>(noteService.getNotes());
        Collections.reverse(sortedNotes);
        model.addAttribute("notes", sortedNotes);
        addPersonaToModel(model);
        return "mails"; 
    }

    @GetMapping("/events")
    public String showEvents(Model model) {
        List<EventItem> events = new ArrayList<>();
        if (flowService.getMails().isEmpty()) {
            try { flowService.fetchMails(); } catch (Exception e) {}
        }
        for (Mail m : flowService.getMails()) {
            if (m.getAction() == EisenhowerAction.DO) {
                String ex = classifier.extractEventDetails(m.getContent());
                String displayDate = ex.contains("AUCUN") ? "⚠️ À Planifier (Urgent)" : ex;
                events.add(new EventItem(m.getSubject(), displayDate, "MAIL", m.getMessageId()));
            }
        }
        Collections.reverse(events);
        model.addAttribute("view", "events");
        model.addAttribute("events", events);
        addPersonaToModel(model);
        return "mails";
    }

    @PostMapping("/delegate-auto")
    @ResponseBody
    public DelegationData delegateAuto(@RequestParam String messageId) {
        return flowService.processDelegation(messageId);
    }

    @PostMapping("/delegate-manual")
    public String delegateManual(@RequestParam String messageId, @RequestParam String assignee) {
        flowService.processManualDelegation(messageId, assignee);
        return "redirect:/kanban";
    }

    @PostMapping("/update-mail-tag") 
    public String updateMailTag(@RequestParam("messageId") String messageId, @RequestParam("tag") String tag) {
        flowService.updateMailTagById(messageId, tag);
        return "redirect:/";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam String messageId, @RequestParam String status) {
        flowService.updateStatusById(messageId, status.toUpperCase());
        return "redirect:/kanban";
    }

    @PostMapping("/fetch") public String fetch() throws Exception { flowService.fetchMails(); return "redirect:/"; }
    @PostMapping("/analyze") public String analyze() { flowService.processPendingMails(PersonaResourceService.loadPersona()); return "redirect:/"; }
    @PostMapping("/sync") public String sync() { flowService.syncToGmail(); return "redirect:/"; }
    @PostMapping("/auto-status") public String autoStatus() { flowService.detectStatusWithAI(); return "redirect:/kanban"; }
    
    @PostMapping("/persona") 
    public String persona(@RequestParam("persona") String p) throws Exception { 
        PersonaResourceService.savePersona(Persona.valueOf(p.toUpperCase())); 
        return "redirect:/"; 
    }

    @GetMapping("/api") @ResponseBody public RestResponse<String> api() { return new RestResponse<>("API Ready", new HashMap<>()); }
}