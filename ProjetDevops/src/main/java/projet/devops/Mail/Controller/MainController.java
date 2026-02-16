package projet.devops.Mail.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import projet.devops.Mail.Classifier.EisenhowerAction;
import projet.devops.Mail.Classifier.EisenhowerClassifier;
import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Mail;
import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.NoteService;

// Record sorti de la classe pour éviter les problèmes de visibilité
record EventItem(String title, String dateLieu, String type, String sourceId) {}

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

    // --- SECTION INBOX ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox");
        List<Mail> sortedMails = new ArrayList<>(flowService.getMails());
        Collections.reverse(sortedMails);
        model.addAttribute("mails", sortedMails);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // --- SECTION KNOWLEDGE ---
    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        List<Note> sortedNotes = new ArrayList<>(noteService.getNotes());
        Collections.reverse(sortedNotes);
        model.addAttribute("notes", sortedNotes);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails"; 
    }

    // --- SECTION AGENDA (EVENTS) ---
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

    // --- SECTION KANBAN (SUIVI DÉLÉGATION) ---
    @GetMapping("/kanban")
    public String kanban(Model model) {
        List<Mail> mails = flowService.getMails();
        if (mails == null) mails = new ArrayList<>();

        // LOGIQUE MODIFIÉE ICI :
        // On ne garde QUE les mails classés DELEGATE dans la colonne "À SUIVRE".
        // Les mails PENDING, DO ou PLAN n'apparaîtront pas ici.
        List<Mail> delegues = mails.stream()
            .filter(m -> m.getAction() == EisenhowerAction.DELEGATE) // Filtre Strict
            .filter(m -> !"FINALISÉ".equals(m.getStatus()))          // Pas encore fini
            .toList();

        // Pour la colonne FINALISÉ, on affiche tout ce qui est terminé (pour l'historique)
        List<Mail> termines = mails.stream()
            .filter(m -> "FINALISÉ".equals(m.getStatus()))
            .toList();
        
        model.addAttribute("todoMails", delegues);
        model.addAttribute("doneMails", termines);
        
        model.addAttribute("view", "kanban");
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // --- ACTIONS ---

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

    // --- ACTIONS STANDARDS ---

    @PostMapping("/fetch") public String fetchMails() throws Exception { flowService.fetchMails(); return "redirect:/"; }
    @PostMapping("/analyze") public String analyzeMails() { flowService.processPendingMails(PersonaResourceService.loadPersona()); return "redirect:/"; }
    @PostMapping("/sync") public String sync() { flowService.syncToGmail(); return "redirect:/"; }
    @PostMapping("/persona") public String changePersona(@RequestParam String persona) throws Exception { PersonaResourceService.savePersona(Persona.valueOf(persona.toUpperCase())); return "redirect:/"; }
    
    @PostMapping("/update-mail-tag") 
    public String updateMailTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        int realIndex = flowService.getMails().size() - 1 - index;
        flowService.updateMailTag(realIndex, tag);
        return "redirect:/";
    }

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
}