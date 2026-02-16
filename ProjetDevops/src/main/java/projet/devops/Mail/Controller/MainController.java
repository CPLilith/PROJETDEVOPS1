package projet.devops.Mail.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Mail;
import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.NoteService;

// Records sortis de la classe pour éviter les problèmes de visibilité
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

    // ==========================================
    // --- SECTION 1 : VUES HTML (WEB) ---
    // ==========================================

    // --- INBOX ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox");
        List<Mail> sortedMails = new ArrayList<>(flowService.getMails());
        Collections.reverse(sortedMails);
        model.addAttribute("mails", sortedMails);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // --- KNOWLEDGE ---
    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        List<Note> sortedNotes = new ArrayList<>(noteService.getNotes());
        Collections.reverse(sortedNotes);
        model.addAttribute("notes", sortedNotes);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails"; 
    }

    // --- AGENDA (EVENTS) ---
    @GetMapping("/events")
    public String showEvents(Model model) {
        List<EventItem> events = new ArrayList<>();
        Persona current = PersonaResourceService.loadPersona();

        if (flowService.getMails().isEmpty()) {
            try { flowService.fetchMails(); } catch (Exception e) {}
        }

        // Extraction pour les Mails (Seulement DO)
        for (Mail mail : flowService.getMails()) {
            if (mail.getAction() == EisenhowerAction.DO) {
                String extraction = classifier.extractEventDetails(mail.getContent());
                if (!extraction.contains("AUCUN")) {
                    events.add(new EventItem(mail.getSubject(), extraction, "MAIL", mail.getMessageId()));
                }
            }
        }
        
        // Extraction pour les Notes
        for (Note note : noteService.getNotes()) {
            if ("DO".equalsIgnoreCase(note.getAction())) {
                 String extraction = classifier.extractEventDetails(note.getContent());
                 if (!extraction.contains("AUCUN")) {
                    events.add(new EventItem(note.getTitle(), extraction, "NOTE", note.getId()));
                }
            }
        }
        
        Collections.reverse(events);
        model.addAttribute("view", "events");
        model.addAttribute("events", events);
        model.addAttribute("currentPersona", current);
        return "mails";
    }

    // --- KANBAN (SUIVI DÉLÉGATION) ---
    @GetMapping("/kanban")
    public String kanban(Model model) {
        List<Mail> mails = flowService.getMails();
        if (mails == null) mails = new ArrayList<>();

        // LOGIQUE KANBAN : "À SUIVRE" = Seulement les DELEGATE non finis
        List<Mail> delegues = mails.stream()
            .filter(m -> m.getAction() == EisenhowerAction.DELEGATE) 
            .filter(m -> !"FINALISÉ".equals(m.getStatus()))          
            .toList();

        // "FINALISÉ" = Tout ce qui est fini
        List<Mail> termines = mails.stream()
            .filter(m -> "FINALISÉ".equals(m.getStatus()))
            .toList();
        
        model.addAttribute("todoMails", delegues);
        model.addAttribute("doneMails", termines);
        
        model.addAttribute("view", "kanban");
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // ==========================================
    // --- SECTION 2 : API REST (Conservée du Remote) ---
    // ==========================================

    @GetMapping("/api")
    @ResponseBody
    public RestResponse<String> apiRoot() {
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api");
        links.put("mails", "/api/mails");
        links.put("notes", "/api/notes");
        links.put("events", "/api/events");
        links.put("persona", "/api/persona");
        return new RestResponse<>("Bienvenue sur l'API EisenFlow", links);
    }

    @GetMapping("/api/mails")
    @ResponseBody
    public List<RestResponse<Mail>> apiMails() {
        List<Mail> mails = getSortedMails();
        List<RestResponse<Mail>> response = new ArrayList<>();
        for (int i = 0; i < mails.size(); i++) {
            Map<String, String> links = new HashMap<>();
            links.put("self", "/api/mails/" + i);
            links.put("update_tag", "/update-mail-tag?index=" + i + "&tag={tag}");
            response.add(new RestResponse<>(mails.get(i), links));
        }
        return response;
    }

    @GetMapping("/api/events")
    @ResponseBody
    public RestResponse<List<EventItem>> apiEvents() {
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/events");
        links.put("refresh", "/events");
        return new RestResponse<>(getCalculatedEvents(), links);
    }

    // ==========================================
    // --- SECTION 3 : ACTIONS ---
    // ==========================================

    // --- ACTIONS KANBAN ---
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam String messageId, @RequestParam String status) {
        flowService.updateStatusById(messageId, status.toUpperCase());
        return "redirect:/kanban";
    }

    @PostMapping("/auto-status")
    public String autoStatus() {
        flowService.detectStatusWithAI();
        return "redirect:/kanban";
    }

    // --- ACTIONS FLUX MAILS ---
    @PostMapping("/fetch")
    public String fetchMails() throws Exception {
        flowService.fetchMails();
        return "redirect:/";
    }

    @PostMapping("/analyze")
    public String analyzeMails() {
        flowService.processPendingMails(PersonaResourceService.loadPersona());
        return "redirect:/";
    }

    @PostMapping("/sync")
    public String sync() {
        flowService.syncToGmail();
        return "redirect:/";
    }

    @PostMapping("/update-mail-tag") 
    public String updateMailTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        int realIndex = flowService.getMails().size() - 1 - index;
        flowService.updateMailTag(realIndex, tag);
        return "redirect:/";
    }

    // --- ACTIONS NOTES / OBSIDIAN ---
    @PostMapping("/import-obsidian") 
    public String importObsidian(@RequestParam("files") MultipartFile[] files) {
        try { noteService.generateAiKnowledge(files, PersonaResourceService.loadPersona()); } catch (Exception e) {}
        return "redirect:/knowledge";
    }

    @PostMapping("/delete-note") 
    public String deleteNote(@RequestParam("index") int index) {
        int realIndex = noteService.getNotes().size() - 1 - index;
        noteService.deleteNote(realIndex);
        return "redirect:/knowledge";
    }

    @PostMapping("/update-note-tag") 
    public String updateNoteTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        int realIndex = noteService.getNotes().size() - 1 - index;
        noteService.updateNoteTag(realIndex, tag);
        return "redirect:/knowledge";
    }

    @PostMapping("/persona")
    public String changePersona(@RequestParam String persona) throws Exception {
        PersonaResourceService.savePersona(Persona.valueOf(persona.toUpperCase()));
        return "redirect:/";
    }

    // ==========================================
    // --- METHODES PRIVEES (Utilitaires pour API) ---
    // ==========================================

    private List<Mail> getSortedMails() {
        List<Mail> sorted = new ArrayList<>(flowService.getMails());
        Collections.reverse(sorted);
        return sorted;
    }

    private List<Note> getSortedNotes() {
        List<Note> sorted = new ArrayList<>(noteService.getNotes());
        Collections.reverse(sorted);
        return sorted;
    }

    private List<EventItem> getCalculatedEvents() {
        List<EventItem> events = new ArrayList<>();
        if (flowService.getMails().isEmpty()) {
            try { flowService.fetchMails(); } catch (Exception e) {}
        }
        for (Mail m : flowService.getMails()) {
            if (m.getAction() == EisenhowerAction.DO) {
                String extract = classifier.extractEventDetails(m.getContent());
                if (!extract.contains("AUCUN")) events.add(new EventItem(m.getSubject(), extract, "MAIL", m.getMessageId()));
            }
        }
        for (Note n : noteService.getNotes()) {
            if ("DO".equalsIgnoreCase(n.getAction())) {
                String extract = classifier.extractEventDetails(n.getContent());
                if (!extract.contains("AUCUN")) events.add(new EventItem(n.getTitle(), extract, "NOTE", n.getId()));
            }
        }
        Collections.reverse(events);
        return events;
    }
}