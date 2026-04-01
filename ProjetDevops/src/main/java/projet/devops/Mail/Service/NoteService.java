package projet.devops.Mail.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import projet.devops.Mail.Classifier.OllamaClient;
import projet.devops.Mail.Model.Note;
import projet.devops.Mail.Model.Persona;
import projet.devops.Mail.Repository.NoteRepository;

@Service
public class NoteService {

    private final OllamaClient ollamaClient;
    private final NoteRepository noteRepository;
    private final ExternalNoteApiService notionService; // 1. Injection du service Notion

    private List<Note> notes;

    public NoteService(OllamaClient ollamaClient, NoteRepository noteRepository, ExternalNoteApiService notionService) {
        this.ollamaClient = ollamaClient;
        this.noteRepository = noteRepository;
        this.notionService = notionService;
    }

    @PostConstruct
    public void init() {
        this.notes = noteRepository.loadNotes();
    }

    public List<String> importMarkdownFiles(MultipartFile[] files) {
        List<String> importedIds = new ArrayList<>();
        for (MultipartFile f : files) {
            try {
                if (f.getOriginalFilename() != null && f.getOriginalFilename().endsWith(".md")) {
                    String content = new String(f.getBytes(), StandardCharsets.UTF_8);
                    String title = f.getOriginalFilename().replace(".md", "");
                    Note note = new Note(title, "Import", content, "PENDING", "IMPORT");
                    notes.add(0, note);
                    importedIds.add(note.getId());
                }
            } catch (Exception e) {
                System.err.println("Erreur import fichier : " + f.getOriginalFilename());
            }
        }
        noteRepository.saveNotes(notes);
        return importedIds;
    }

    public void mergeSelectedNotes(List<String> selectedIds, Persona persona) throws Exception {
        if (selectedIds == null || selectedIds.isEmpty()) return;

        // 2. CRITIQUE : Créer une liste de TOUTES les notes possibles (Locales + Notion)
        List<Note> allPossibleNotes = new ArrayList<>(this.notes);
        allPossibleNotes.addAll(notionService.fetchAllNotionNotes());

        // 3. On filtre maintenant sur cette liste complète
        List<Note> selectedNotes = allPossibleNotes.stream()
                .filter(note -> selectedIds.contains(note.getId()))
                .collect(Collectors.toList());

        System.out.println("🚀 Tentative de fusion de " + selectedNotes.size() + " notes");

        if (selectedNotes.size() < 2) {
            System.err.println("⚠️ Pas assez de notes trouvées pour fusionner.");
            return;
        }

        StringBuilder rawContext = new StringBuilder();
        for (Note n : selectedNotes) {
            rawContext.append("\n--- NOTE : ").append(n.getTitle()).append(" ---\n");
            rawContext.append(n.getContent()).append("\n");
        }

        String synthesisPrompt = "En tant que " + persona.name()
                + ", fais une synthèse comparative intelligente de ces notes en français. "
                + "Identifie les points clés :\n" + rawContext;

        String aiSynthesis = ollamaClient.generateResponse("tinyllama", synthesisPrompt);

        String tagPrompt = "Répond uniquement par un mot (DO, PLAN, DELEGATE ou DELETE) pour classer ce texte : " + aiSynthesis;
        String eisenhowerTag = ollamaClient.generateResponse("tinyllama", tagPrompt).trim().toUpperCase();

        if (!Arrays.asList("DO", "PLAN", "DELEGATE", "DELETE").contains(eisenhowerTag)) {
            eisenhowerTag = "PENDING";
        }

        String mergedTitles = selectedNotes.stream().map(Note::getTitle).collect(Collectors.joining(", "));
        Note newNote = new Note("Synthèse IA : " + mergedTitles, "AI Orchestrator", aiSynthesis, eisenhowerTag, "MERGE");
        
        // 4. On supprime uniquement les notes qui étaient locales
        notes.removeIf(note -> selectedIds.contains(note.getId()));
        
        // 5. On ajoute la synthèse (qui devient une note locale définitive)
        notes.add(0, newNote);
        
        noteRepository.saveNotes(notes);
        System.out.println("✅ Nouvelle note de synthèse créée avec succès !");
    }

    public void generateAiKnowledge(MultipartFile[] files, Persona persona) throws Exception {
        List<String> importedIds = importMarkdownFiles(files);
        mergeSelectedNotes(importedIds, persona);
    }

    public void deleteNote(String id) {
        boolean removed = notes.removeIf(note -> note.getId().equals(id));
        if (removed) {
            noteRepository.saveNotes(notes);
        }
    }

    public void updateNoteTag(String id, String tag) {
        for (Note note : notes) {
            if (note.getId().equals(id)) {
                note.setAction(tag);
                noteRepository.saveNotes(notes);
                break;
            }
        }
    }

    public List<Note> getNotes() {
        return notes;
    }
}