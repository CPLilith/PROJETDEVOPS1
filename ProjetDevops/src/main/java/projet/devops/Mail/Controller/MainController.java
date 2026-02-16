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

    public record EventItem(String title, String dateLieu, String type, String sourceId) {}

    // --- SECTION INBOX (Affichage Inversé) ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox");
        List<Mail> sortedMails = new ArrayList<>(flowService.getMails());
        Collections.reverse(sortedMails); // On inverse pour l'affichage
        model.addAttribute("mails", sortedMails);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // --- SECTION KNOWLEDGE (Affichage Inversé) ---
    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        List<Note> sortedNotes = new ArrayList<>(noteService.getNotes());
        Collections.reverse(sortedNotes); // On inverse pour l'affichage
        model.addAttribute("notes", sortedNotes);
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails"; 
    }

    // --- SECTION EVENTS ---
    @GetMapping("/events")
    public String showEvents(Model model) {
        System.out.println("\n========== 📅 CHARGEMENT AGENDA ==========");
        List<EventItem> events = new ArrayList<>();
        Persona current = PersonaResourceService.loadPersona();

        if (flowService.getMails().isEmpty()) {
            try { flowService.fetchMails(); } catch (Exception e) {}
        }

        // Scan des Mails
        List<Mail> mails = flowService.getMails();
        if (mails != null) {
            for (Mail mail : mails) {
                if (mail.getAction() == EisenhowerAction.DO) {
                    String extraction = classifier.extractEventDetails(mail.getContent());
                    if (!extraction.contains("AUCUN")) {
                        events.add(new EventItem(mail.getSubject(), extraction, "MAIL", mail.getMessageId()));
                    }
                }
            }
        }

        // Scan des Notes
        List<Note> notes = noteService.getNotes();
        if (notes != null) {
            for (Note note : notes) {
                String action = note.getAction() != null ? note.getAction() : "NULL";
                if ("DO".equalsIgnoreCase(action)) {
                     String extraction = classifier.extractEventDetails(note.getContent());
                     if (!extraction.contains("AUCUN")) {
                        events.add(new EventItem(note.getTitle(), extraction, "NOTE", note.getId()));
                    }
                }
            }
        }
        
        Collections.reverse(events); // On inverse aussi les events

        model.addAttribute("view", "events");
        model.addAttribute("events", events);
        model.addAttribute("currentPersona", current);
        return "mails";
    }

    // --- ACTIONS (CORRECTION DES INDEX INVERSÉS) ---

    @PostMapping("/delete-note")
    public String deleteNote(@RequestParam("index") int index) {
        // Formule magique : (Taille - 1) - IndexAffiché = VraiIndex
        List<Note> notes = noteService.getNotes();
        if (notes != null && !notes.isEmpty()) {
            int realIndex = notes.size() - 1 - index;
            noteService.deleteNote(realIndex);
        }
        return "redirect:/knowledge";
    }

    @PostMapping("/update-note-tag")
    public String updateNoteTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        // Correction de l'index pour les Notes
        List<Note> notes = noteService.getNotes();
        if (notes != null && !notes.isEmpty()) {
            int realIndex = notes.size() - 1 - index;
            noteService.updateNoteTag(realIndex, tag);
        }
        return "redirect:/knowledge";
    }

    @PostMapping("/update-mail-tag")
    public String updateMailTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        // Correction de l'index pour les Mails
        List<Mail> mails = flowService.getMails();
        if (mails != null && !mails.isEmpty()) {
            int realIndex = mails.size() - 1 - index;
            flowService.updateMailTag(realIndex, tag);
        }
        return "redirect:/";
    }

    // --- AUTRES ACTIONS (Sans index) ---

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

    @PostMapping("/import-obsidian")
    public String importObsidian(@RequestParam("files") MultipartFile[] files) {
        try {
            Persona current = PersonaResourceService.loadPersona();
            noteService.generateAiKnowledge(files, current);
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/knowledge";
    }
    
    @PostMapping("/persona")
    public String changePersona(@RequestParam String persona) throws Exception {
        PersonaResourceService.savePersona(Persona.valueOf(persona.toUpperCase()));
        return "redirect:/";
    }
}