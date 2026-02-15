package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import projet.devops.Mail.Classifier.Persona;
import projet.devops.Mail.Classifier.PersonaResourceService;
import projet.devops.Mail.Service.MailFlowService;
import projet.devops.Mail.Service.NoteService;

@Controller
public class MainController {

    private final MailFlowService flowService;
    private final NoteService noteService;

    public MainController(MailFlowService flowService, NoteService noteService) {
        this.flowService = flowService;
        this.noteService = noteService;
    }
    
    // --- SECTION INBOX (ACCUEIL) ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("view", "inbox"); 
        model.addAttribute("mails", flowService.getMails());
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }

    // --- SECTION KNOWLEDGE (NOTES SYNTHÉTISÉES) ---
    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge"); 
        // Récupère la liste des notes (chargée depuis le JSON au démarrage)
        model.addAttribute("notes", noteService.getNotes()); 
        model.addAttribute("currentPersona", PersonaResourceService.loadPersona());
        return "mails";
    }


    // --- Suprimer une note spécifique (en fonction de son index dans la liste) ---
    @PostMapping("/delete-note")
    public String deleteNote(@RequestParam("index") int index) {
        noteService.deleteNote(index);
        return "redirect:/knowledge";
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

    // --- ACTIONS OBSIDIAN (DOUBLE ANALYSE IA + PERSISTANCE) ---
    @PostMapping("/import-obsidian")
    public String importObsidian(@RequestParam("files") MultipartFile[] files) {
        try {
            // 1. Récupération du profil actif pour influencer l'IA
            Persona current = PersonaResourceService.loadPersona();
            
            // 2. Déclenchement de la Feature 4 & 5 (Synthèse + Triage Eisenhower)
            // Cette méthode gère aussi la sauvegarde automatique en JSON
            noteService.generateAiKnowledge(files, current);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'importation IA : " + e.getMessage());
        }
        
        // Redirection vers l'onglet Knowledge pour voir le résultat
        return "redirect:/knowledge";
    }
    
    // --- CONFIGURATION ---
    @PostMapping("/persona")
    public String changePersona(@RequestParam String persona) throws Exception {
        PersonaResourceService.savePersona(Persona.valueOf(persona.toUpperCase()));
        return "redirect:/";
    }

    @PostMapping("/update-note-tag")
    public String updateNoteTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        noteService.updateNoteTag(index, tag);
        return "redirect:/knowledge"; // On recharge pour voir le changement de couleur
    }
}