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

    // Structure compatible Niveau 3 : Data + Liens
    public record RestResponse<T>(T data, Map<String, String> _links) {}
    public record EventItem(String title, String dateLieu, String type, String sourceId) {}

    // ==========================================
    // --- SECTION 1 : VUES HTML (WEB) ---
    // ==========================================

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox");
        model.addAttribute("mails", getSortedMails());
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        model.addAttribute("notes", getSortedNotes());
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails"; 
    }

    @GetMapping("/events")
    public String showEvents(Model model) {
        model.addAttribute("view", "events");
        model.addAttribute("events", getCalculatedEvents());
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // ==========================================
    // --- SECTION 2 : API REST NIVEAU 3 (JSON + HATEOAS) ---
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
    // --- SECTION 3 : ACTIONS (Inchangées) ---
    // ==========================================

    @PostMapping("/update-mail-tag")
    public String updateMailTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        List<Mail> mails = flowService.getMails();
        if (mails != null && !mails.isEmpty()) {
            int realIndex = mails.size() - 1 - index;
            flowService.updateMailTag(realIndex, tag);
        }
        return "redirect:/";
    }

    @PostMapping("/update-note-tag")
    public String updateNoteTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        List<Note> notes = noteService.getNotes();
        if (notes != null && !notes.isEmpty()) {
            int realIndex = notes.size() - 1 - index;
            noteService.updateNoteTag(realIndex, tag);
        }
        return "redirect:/knowledge";
    }

    @PostMapping("/delete-note")
    public String deleteNote(@RequestParam("index") int index) {
        List<Note> notes = noteService.getNotes();
        if (notes != null && !notes.isEmpty()) {
            int realIndex = notes.size() - 1 - index;
            noteService.deleteNote(realIndex);
        }
        return "redirect:/knowledge";
    }

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

    @PostMapping("/persona")
    public String changePersona(@RequestParam String persona) throws Exception {
        PersonaResourceService.savePersona(Persona.valueOf(persona.toUpperCase()));
        return "redirect:/";
    }

    // ==========================================
    // --- METHODES PRIVEES (LOGIQUE PARTAGÉE) ---
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
