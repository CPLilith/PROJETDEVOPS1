package projet.devops.Mail.Controller;

import java.util.ArrayList;
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

    // --- SECTION INBOX ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox"); 
        model.addAttribute("mails", flowService.getMails());
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails"; // On utilise le template unique mails.html
    }

    // --- SECTION KNOWLEDGE ---
    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge"); 
        model.addAttribute("notes", noteService.getNotes());
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails"; 
    }

    // --- SECTION EVENTS (AGENDA) ---
    @GetMapping("/events")
    public String showEvents(Model model) {
        System.out.println("\n========== 🕵️ DÉBUT DEBUG EVENTS (AVEC AUTO-FETCH) ==========");
        List<EventItem> events = new ArrayList<>();
        Persona current = PersonaResourceService.loadPersona();

        // 1. AUTO-FETCH FIX : Si la mémoire est vide (F5), on recharge Gmail
        if (flowService.getMails().isEmpty()) {
            System.out.println("⚠️ Mémoire vide ! Tentative de téléchargement automatique...");
            try {
                flowService.fetchMails();
            } catch (Exception e) {
                System.err.println("❌ Erreur auto-fetch : " + e.getMessage());
            }
        }

        // 2. DEBUG MAILS
        List<Mail> mails = flowService.getMails();
        System.out.println("📧 Mails en mémoire : " + (mails != null ? mails.size() : 0));
        
        if (mails != null) {
            for (Mail mail : mails) {
                // On n'affiche le détail que si c'est un DO pour ne pas polluer la console
                if (mail.getAction() == EisenhowerAction.DO) {
                    System.out.print("   - [MAIL DO] '" + mail.getSubject() + "'");
                    System.out.print(" -> 🧠 Analyse IA...");
                    
                    String extraction = classifier.extractEventDetails(mail.getContent());
                    System.out.println(" Résultat: " + extraction);
                    
                    if (!extraction.contains("AUCUN")) {
                        events.add(new EventItem(mail.getSubject(), extraction, "MAIL", mail.getMessageId()));
                    }
                }
            }
        }

        // 3. DEBUG NOTES
        List<Note> notes = noteService.getNotes();
        System.out.println("📝 Notes en mémoire : " + (notes != null ? notes.size() : 0));

        if (notes != null) {
            for (Note note : notes) {
                String action = note.getAction() != null ? note.getAction() : "NULL";
                
                if ("DO".equalsIgnoreCase(action)) {
                     System.out.print("   - [NOTE DO] '" + note.getTitle() + "'");
                     System.out.print(" -> 🧠 Analyse IA...");
                     
                     String extraction = classifier.extractEventDetails(note.getContent());
                     System.out.println(" Résultat: " + extraction);
                     
                     if (!extraction.contains("AUCUN")) {
                        events.add(new EventItem(note.getTitle(), extraction, "NOTE", note.getId()));
                    }
                }
            }
        }
        
        System.out.println("🎯 TOTAL ÉVÉNEMENTS AFFICHÉS : " + events.size());
        System.out.println("========== FIN DEBUG EVENTS ==========\n");

        model.addAttribute("view", "events");
        model.addAttribute("events", events);
        model.addAttribute("currentPersona", current);
        return "mails"; // Important : renvoie vers mails.html
    }

    // --- LE RESTE DES MÉTHODES ---

    @PostMapping("/delete-note")
    public String deleteNote(@RequestParam("index") int index) {
        noteService.deleteNote(index);
        return "redirect:/knowledge";
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

    @PostMapping("/import-obsidian")
    public String importObsidian(@RequestParam("files") MultipartFile[] files) {
        try {
            Persona current = PersonaResourceService.loadPersona();
            noteService.generateAiKnowledge(files, current);
        } catch (Exception e) {
            System.err.println("❌ Erreur import IA : " + e.getMessage());
        }
        return "redirect:/knowledge";
    }
    
    @PostMapping("/persona")
    public String changePersona(@RequestParam String persona) throws Exception {
        PersonaResourceService.savePersona(Persona.valueOf(persona.toUpperCase()));
        return "redirect:/";
    }

    @PostMapping("/update-note-tag")
    public String updateNoteTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        noteService.updateNoteTag(index, tag);
        return "redirect:/knowledge";
    }

    @PostMapping("/update-mail-tag")
    public String updateMailTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        flowService.updateMailTag(index, tag);
        return "redirect:/";
    }
}