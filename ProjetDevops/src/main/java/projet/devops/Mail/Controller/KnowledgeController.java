package projet.devops.Mail.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Service.NoteService;
import projet.devops.Mail.Service.PersonaResourceService;

@Controller
public class KnowledgeController {

    private static final String REDIRECT_KNOWLEDGE = "redirect:/knowledge";
    private final NoteService noteService;

    public KnowledgeController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/knowledge")
    public String knowledge(Model model) {
        model.addAttribute("view", "knowledge");
        List<Note> sortedNotes = new ArrayList<>(noteService.getNotes());
        Collections.reverse(sortedNotes);
        model.addAttribute("notes", sortedNotes);
        return "knowledge";
    }

    @PostMapping("/knowledge/import")
    public String importNotes(@RequestParam("files") MultipartFile[] files) throws Exception {
        noteService.importMarkdownFiles(files);
        return REDIRECT_KNOWLEDGE;
    }

    @PostMapping("/knowledge/merge")
    public String mergeSelectedNotes(@RequestParam(value = "selectedNotes", required = false) List<Integer> selectedNotes) throws Exception {
        if (selectedNotes != null && !selectedNotes.isEmpty()) {
            noteService.mergeSelectedNotes(selectedNotes, PersonaResourceService.loadPersona());
        }
        return REDIRECT_KNOWLEDGE;
    }

    @PostMapping("/update-note-tag")
    public String updateNoteTag(@RequestParam("index") int index, @RequestParam("tag") String tag) {
        noteService.updateNoteTag(index, tag);
        return REDIRECT_KNOWLEDGE;
    }

    @PostMapping("/knowledge/delete")
    public String deleteNote(@RequestParam("index") int index) {
        noteService.deleteNote(index);
        return REDIRECT_KNOWLEDGE;
    }
}