package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile; // Important pour l'upload

import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.NoteService;

@Controller
public class MainController {

    private final MailFlowService flowService;
    private final NoteService noteService;

    /**
     * Le constructeur injecte les deux services nécessaires au fonctionnement 
     * de l'Inbox et de la Knowledge Base.
     */
    public MainController(MailFlowService flowService, NoteService noteService) {
        this.flowService = flowService;
        this.noteService = noteService;
    }
    
    // --- SECTION INBOX (ACCUEIL) ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox"); // Active la vue Inbox dans le HTML
        model.addAttribute("mails", flowService.getMails());
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // --- SECTION KNOWLEDGE (NOTES) ---
    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge"); // Active la vue Notes dans le HTML
        model.addAttribute("notes", noteService.getNotes()); 
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // --- ACTIONS GMAIL ---
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

    // --- ACTIONS OBSIDIAN (CORRIGÉ POUR EXPLORATEUR) ---
    @PostMapping("/import-obsidian")
    public String importObsidian(@RequestParam("files") MultipartFile[] files) {
        try {
            // On récupère le persona actuel pour adapter le ton de la synthèse
            Persona current = PersonaResourceService.loadPersona();
            noteService.generateAiSynthesis(files, current);
        } catch (Exception e) {
            System.err.println("Erreur IA : " + e.getMessage());
        }
        return "redirect:/knowledge";
    }
    
    // --- CONFIGURATION ---
    @PostMapping("/persona")
    public String changePersona(@RequestParam String persona) throws Exception {
        PersonaResourceService.savePersona(Persona.valueOf(persona.toUpperCase()));
        return "redirect:/";
    }
}