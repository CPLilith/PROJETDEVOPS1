package projet.devops.Mail.Controller;

import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Mail;
import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.NoteService;
import projet.devops.Mail.Classifier.StatusClassifier;

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

    // --- VUES ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox");
        List<Mail> sortedMails = new ArrayList<>(flowService.getMails());
        Collections.reverse(sortedMails);
        model.addAttribute("mails", sortedMails);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    @GetMapping("/kanban")
    public String kanban(Model model) {
        List<Mail> mails = flowService.getMails();
        if (mails == null) mails = new ArrayList<>();
        
        // "À SUIVRE" = Uniquement DELEGATE non fini
        model.addAttribute("todoMails", mails.stream()
            .filter(m -> m.getAction() == EisenhowerAction.DELEGATE && !"FINALISÉ".equals(m.getStatus())).toList());
        
        // "FINALISÉ" = Tout ce qui est fini
        model.addAttribute("doneMails", mails.stream()
            .filter(m -> "FINALISÉ".equals(m.getStatus())).toList());
        
        model.addAttribute("view", "kanban");
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        List<Note> sortedNotes = new ArrayList<>(noteService.getNotes());
        Collections.reverse(sortedNotes);
        model.addAttribute("notes", sortedNotes);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails"; 
    }

    @GetMapping("/events")
    public String showEvents(Model model) {
        List<EventItem> events = new ArrayList<>();
        if (flowService.getMails().isEmpty()) try { flowService.fetchMails(); } catch (Exception e) {}

        for (Mail m : flowService.getMails()) {
            if (m.getAction() == EisenhowerAction.DO) {
                String ex = classifier.extractEventDetails(m.getContent());
                if (!ex.contains("AUCUN")) events.add(new EventItem(m.getSubject(), ex, "MAIL", m.getMessageId()));
            }
        }
        Collections.reverse(events);
        model.addAttribute("view", "events");
        model.addAttribute("events", events);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // --- ACTIONS CORRIGÉES ---

    // C'EST ICI QUE CA CHANGE : On utilise messageId
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

    // --- AUTRES ACTIONS ---
    @PostMapping("/fetch") public String fetch() throws Exception { flowService.fetchMails(); return "redirect:/"; }
    @PostMapping("/analyze") public String analyze() { flowService.processPendingMails(PersonaResourceService.loadPersona()); return "redirect:/"; }
    @PostMapping("/sync") public String sync() { flowService.syncToGmail(); return "redirect:/"; }
    @PostMapping("/auto-status") public String autoStatus() { flowService.detectStatusWithAI(); return "redirect:/kanban"; }
    @PostMapping("/persona") public String persona(@RequestParam String p) throws Exception { PersonaResourceService.savePersona(Persona.valueOf(p)); return "redirect:/"; }
    
    // Notes (restent par index pour l'instant)
    @PostMapping("/update-note-tag") public String upNote(@RequestParam int index, @RequestParam String tag) {
        int realIndex = noteService.getNotes().size() - 1 - index;
        noteService.updateNoteTag(realIndex, tag);
        return "redirect:/knowledge";
    }
    @PostMapping("/delete-note") public String delNote(@RequestParam int index) {
        int realIndex = noteService.getNotes().size() - 1 - index;
        noteService.deleteNote(realIndex);
        return "redirect:/knowledge";
    }
    @PostMapping("/import-obsidian") public String impObs(@RequestParam("files") MultipartFile[] f) {
        try { noteService.generateAiKnowledge(f, PersonaResourceService.loadPersona()); } catch (Exception e) {}
        return "redirect:/knowledge";
    }
    
    // API (Conservée)
    @GetMapping("/api") @ResponseBody public RestResponse<String> api() { return new RestResponse<>("API Ready", new HashMap<>()); }
}