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

import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Service.ExternalNoteApiService;
import projet.devops.Mail.Service.NoteService;
import projet.devops.Mail.Service.PersonaResourceService;

@Controller
public class KnowledgeController {

    private static final String REDIRECT_KNOWLEDGE = "redirect:/knowledge";
    
    private final NoteService noteService;
    // Ajout du service Notion
    private final ExternalNoteApiService notionService;

    // Injection des deux services dans le constructeur
    public KnowledgeController(NoteService noteService, ExternalNoteApiService notionService) {
        this.noteService = noteService;
        this.notionService = notionService;
    }

    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        
        // 1. On récupère les notes locales et on les inverse (les plus récentes en haut)
        List<Note> allNotes = new ArrayList<>(noteService.getNotes());
        Collections.reverse(allNotes);
        
        // 2. On récupère les notes Notion
        List<Note> notionNotes = notionService.fetchAllNotionNotes();
        
        // 3. On ajoute les notes Notion au TOUT DÉBUT de la liste principale
        allNotes.addAll(0, notionNotes);
        
        // 4. On envoie la liste fusionnée à la vue
        model.addAttribute("notes", allNotes);
        
        return "knowledge";
    }

    @PostMapping("/knowledge/import")
    public String importNotes(@RequestParam("files") MultipartFile[] files) throws Exception {
        noteService.importMarkdownFiles(files);
        return REDIRECT_KNOWLEDGE;
    }

    @PostMapping("/knowledge/merge")
    // On passe d'une List<Integer> à une List<String>
    public String mergeSelectedNotes(@RequestParam(value = "selectedNotes", required = false) List<String> selectedNotesIds) throws Exception {
        if (selectedNotesIds != null && !selectedNotesIds.isEmpty()) {
            noteService.mergeSelectedNotes(selectedNotesIds, PersonaResourceService.loadPersona());
        }
        return REDIRECT_KNOWLEDGE;
    }

    @PostMapping("/update-note-tag")
    public String updateNoteTag(@RequestParam("id") String id, @RequestParam("tag") String tag) { // Remplacé index par id
        noteService.updateNoteTag(id, tag);
        return REDIRECT_KNOWLEDGE;
    }

    @PostMapping("/knowledge/delete")
    public String deleteNote(@RequestParam(value = "id", required = false) String id) {
        // Si la requête fantôme arrive sans ID, on la redirige silencieusement sans crasher
        if (id == null || id.isEmpty()) {
            return "redirect:/knowledge";
        }
        
        System.out.println("🗑️ Demande de suppression pour l'ID : " + id);
        noteService.deleteNote(id);
        
        return "redirect:/knowledge";
    }
}